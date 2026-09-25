package com.example.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SensorStatus(
    val hasHardwareSensor: Boolean,
    val sensorType: String,
    val isListening: Boolean,
    val isSimulating: Boolean
)

class StepSensorManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onStepDetected: (stepsIncrement: Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepDetector: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val stepCounter: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var initialCounterValue: Float? = null
    private var lastCounterValue: Float? = null

    private val _sensorStatus = MutableStateFlow(
        SensorStatus(
            hasHardwareSensor = (stepDetector != null || stepCounter != null),
            sensorType = when {
                stepDetector != null -> "Hardware Step Detector"
                stepCounter != null -> "Hardware Step Counter"
                else -> "Simulated Sensor (No hardware found)"
            },
            isListening = false,
            isSimulating = false
        )
    )
    val sensorStatus: StateFlow<SensorStatus> = _sensorStatus.asStateFlow()

    private var simulatorJob: Job? = null

    fun startListening() {
        var registered = false
        if (sensorManager != null) {
            if (stepDetector != null) {
                registered = sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_UI)
            } else if (stepCounter != null) {
                registered = sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI)
            }
        }
        _sensorStatus.value = _sensorStatus.value.copy(
            isListening = registered
        )
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
        _sensorStatus.value = _sensorStatus.value.copy(isListening = false)
    }

    fun toggleWalkingSimulation() {
        if (simulatorJob?.isActive == true) {
            simulatorJob?.cancel()
            simulatorJob = null
            _sensorStatus.value = _sensorStatus.value.copy(isSimulating = false)
        } else {
            _sensorStatus.value = _sensorStatus.value.copy(isSimulating = true)
            simulatorJob = scope.launch(Dispatchers.Default) {
                while (isActive) {
                    delay(1200) // ~1.2s per step while walking
                    onStepDetected(1)
                }
            }
        }
    }

    fun addManualSteps(steps: Int) {
        onStepDetected(steps)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                // Step detector fires event with value 1.0 for each step
                val steps = event.values.firstOrNull()?.toInt() ?: 1
                if (steps > 0) {
                    onStepDetected(steps)
                }
            }
            Sensor.TYPE_STEP_COUNTER -> {
                // Step counter reports total steps since last boot
                val totalSteps = event.values.firstOrNull() ?: return
                if (lastCounterValue == null) {
                    lastCounterValue = totalSteps
                    initialCounterValue = totalSteps
                } else {
                    val delta = (totalSteps - (lastCounterValue ?: totalSteps)).toInt()
                    if (delta > 0 && delta < 1000) { // sanity check
                        onStepDetected(delta)
                    }
                    lastCounterValue = totalSteps
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no-op
    }
}
