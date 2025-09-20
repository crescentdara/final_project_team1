package bitc.fullstack502.final_project_team1.ui.surveyList

import android.app.Dialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.os.bundleOf
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

        fun newInstance(lat: Double, lng: Double, addr: String): MapBottomSheetFragment =
            MapBottomSheetFragment().apply {
                arguments = bundleOf(
                    ARG_LAT to lat,
                    ARG_LNG to lng,
                    ARG_ADDR to addr
                )
            }
    }

    private lateinit var mapView: MapView
    private lateinit var locationSource: FusedLocationSource
    private var naverMap: NaverMap? = null
    private var lastLocation: Location? = null
    private val REQ_LOCATION = 1100

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // theme 프로퍼티 인식 이슈를 피하려면 style을 직접 지정해도 됩니다.
        val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)

        val content = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_map, null, false)
        dialog.setContentView(content)

        mapView = content.findViewById(R.id.mapView)
        locationSource = FusedLocationSource(this, REQ_LOCATION)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        content.findViewById<Button>(R.id.btnRadius1).setOnClickListener {
            val args = requireArguments()
            val intent: Intent = FullMapActivity.newIntent(
                /* context = */ requireContext(),
                lat = args.getDouble(ARG_LAT),
                lng = args.getDouble(ARG_LNG),
                address = args.getString(ARG_ADDR)
            )
            startActivity(intent)
            dismiss()
        }
        return dialog
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.locationSource = locationSource
        map.uiSettings.isLocationButtonEnabled = true

        val args = requireArguments()
        val hasLat = args.containsKey(ARG_LAT)
        val hasLng = args.containsKey(ARG_LNG)

        if (hasLat && hasLng) {
            val lat = args.getDouble(ARG_LAT)
            val lng = args.getDouble(ARG_LNG)
            val addr = args.getString(ARG_ADDR) ?: "조사지"

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
