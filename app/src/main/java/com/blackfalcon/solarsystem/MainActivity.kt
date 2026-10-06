package com.blackfalcon.solarsystem

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private lateinit var solarSystemView: SolarSystemView
    private lateinit var tvInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        solarSystemView = findViewById(R.id.solarSystemView)
        tvInfo = findViewById(R.id.tvInfo)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        solarSystemView.setOnPlanetSelectedListener { planet ->
            tvInfo.text = "${planet.name}: ${planet.distanceAU} AU from Sun • ${planet.funFact}"
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
        }

        if (gravity != null && geomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                // orientation[0] = azimuth (rotation around -Z axis)
                val azimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                // Normalize to 0-360
                val heading = (azimuthDegrees + 360) % 360
                solarSystemView.setHeading(heading)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
