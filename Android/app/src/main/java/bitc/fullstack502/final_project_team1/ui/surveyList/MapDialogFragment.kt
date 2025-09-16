package bitc.fullstack502.final_project_team1.ui.surveyList

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import bitc.fullstack502.final_project_team1.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource

class MapBottomSheetFragment : BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var locationSource: FusedLocationSource
    private var naverMap: NaverMap? = null
    private var lastLocation: Location? = null

    private val REQ_LOCATION = 1100

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        val content = layoutInflater.inflate(R.layout.dialog_map, null, false)
        dialog.setContentView(content)

        mapView = content.findViewById(R.id.mapView)
        // ✅ Fragment 자신을 넘겨서 권한 콜백을 이 프래그먼트가 받게 함
        locationSource = FusedLocationSource(this, REQ_LOCATION)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        content.findViewById<Button>(R.id.btnRadius1).setOnClickListener {
            startActivity(Intent(requireContext(), FullMapActivity::class.java))
            dismiss()
        }
        return dialog
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.cameraPosition = CameraPosition(LatLng(37.5666102, 126.9783881), 13.5)

        map.locationSource = locationSource
        map.uiSettings.isLocationButtonEnabled = true

        // ✅ 권한이 있으면 바로 Follow, 없으면 즉시 요청
        if (hasLocationPermission()) {
            map.locationTrackingMode = LocationTrackingMode.Follow
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQ_LOCATION
            )
            // 아직 권한 전이라도 버튼은 보이게 유지
            map.locationTrackingMode = LocationTrackingMode.NoFollow
        }

        // 첫 위치 오면 카메라 한 번 위치로 이동(시트에서 바로 보이게)
        map.addOnLocationChangeListener { loc: Location ->
            if (lastLocation == null) {
                map.cameraPosition = CameraPosition(LatLng(loc.latitude, loc.longitude), 14.5)
            }
            lastLocation = loc
        }
    }

    private fun hasLocationPermission(): Boolean {
        val c = requireContext()
        return ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // 생명주기 위임은 그대로
    override fun onStart() { super.onStart(); if (this::mapView.isInitialized) mapView.onStart() }
    override fun onResume() { super.onResume(); if (this::mapView.isInitialized) mapView.onResume() }
    override fun onPause() { if (this::mapView.isInitialized) mapView.onPause(); super.onPause() }
    override fun onStop() { if (this::mapView.isInitialized) mapView.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); if (this::mapView.isInitialized) mapView.onLowMemory() }
    override fun onDestroy() { if (this::mapView.isInitialized) mapView.onDestroy(); super.onDestroy() }

    // ✅ 권한 결과 처리: 허용 시 Follow로 전환
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (requestCode == REQ_LOCATION) {
                if (locationSource.isActivated) {
                    naverMap?.locationTrackingMode = LocationTrackingMode.Follow
                } else {
                    naverMap?.locationTrackingMode = LocationTrackingMode.None
                }
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
