package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural Synthesizer for Submarine AR Acoustics.
 * Generates smooth, resonant, organic marine sounds using harmonic synthesis,
 * exponential envelopes, and smooth low-pass filtering to avoid digital clipping.
 */
object MarineSoundEngine {

  private val audioScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private const val SAMPLE_RATE = 44100
  private var isStaticLooping = false
  private var staticVolume = 0f
  private var staticJob: Job? = null

  private fun playPcm(samples: ShortArray) {
    try {
      val minBufSize = AudioTrack.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
      )
      val bufSize = maxOf(minBufSize, samples.size * 2)

      val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(bufSize)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      audioTrack.write(samples, 0, samples.size)
      audioTrack.play()
      audioScope.launch {
        delay((samples.size * 1000L / SAMPLE_RATE) + 120L)
        try {
          audioTrack.stop()
          audioTrack.release()
        } catch (_: Exception) {}
      }
    } catch (_: Exception) {}
  }

  // Smooth Resonant Sonar Ping with Sub-Surface Reverberation
  fun playSonarPing() {
    audioScope.launch {
      val durationMs = 380
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)
      val baseFreq = 840.0

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val decay = exp(-t * 9.0) // smooth exponential decay
        val fundamental = sin(2.0 * PI * baseFreq * t)
        val octave = sin(2.0 * PI * (baseFreq * 2.0) * t) * 0.35
        val sub = sin(2.0 * PI * (baseFreq * 0.5) * t) * 0.2
        val wave = (fundamental + octave + sub) * decay
        samples[i] = (wave * 24000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Mechanical Tactile Flashlight Switch Click
  fun playFlashlightClick(isOn: Boolean) {
    audioScope.launch {
      val durationMs = 35
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)
      val clickFreq = if (isOn) 1900.0 else 1400.0

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val decay = exp(-t * 90.0)
        val wave = sin(2.0 * PI * clickFreq * t) * decay
        samples[i] = (wave * 22000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Capacitor Discharge & Electric Lightning Shock
  fun playElectricShock() {
    audioScope.launch {
      val durationMs = 450
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val envelope = exp(-t * 6.5)
        // Electric spark crackle + sub-buzz
        val subBuzz = sin(2.0 * PI * 85.0 * t) * 0.5
        val hum = sin(2.0 * PI * 180.0 * t) * 0.3
        val spark = (Random.nextDouble() * 2.0 - 1.0) * 0.35
        val wave = (subBuzz + hum + spark) * envelope
        samples[i] = (wave * 28000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Atmospheric Abyssal Drone for Haywire (Tense, Cinematic Sub-bass instead of harsh screech)
  fun playHaywireAlarm() {
    audioScope.launch {
      val durationMs = 650
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        // Ominous pulsing frequency modulation (90Hz to 140Hz)
        val fmFreq = 110.0 + sin(2.0 * PI * 6.0 * t) * 35.0
        val drone = sin(2.0 * PI * fmFreq * t) * 0.65
        val subOctave = sin(2.0 * PI * (fmFreq * 0.5) * t) * 0.45
        // Soft water cavitation shimmer
        val cavitation = (Random.nextDouble() * 2.0 - 1.0) * 0.15
        val envelope = (sin(PI * (i.toDouble() / numSamples))).coerceIn(0.0, 1.0)
        val wave = (drone + subOctave + cavitation) * envelope
        samples[i] = (wave * 26000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Distinct Decoy Acoustic Whoosh for Fake Charge (Bubble Cavitation Dispersal)
  fun playDecoyWhoosh() {
    audioScope.launch {
      val durationMs = 500
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        // Swirling bubbling pitch from 300Hz down to 120Hz
        val freq = 320.0 - (t * 380.0).coerceAtLeast(0.0)
        val bubble = sin(2.0 * PI * freq * t) * 0.6
        val hiss = (Random.nextDouble() * 2.0 - 1.0) * 0.2
        val envelope = exp(-t * 5.0)
        val wave = (bubble + hiss) * envelope
        samples[i] = (wave * 22000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Deep Approaching Cavitation Roar for Real Charge
  fun playRealChargeApproach() {
    audioScope.launch {
      val durationMs = 750
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        // Accelerating Doppler frequency
        val freq = 65.0 + (t * 80.0)
        val rumble = sin(2.0 * PI * freq * t) * 0.7
        val waterSwirl = sin(2.0 * PI * (freq * 1.5) * t) * 0.3
        val rise = (t / (durationMs / 1000.0)).coerceIn(0.0, 1.0)
        val wave = (rumble + waterSwirl) * rise
        samples[i] = (wave * 28000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Súper Pez Resonant Forcefield Shield Hum
  fun playShieldHum() {
    audioScope.launch {
      val durationMs = 450
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val chord1 = sin(2.0 * PI * 440.0 * t) * 0.4
        val chord2 = sin(2.0 * PI * 554.37 * t) * 0.3 // C#
        val chord3 = sin(2.0 * PI * 659.25 * t) * 0.3 // E
        val env = (1.0 - (i.toDouble() / numSamples)).coerceIn(0.0, 1.0)
        val wave = (chord1 + chord2 + chord3) * env
        samples[i] = (wave * 24000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Soft Water Bubble Pop for Tab / Navigation Click
  fun playNavClick() {
    audioScope.launch {
      val durationMs = 28
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)
      val freq = 1250.0

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val decay = exp(-t * 110.0)
        val wave = sin(2.0 * PI * freq * t) * decay
        samples[i] = (wave * 18000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Harmonious Discovery Chime
  fun playSuccessChime() {
    audioScope.launch {
      val durationMs = 500
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val progress = t / (durationMs / 1000.0)
        val freq = if (progress < 0.33) 523.25 else if (progress < 0.66) 659.25 else 783.99
        val decay = exp(-(t % 0.16) * 14.0)
        val wave = sin(2.0 * PI * freq * t) * decay
        samples[i] = (wave * 24000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Subsea Hull Splash Impact
  fun playJumpscareSplash() {
    audioScope.launch {
      val durationMs = 600
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.6
        val boom = sin(2.0 * PI * 70.0 * t) * 0.6
        val decay = exp(-t * 5.0)
        val wave = (noise + boom) * decay
        samples[i] = (wave * 30000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Tense proximity alert / countdown tick when creature charges
  fun playChargeWarningTick(urgency: Float) {
    audioScope.launch {
      val durationMs = 45
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)
      val baseFreq = 880.0 + (urgency * 440.0)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val decay = exp(-t * 80.0)
        val wave = sin(2.0 * PI * baseFreq * t) * decay
        samples[i] = (wave * 20000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  // Safe chime when player successfully averts eyes and survives Haywire
  fun playAvertSuccess() {
    audioScope.launch {
      val durationMs = 280
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val freq = 587.33 + (t * 400.0) // Ascending D5
        val decay = exp(-t * 12.0)
        val wave = sin(2.0 * PI * freq * t) * decay
        samples[i] = (wave * 22000).toInt().coerceIn(-32768, 32767).toShort()
      }
      playPcm(samples)
    }
  }

  fun updateStaticLoop(intensity: Float) {
    staticVolume = intensity.coerceIn(0f, 1f)
    if (staticVolume > 0.08f && !isStaticLooping) {
      startStaticLoop()
    }
  }

  fun stopStaticLoop() {
    isStaticLooping = false
    staticJob?.cancel()
    staticJob = null
  }

  private fun startStaticLoop() {
    if (isStaticLooping) return
    isStaticLooping = true

    staticJob = audioScope.launch {
      val bufferSamples = 2205
      val samples = ShortArray(bufferSamples)

      try {
        val audioTrack = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(SAMPLE_RATE)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(bufferSamples * 2 * 2)
          .setTransferMode(AudioTrack.MODE_STREAM)
          .build()

        audioTrack.play()

        while (isActive && isStaticLooping && staticVolume > 0.05f) {
          // Filtered hydrophone water static
          for (i in 0 until bufferSamples) {
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.35
            val subHiss = sin(2.0 * PI * 140.0 * (i.toDouble() / SAMPLE_RATE)) * 0.25
            val amp = (noise + subHiss) * staticVolume * 16000
            samples[i] = amp.toInt().coerceIn(-32768, 32767).toShort()
          }
          audioTrack.write(samples, 0, samples.size)
        }

        audioTrack.stop()
        audioTrack.release()
      } catch (_: Exception) {}
      isStaticLooping = false
    }
  }
}
