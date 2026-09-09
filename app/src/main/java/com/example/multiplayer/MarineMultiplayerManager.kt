package com.example.multiplayer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.UUID

sealed interface MarinePeerState {
  data object Idle : MarinePeerState
  data object Listening : MarinePeerState
  data object Connecting : MarinePeerState
  data class Connected(val deviceName: String) : MarinePeerState
  data class Error(val message: String) : MarinePeerState
}

data class MarinePeerMessage(
  val type: String,
  val value: Int = 0,
  val text: String = ""
)

class MarineMultiplayerManager(context: Context) {
  companion object {
    private val SERVICE_UUID: UUID = UUID.fromString("d4c2e4f4-76d6-4e1c-a6a0-9a2c6a7d2e41")
  }

  @Suppress("DEPRECATION")
  private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
  private val scope = CoroutineScope(Dispatchers.IO)
  private var socket: BluetoothSocket? = null
  private var serverSocket: BluetoothServerSocket? = null
  private var ioJob: Job? = null
  private var writer: PrintWriter? = null

  private val _state = MutableStateFlow<MarinePeerState>(MarinePeerState.Idle)
  val state: StateFlow<MarinePeerState> = _state.asStateFlow()

  private val _messages = MutableStateFlow<MarinePeerMessage?>(null)
  val messages: StateFlow<MarinePeerMessage?> = _messages.asStateFlow()

  @SuppressLint("MissingPermission")
  fun pairedDevices(): List<BluetoothDevice> {
    val bt = adapter ?: return emptyList()
    return try {
      bt.bondedDevices.toList().sortedBy { it.name ?: it.address }
    } catch (_: SecurityException) {
      _state.value = MarinePeerState.Error("Permiso de Bluetooth no concedido.")
      emptyList()
    }
  }

  @SuppressLint("MissingPermission")
  fun startListening() {
    close()
    val bt = adapter ?: run { _state.value = MarinePeerState.Error("Este teléfono no tiene Bluetooth."); return }
    if (!bt.isEnabled) {
      _state.value = MarinePeerState.Error("Activa Bluetooth para recibir un rival.")
      return
    }
    scope.launch {
      try {
        serverSocket = bt.listenUsingRfcommWithServiceRecord("Fish AR", SERVICE_UUID)
        _state.value = MarinePeerState.Listening
        socket = serverSocket?.accept()
        serverSocket?.close()
        serverSocket = null
        socket?.let { connected ->
          writer = PrintWriter(connected.outputStream, true)
          _state.value = MarinePeerState.Connected(connected.remoteDevice.name ?: "Jugador")
          readLoop(connected)
        }
      } catch (e: SecurityException) {
        _state.value = MarinePeerState.Error("Permiso de Bluetooth no concedido.")
        closeSocketOnly()
      } catch (e: Exception) {
        if (_state.value !is MarinePeerState.Connected) _state.value = MarinePeerState.Error(e.message ?: "No se pudo abrir la conexión")
      }
    }
  }

  @SuppressLint("MissingPermission")
  fun connect(device: BluetoothDevice) {
    val bt = adapter ?: run { _state.value = MarinePeerState.Error("Este teléfono no tiene Bluetooth."); return }
    if (!bt.isEnabled) {
      _state.value = MarinePeerState.Error("Activa Bluetooth para conectarte al rival.")
      return
    }
    close()
    _state.value = MarinePeerState.Connecting
    scope.launch {
      try {
        val connected = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
        connected.connect()
        socket = connected
        writer = PrintWriter(connected.outputStream, true)
        _state.value = MarinePeerState.Connected(device.name ?: device.address)
        readLoop(connected)
      } catch (e: SecurityException) {
        _state.value = MarinePeerState.Error("Permiso de Bluetooth no concedido.")
        closeSocketOnly()
      } catch (e: Exception) {
        _state.value = MarinePeerState.Error(e.message ?: "No se pudo conectar")
        closeSocketOnly()
      }
    }
  }

  fun send(message: MarinePeerMessage): Boolean {
    val out = writer ?: return false
    val safeText = message.text.replace("|", "/").replace("\n", " ")
    out.println(listOf(message.type, message.value, safeText).joinToString("|"))
    return !out.checkError()
  }

  suspend fun requestDirectLinkTest(): Boolean = withContext(Dispatchers.IO) {
    send(MarinePeerMessage("PING", 1, "Fish AR"))
  }

  private fun readLoop(connected: BluetoothSocket) {
    val reader = BufferedReader(InputStreamReader(connected.inputStream))
    ioJob?.cancel()
    ioJob = scope.launch {
      try {
        while (isActive) {
          val line = reader.readLine() ?: break
          val parts = line.split('|', limit = 3)
          if (parts.isNotEmpty()) {
            _messages.value = MarinePeerMessage(
              type = parts[0],
              value = parts.getOrNull(1)?.toIntOrNull() ?: 0,
              text = parts.getOrNull(2).orEmpty()
            )
          }
        }
      } catch (_: Exception) {
        if (_state.value is MarinePeerState.Connected) _state.value = MarinePeerState.Idle
      }
    }
  }

  fun close() {
    ioJob?.cancel()
    closeSocketOnly()
    try { serverSocket?.close() } catch (_: Exception) { }
    serverSocket = null
    writer = null
    _state.value = MarinePeerState.Idle
  }

  private fun closeSocketOnly() {
    try { socket?.close() } catch (_: Exception) { }
    socket = null
  }
}
