package bitc.fullstack502.final_project_team1.ui.surveyList

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import bitc.fullstack502.final_project_team1.R
import com.google.android.gms.location.LocationServices

class ArActivity : ComponentActivity(), SensorEventListener {

    companion object { const val EXTRA_SITES = "extra_sites" }

    private lateinit var previewView: PreviewView
    private lateinit var overlay: AROverlayView

    private lateinit var sensorManager: SensorManager
    private val rotation = FloatArray(9)
    private val orient = FloatArray(3)

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val handler = Handler(Looper.getMainLooper())

    private val perm = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { startCameraAndSensors() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        previewView = findViewById(R.id.previewView)
        overlay = findViewById(R.id.arOverlay)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        overlay.sites = intent.getParcelableArrayListExtra(EXTRA_SITES) ?: arrayListOf()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val needs = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            needs += Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            needs += Manifest.permission.ACCESS_FINE_LOCATION

        if (needs.isNotEmpty()) perm.launch(needs.toTypedArray()) else startCameraAndSensors()
    }

    private fun startCameraAndSensors() {
        // 카메라 프리뷰 시작
        val fut = ProcessCameraProvider.getInstance(this)
        fut.addListener({
            val provider = fut.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
        }, ContextCompat.getMainExecutor(this))

        try { fused.lastLocation.addOnSuccessListener { it?.let(::updateLoc) } } catch (_: SecurityException) {}

        if (isEmulator()) {
            handler.post(object : Runnable {
                override fun run() {
                    overlay.updatePose(90f, 0f)
                    handler.postDelayed(this, 1000L)
                }
            })
        } else {
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.also {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    private fun updateLoc(loc: Location) { overlay.updateUserLocation(loc) }

    override fun onSensorChanged(event: android.hardware.SensorEvent?) {
        if (!isEmulator() && event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotation, event.values)
            SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotation)
            SensorManager.getOrientation(rotation, orient)
            val az = Math.toDegrees(orient[0].toDouble()).toFloat().let { if (it < 0f) it + 360f else it }
            val pitch = Math.toDegrees(orient[1].toDouble()).toFloat()

            overlay.updatePose(az, pitch)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onPause() {
        super.onPause()
        if (!isEmulator()) sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        try { fused.lastLocation.addOnSuccessListener { it?.let(::updateLoc) } } catch (_: SecurityException) {}
    }

    private fun isEmulator(): Boolean =
        Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("google_sdk") ||
                Build.MODEL.lowercase().contains("emulator") || Build.BRAND.startsWith("generic")
}
