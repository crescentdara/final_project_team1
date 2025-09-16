package bitc.fullstack502.final_project_team1

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import bitc.fullstack502.final_project_team1.ui.FullMapActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MapBottomSheetFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        val content = layoutInflater.inflate(R.layout.dialog_map, null, false)
        dialog.setContentView(content)

        mapView = content.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { naverMap ->
            // 카메라 기본 위치
            naverMap.cameraPosition = CameraPosition(LatLng(37.5666102, 126.9783881), 13.5)
        }

            // ✅ "지도 전체화면" 버튼 → FullMapActivity 로 이동
            content.findViewById<Button>(R.id.btnRadius1).setOnClickListener {
                // 필요하면 현재 지도 중심/필터 값 등을 putExtra 로 넘겨주세요.
                val intent = Intent(requireContext(), FullMapActivity::class.java).apply {
                    // putExtra("centerLat", 37.5665)
                    // putExtra("centerLng", 126.9780)
                }
                startActivity(intent)
                dismiss() // 바텀시트 닫기
            }

        return dialog
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onDestroyView() { mapView.onDestroy(); super.onDestroyView() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}
