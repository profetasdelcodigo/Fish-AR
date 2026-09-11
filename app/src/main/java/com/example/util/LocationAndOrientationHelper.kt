package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RealLocationData(
  val latitude: Double,
  val longitude: Double,
  val accuracyMeters: Float = 5f,
  val isRealGps: Boolean = false
)

class LocationAndOrientationHelper(private val context: Context) : SensorEventListener, LocationListener {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

  private val _deviceHeading = MutableStateFlow(0f)
  val deviceHeading: StateFlow<Float> = _deviceHeading.asStateFlow()

  private val _devicePitch = MutableStateFlow(0f)
  val devicePitch: StateFlow<Float> = _devicePitch.asStateFlow()

  private val _currentLocation = MutableStateFlow(
    RealLocationData(latitude = -5.1970, longitude = -80.6350, isRealGps = false)
  )
  val currentLocation: StateFlow<RealLocationData> = _currentLocation.asStateFlow()

  private val rotationMatrix = FloatArray(9)
  private val orientationAngles = FloatArray(3)
  private var rotationSensor: Sensor? = null
  private var isSensorRunning = false
  private var orientationCallback: ((Float, Float) -> Unit)? = null

  init {
    rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
      ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
  }

  fun startTrackingOrientation(onOrientationChanged: (Float, Float) -> Unit) {
    orientationCallback = onOrientationChanged
    if (isSensorRunning) return
    rotationSensor?.let { sensor ->
      sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
      isSensorRunning = true
    }
  }

  fun stopTrackingOrientation() {
    if (isSensorRunning) {
      sensorManager?.unregisterListener(this)
      isSensorRunning = false
    }
    orientationCallback = null
  }

  @SuppressLint("MissingPermission")
  fun requestRealLocationUpdate() {
    try {
      val lm = locationManager ?: return
      val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
      for (provider in providers) {
        if (lm.isProviderEnabled(provider)) {
          val lastLoc = lm.getLastKnownLocation(provider)
          if (lastLoc != null) updateFromLocation(lastLoc)
          lm.requestLocationUpdates(provider, 3000L, 2f, this)
        }
      }
    } catch (_: SecurityException) {
      // Graceful fallback to the last known/default location.
    }
  }

  fun stopLocationUpdates() {
    try {
      locationManager?.removeUpdates(this)
    } catch (_: Exception) {}
  }

  private fun updateFromLocation(loc: Location) {
    _currentLocation.value = RealLocationData(
      latitude = loc.latitude,
      longitude = loc.longitude,
      accuracyMeters = loc.accuracy,
      isRealGps = true
    )
  }

  private fun publishOrientation(heading: Float, pitch: Float) {
    _deviceHeading.value = heading
    _devicePitch.value = pitch
    orientationCallback?.invoke(heading, pitch)
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null) return
    when (event.sensor.type) {
      Sensor.TYPE_ROTATION_VECTOR -> {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        // Remap coordinate system for portrait camera/AR usage
        val remappedMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
          rotationMatrix,
          SensorManager.AXIS_X,
          SensorManager.AXIS_Z,
          remappedMatrix
        )
        SensorManager.getOrientation(remappedMatrix, orientationAngles)
        val azimuthDeg = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360) % 360).toFloat()
        // In remapped AR coordinates: 0 is eye-level horizon, positive is up, negative is down
        val pitchDeg = Math.toDegrees(orientationAngles[1].toDouble()).toFloat().coerceIn(-60f, 60f)
        publishOrientation(azimuthDeg, pitchDeg)
      }
      Sensor.TYPE_ORIENTATION -> {
        val azimuth = (event.values[0] + 360f) % 360f
        // Sensor.TYPE_ORIENTATION pitch is -90 when vertical; offset so upright is 0
        val rawPitch = event.values[1]
        val pitch = (rawPitch + 90f).coerceIn(-60f, 60f)
        publishOrientation(azimuth, pitch)
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

  override fun onLocationChanged(location: Location) {
    updateFromLocation(location)
  }

  @Deprecated("Deprecated in Java")
  override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
  override fun onProviderEnabled(provider: String) {}
  override fun onProviderDisabled(provider: String) {}
}
