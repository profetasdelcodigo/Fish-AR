package com.example.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.UUID

sealed class MultiplayerState {
  object Disconnected : MultiplayerState()
  data class Hosting(val roomName: String) : MultiplayerState()
  data class Connecting(val deviceName: String) : MultiplayerState()
  data class Connected(val peerName: String, val isHost: Boolean) : MultiplayerState()
  data class Error(val message: String) : MultiplayerState()
}

sealed class MultiplayerMessage {
  data class ScoreUpdate(val username: String, val score: Int, val fishesCaught: Int, val combo: Int) : MultiplayerMessage()
  data class SabotageTriggered(val type: String, val fromUser: String) : MultiplayerMessage()
  data class CoopAction(val actionType: String, val value: Int, val extra: String = "") : MultiplayerMessage()
  data class CoopSharedCatch(val speciesId: String, val fishName: String, val points: Int) : MultiplayerMessage()
  data class CoopHullUpdate(val newHullPercent: Int) : MultiplayerMessage()
  data class MatchStart(val mode: String, val initialSeed: Long) : MultiplayerMessage()
  data class SpawnFish(val speciesId: String, val initialHeading: Float, val distance: Float) : MultiplayerMessage()
  data class PlayerReady(val username: String) : MultiplayerMessage()
  data class PlayerDied(val username: String) : MultiplayerMessage()
  data class FinalMatchResults(val username: String, val score: Int, val captures: Int, val bestFish: String) : MultiplayerMessage()
  data class ChatOrAlert(val sender: String, val text: String) : MultiplayerMessage()
}

class MarineMultiplayerManager(private val context: Context) {
  private val scope = CoroutineScope(Dispatchers.IO + Job())
  private val bluetoothAdapter: BluetoothAdapter? = try {
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
    manager.adapter
  } catch (e: Exception) {
    null
  }

  private val _connectionState = MutableStateFlow<MultiplayerState>(MultiplayerState.Disconnected)
  val connectionState: StateFlow<MultiplayerState> = _connectionState.asStateFlow()

  private val _incomingMessages = MutableSharedFlow<MultiplayerMessage>(extraBufferCapacity = 64)
  val incomingMessages: SharedFlow<MultiplayerMessage> = _incomingMessages.asSharedFlow()

  private var serverSocket: BluetoothServerSocket? = null
  private var activeSocket: BluetoothSocket? = null
  private var writer: PrintWriter? = null
  private var reader: BufferedReader? = null
  private var listenJob: Job? = null

  companion object {
    private const val TAG = "MarineMultiplayer"
    private const val APP_SERVICE_NAME = "PescActivateAR"
    private val SECURE_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
  }

  @SuppressLint("MissingPermission")
  fun getPairedDevices(): List<Pair<String, String>> {
    return try {
      bluetoothAdapter?.bondedDevices?.map { it.name to it.address } ?: emptyList()
    } catch (e: Exception) {
      emptyList()
    }
  }

  private fun hasBluetoothPermissions(): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
      context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
      context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  @SuppressLint("MissingPermission")
  fun startHostRoom(roomName: String = "PescActivate-Host") {
    disconnect()
    
    if (!hasBluetoothPermissions()) {
      _connectionState.value = MultiplayerState.Error("Faltan permisos de Bluetooth. Por favor, acéptalos.")
      return
    }

    if (bluetoothAdapter == null) {
      _connectionState.value = MultiplayerState.Error("Este dispositivo no soporta Bluetooth.")
      return
    }

    if (!bluetoothAdapter.isEnabled) {
      _connectionState.value = MultiplayerState.Error("El Bluetooth está desactivado. Actívalo para jugar.")
      return
    }

    _connectionState.value = MultiplayerState.Hosting(roomName)

    scope.launch {
      try {
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_SERVICE_NAME, SECURE_UUID)
        Log.d(TAG, "Bluetooth RFCOMM Server listening...")
        val socket = serverSocket?.accept(60000) // 60s timeout
        if (socket != null) {
          setupActiveSocket(socket, peerName = socket.remoteDevice?.name ?: "P2P Rival", isHost = true)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Bluetooth server accept error: ${e.message}")
        _connectionState.value = MultiplayerState.Error("Error al crear sala: ${e.message}")
      }
    }
  }

