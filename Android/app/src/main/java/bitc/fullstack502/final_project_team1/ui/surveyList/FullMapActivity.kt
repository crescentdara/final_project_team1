package bitc.fullstack502.final_project_team1.ui.surveyList

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.util.FusedLocationSource
import kotlin.math.cos

class FullMapActivity : AppCompatActivity() {

    private lateinit var locationSource: FusedLocationSource
    private var naverMap: NaverMap? = null
    private var lastLocation: Location? = null
    private var circle: CircleOverlay? = null
    private val REQ_LOCATION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullmap)

        locationSource = FusedLocationSource(this, REQ_LOCATION)

        // ✅ MapFragment 사용
        val fm = supportFragmentManager
        var mapFragment = fm.findFragmentById(R.id.map_container) as MapFragment?
        if (mapFragment == null) {
            val opts = NaverMapOptions()
                .camera(CameraPosition(LatLng(37.5666102, 126.9783881), 14.0))
            mapFragment = MapFragment.newInstance(opts)
            fm.beginTransaction().replace(R.id.map_container, mapFragment).commit()
        }

        mapFragment.getMapAsync { map: NaverMap ->
            naverMap = map
            map.locationSource = locationSource
            map.uiSettings.isLocationButtonEnabled = true
            map.locationTrackingMode = LocationTrackingMode.Follow
            map.addOnLocationChangeListener { loc: Location ->
                lastLocation = loc
            }
        }

        findViewById<Button>(R.id.btnRadius1).setOnClickListener { drawCircle(1000.0) }
        findViewById<Button>(R.id.btnRadius2).setOnClickListener { drawCircle(2000.0) }
        findViewById<Button>(R.id.btnRadius3).setOnClickListener { drawCircle(3000.0) }
    }

    private fun drawCircle(radiusMeters: Double) {
        val map = naverMap ?: return
        val center = lastLocation?.let { LatLng(it.latitude, it.longitude) }
            ?: map.cameraPosition.target

        if (circle == null) {
            circle = CircleOverlay().apply {
                this.center = center
                this.radius = radiusMeters
                color = Color.argb(48, 33, 150, 243)
                outlineColor = Color.argb(200, 33, 150, 243)
                outlineWidth = 2
                this.map = map
            }
        } else {
            circle!!.center = center
            circle!!.radius = radiusMeters
            circle!!.map = map
        }

        val bounds = circleBounds(center, radiusMeters)
        map.moveCamera(CameraUpdate.fitBounds(bounds, 64))
    }

    private fun circleBounds(center: LatLng, radiusMeters: Double): LatLngBounds {
        val degPerMeterLat = 1.0 / 111_320.0
        val degPerMeterLng = 1.0 / (111_320.0 * cos(Math.toRadians(center.latitude)))
        val dLat = radiusMeters * degPerMeterLat
        val dLng = radiusMeters * degPerMeterLng
        return LatLngBounds(
            LatLng(center.latitude - dLat, center.longitude - dLng),
            LatLng(center.latitude + dLat, center.longitude + dLng)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (requestCode == REQ_LOCATION &&
                naverMap?.locationTrackingMode == LocationTrackingMode.Follow &&
                !locationSource.isActivated
            ) {
                naverMap?.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
