// ui/surveyList/FullMapActivity.kt
package bitc.fullstack502.final_project_team1.ui.surveyList

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.AssignedBuilding
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max

class FullMapActivity : AppCompatActivity() {

    companion object {
        private const val REQ_LOCATION = 1001
        private const val EXTRA_LAT = "lat"
        private const val EXTRA_LNG = "lng"
        private const val EXTRA_ADDR = "addr"

        fun newIntent(ctx: Context, lat: Double?, lng: Double?, address: String?): Intent =
            Intent(ctx, FullMapActivity::class.java).apply {
                if (lat != null && lng != null) {
                    putExtra(EXTRA_LAT, lat)
                    putExtra(EXTRA_LNG, lng)
                    putExtra(EXTRA_ADDR, address ?: "조사지")
                }
            }
    }

    private lateinit var locationSource: FusedLocationSource
    private var naverMap: NaverMap? = null
    private var lastLocation: Location? = null

    private var surveyPicker: Marker? = null   // ✅ 조사지 피커(항상 유지)
    private var circle: CircleOverlay? = null
    private val markers = mutableListOf<Marker>() // 서버에서 내려오는 조사지 마커들(반경 조회용)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullmap)

        locationSource = FusedLocationSource(this, REQ_LOCATION)

        val mapFragment = (supportFragmentManager.findFragmentById(R.id.map_container) as MapFragment?)
            ?: MapFragment.newInstance(
                NaverMapOptions().camera(CameraPosition(LatLng(37.5666102, 126.9783881), 14.0))
            ).also {
                supportFragmentManager.beginTransaction().replace(R.id.map_container, it).commit()
            }

        mapFragment.getMapAsync { map ->
            naverMap = map
            map.locationSource = locationSource
            map.uiSettings.isLocationButtonEnabled = true
            map.locationTrackingMode = LocationTrackingMode.NoFollow
            map.locationOverlay.isVisible = true // ✅ 화면 안에 있으면 파란 현위치 점 보이도록

            map.addOnLocationChangeListener { loc -> lastLocation = loc }

            // 초기: 조사지 위치에 피커 표시(반경 누르기 전까지 그대로 유지)
            val lat = intent.getDoubleExtra(EXTRA_LAT, Double.NaN)
            val lng = intent.getDoubleExtra(EXTRA_LNG, Double.NaN)
            val addr = intent.getStringExtra(EXTRA_ADDR) ?: "조사지"
            if (!lat.isNaN() && !lng.isNaN()) {
                val pos = LatLng(lat, lng)
                surveyPicker = Marker(pos).apply {
                    captionText = addr
                    this.map = naverMap
                }
                map.moveCamera(CameraUpdate.toCameraPosition(CameraPosition(pos, 15.0)))
            }
        }

        findViewById<Button>(R.id.btnRadius1).setOnClickListener { drawRadiusFromMyLocation(1.0) }
        findViewById<Button>(R.id.btnRadius2).setOnClickListener { drawRadiusFromMyLocation(2.0) }
        findViewById<Button>(R.id.btnRadius3).setOnClickListener { drawRadiusFromMyLocation(3.0) }
    }

    /** 반경 버튼: 조사지 피커는 유지, 현위치엔 피커를 두지 않고(파란 점만), 현위치 기준 원을 그림 */
    private fun drawRadiusFromMyLocation(radiusKm: Double) {
        val map = naverMap ?: return

        val loc = lastLocation
        if (loc == null) {
            Toast.makeText(this, "현위치 파악 중… 잠시만요.", Toast.LENGTH_SHORT).show()
            return
        }

        var center = LatLng(loc.latitude, loc.longitude)
        if (c