  @SuppressLint("MissingPermission")
  fun connectToDevice(deviceAddress: String, deviceName: String) {
    disconnect()

    if (!hasBluetoothPermissions()) {
      _connectionState.value = MultiplayerState.Error("Faltan permisos de Bluetooth.")
      return
    }

    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
      _connectionState.value = MultiplayerState.Error("Bluetooth no disponible o desactivado.")
      return
    }

    _connectionState.value = MultiplayerState.Connecting(deviceName)

    scope.launch {
      try {
        val device: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(deviceAddress)
        if (device != null) {
          bluetoothAdapter.cancelDiscovery()
          val socket = device.createRfcommSocketToServiceRecord(SECURE_UUID)
          socket.connect()
          setupActiveSocket(socket, peerName = device.name ?: deviceName, isHost = false)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Bluetooth client connection failed: ${e.message}")
        _connectionState.value = MultiplayerState.Error("No se pudo conectar con $deviceName.")
      }
    }
  }

  /**
   * Quick Simulation / Virtual Direct Link Mode:
   * Enables fair organizers to test or run 2-player mode directly even when BT hardware is paired virtually
   */
  fun simulateConnect(peerName: String, isHost: Boolean) {
    disconnect()
    _connectionState.value = MultiplayerState.Connected(peerName = peerName, isHost = isHost)
    scope.launch {
      _incomingMessages.emit(MultiplayerMessage.ChatOrAlert(peerName, "¡Enlace táctico sincronizado con éxito!"))
    }
  }

