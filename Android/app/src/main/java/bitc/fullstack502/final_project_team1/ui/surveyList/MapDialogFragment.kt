package bitc.fullstack502.final_project_team1.ui.surveyList

import android.app.Dialog
import android.location.Location
import android.os.Bundle
import android.widget.Button
import bitc.fullstack502.final_project_team1.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource

class MapBottomSheetFragment : BottomSheetDialogFragment(), OnMapReadyCallback {

    companion object {
        private const val ARG_LAT = "lat"
        private const val ARG_LNG = "lng"
        private const val ARG_ADDR = "addr"

        fun newInstance(lat: Double, lng: Double, addr: String) = MapBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putDouble(ARG_LAT, lat); putDouble(ARG_LNG, lng); putString(ARG_ADDR, addr)
            }
        }
    }

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
        locationSource = FusedLocationSource(this, REQ_LOCATION)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        content.findViewById<Button>(R.id.btnRadius1).setOnClickListener {
            // 전체화면 맵으로 넘어가서 반경 필터 사용
            startActivity(FullMapActivity.newIntent(requireContext(),
                lat = arguments?.getDouble(ARG_LAT),
                lng = arguments?.getDouble(ARG_LNG),
                address = arguments?.getString(ARG_ADDR)
            ))
            dismiss()
        }
        return dialog
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.locationSource = locationSource
        map.uiSettings.isLocationButtonEnabled = true

        // 타겟 마커 표시
        val lat = arguments?.getDouble(ARG_LAT)
        val lng = arguments?.getDouble(ARG_LNG)
        val addr = arguments?.getString(ARG_ADDR) ?: "조사지"

        if (lat != null && lng != null) {
            val pos = LatLng(lat, lng)
            Marker(pos).apply {
                captionText = addr
                this.map = map
            }
            map.moveCamera(CameraUpdate.toCameraPosition(CameraPosition(pos, 15.0)))
        } else {
            map.cameraPosition = CameraPosition(LatLng(37.5666102, 126.9783881), 13.5)
        }
    }

    // 생명주기 위임
    override fun onStart() { super.onStart(); if (this::mapView.isInitialized) mapView.onStart() }
    override fun onResume() { super.onResume(); if (this::mapView.isInitialized) mapView.onResume() }
    override fun onPause() { if (this::mapView.isInitialized) mapView.onPause(); super.onPause() }
    override fun onStop() { if (this::mapView.isInitialized) mapView.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); if (this::mapView.isInitialized) mapView.onLowMemory() }
    override fun onDestroy() { if (this::mapView.isInitialized) mapView.onDestroy(); super.onDestroy() }
}