  private fun setupActiveSocket(socket: BluetoothSocket, peerName: String, isHost: Boolean) {
    activeSocket = socket
    try {
      writer = PrintWriter(OutputStreamWriter(socket.outputStream), true)
      reader = BufferedReader(InputStreamReader(socket.inputStream))
      _connectionState.value = MultiplayerState.Connected(peerName, isHost)

      listenJob = scope.launch {
        try {
          while (activeSocket?.isConnected == true) {
            val line = reader?.readLine() ?: break
            parseAndDispatchMessage(line)
          }
        } catch (e: Exception) {
          Log.d(TAG, "Socket read terminated: ${e.message}")
        } finally {
          disconnect()
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed setting up socket streams: ${e.message}")
      disconnect()
    }
  }

  fun sendMessage(message: MultiplayerMessage) {
    scope.launch {
      val raw = serializeMessage(message)
      writer?.println(raw)

      // In Simulated / Connected mode, echo actions or maintain live state
      if (_connectionState.value is MultiplayerState.Connected && activeSocket == null) {
        // Virtual local bridge
      }
    }
  }

  // Simulated peer message injector (used for testing or simulated dual rival AI)
  fun injectSimulatedPeerMessage(message: MultiplayerMessage) {
    scope.launch {
      _incomingMessages.emit(message)
    }
  }

  private fun serializeMessage(msg: MultiplayerMessage): String {
    return when (msg) {
      is MultiplayerMessage.ScoreUpdate -> "SCORE|${msg.username}|${msg.score}|${msg.fishesCaught}|${msg.combo}"
      is MultiplayerMessage.SabotageTriggered -> "SABOTAGE|${msg.type}|${msg.fromUser}"
      is MultiplayerMessage.CoopAction -> "COOP_ACT|${msg.actionType}|${msg.value}|${msg.extra}"
      is MultiplayerMessage.CoopSharedCatch -> "COOP_CATCH|${msg.speciesId}|${msg.fishName}|${msg.points}"
      is MultiplayerMessage.CoopHullUpdate -> "COOP_HULL|${msg.newHullPercent}"
      is MultiplayerMessage.MatchStart -> "START|${msg.mode}|${msg.initialSeed}"
      is MultiplayerMessage.SpawnFish -> "SPAWN|${msg.speciesId}|${msg.initialHeading}|${msg.distance}"
      is MultiplayerMessage.ChatOrAlert -> "MSG|${msg.sender}|${msg.text}"
      is MultiplayerMessage.FinalMatchResults -> "FINAL|${msg.username}|${msg.score}|${msg.captures}|${msg.bestFish}"
      is MultiplayerMessage.PlayerDied -> "DIED|${msg.username}"
      is MultiplayerMessage.PlayerReady -> "READY|${msg.username}"
    }
  }

  private suspend fun parseAndDispatchMessage(raw: String) {
    try {
      val parts = raw.split("|")
      if (parts.isEmpty()) return
      val msg: MultiplayerMessage? = when (parts[0]) {
        "SCORE" -> MultiplayerMessage.ScoreUpdate(
          username = parts.getOrNull(1) ?: "Rival",
          score = parts.getOrNull(2)?.toIntOrNull() ?: 0,
          fishesCaught = parts.getOrNull(3)?.toIntOrNull() ?: 0,
          combo = parts.getOrNull(4)?.toIntOrNull() ?: 0
        )
        "SABOTAGE" -> MultiplayerMessage.SabotageTriggered(
          type = parts.getOrNull(1) ?: "fog",
          fromUser = parts.getOrNull(2) ?: "Rival"
        )
        "COOP_ACT" -> MultiplayerMessage.CoopAction(
          actionType = parts.getOrNull(1) ?: "SHOCK",
          value = parts.getOrNull(2)?.toIntOrNull() ?: 0,
          extra = parts.getOrNull(3) ?: ""
        )
        "COOP_CATCH" -> MultiplayerMessage.CoopSharedCatch(
          speciesId = parts.getOrNull(1) ?: "bonito",
          fishName = parts.getOrNull(2) ?: "Bonito",
          points = parts.getOrNull(3)?.toIntOrNull() ?: 100
        )
        "COOP_HULL" -> MultiplayerMessage.CoopHullUpdate(
          newHullPercent = parts.getOrNull(1)?.toIntOrNull() ?: 100
        )
        "START" -> MultiplayerMessage.MatchStart(
          mode = parts.getOrNull(1) ?: "1V1",
          initialSeed = parts.getOrNull(2)?.toLongOrNull() ?: 0L
        )
        "SPAWN" -> MultiplayerMessage.SpawnFish(
          speciesId = parts.getOrNull(1) ?: "bonito",
          initialHeading = parts.getOrNull(2)?.toFloatOrNull() ?: 0f,
          distance = parts.getOrNull(3)?.toFloatOrNull() ?: 25f
        )
        "READY" -> MultiplayerMessage.PlayerReady(
          username = parts.getOrNull(1) ?: "Rival"
        )
        "DIED" -> MultiplayerMessage.PlayerDied(
          username = parts.getOrNull(1) ?: "Rival"
        )
        "FINAL" -> MultiplayerMessage.FinalMatchResults(
          username = parts.getOrNull(1) ?: "Rival",
          score = parts.getOrNull(2)?.toIntOrNull() ?: 0,
          captures = parts.getOrNull(3)?.toIntOrNull() ?: 0,
          bestFish = parts.getOrNull(4) ?: "Ninguno"
        )
        "MSG" -> MultiplayerMessage.ChatOrAlert(
          sender = parts.getOrNull(1) ?: "Compañero",
          text = parts.getOrNull(2) ?: ""
        )
        else -> null
      }
      if (msg != null) {
        _incomingMessages.emit(msg)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing message: $raw", e)
    }
  }

  fun disconnect() {
    listenJob?.cancel()
    listenJob = null
    try {
      writer?.close()
      reader?.close()
      activeSocket?.close()
      serverSocket?.close()
    } catch (e: Exception) {
      // Ignored
    }
    writer = null
    reader = null
    activeSocket = null
    serverSocket = null
    _connectionState.value = MultiplayerState.Disconnected
  }
}
