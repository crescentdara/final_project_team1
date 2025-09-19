package bitc.fullstack502.final_project_team1.ui.surveyList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.BuildingDetailDto
import bitc.fullstack502.final_project_team1.ui.transmission.SurveyActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BuildingInfoBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_BUILDING_ID = "buildingId"

        fun newInstance(buildingId: Long): BuildingInfoBottomSheet {
            val f = BuildingInfoBottomSheet()
            f.arguments = Bundle().apply { putLong(ARG_BUILDING_ID, buildingId) }
            return f
        }
    }

    private var buildingId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildingId = arguments?.getLong(ARG_BUILDING_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottomsheet_building_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnStart = view.findViewById<Button>(R.id.btnStartSurvey)
        val infoContainer = view.findViewById<LinearLayout>(R.id.infoContainer)

        // 🔹 SurveyActivity로 이동
        btnStart.setOnClickListener {
            val intent = Intent(requireContext(), SurveyActivity::class.java).apply {
                putExtra("buildingId", buildingId)
            }
            startActivity(intent)
            dismiss() // 바텀시트 닫기
        }

        // 🔹 건물 정보 불러오기
        CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                ApiClient.service.getBuildingDetail(buildingId)
            }.onSuccess { building ->
                showBuildingInfo(infoContainer, building)
            }.onFailure {
                val tv = TextView(requireContext()).apply {
                    text = "건물 정보를 불러오지 못했습니다: ${it.message}"
                }
                infoContainer.addView(tv)
            }
        }
    }

    private fun showBuildingInfo(container: LinearLayout, building: BuildingDetailDto) {
        container.removeAllViews()
        val fields = listOf(
            "번지주소" to (building.lotAddress ?: "-"),
            "도로명주소" to (building.roadAddress ?: "-"),
            "건물명" to (building.buildingName ?: "-"),
            "지상층수" to (building.groundFloors?.toString() ?: "-"),
            "지하층수" to (building.basementFloors?.toString() ?: "-"),
            "연면적" to (building.totalFloorArea?.toString() ?: "-"),
            "대지면적" to (building.landArea?.toString() ?: "-"),
            "주용도코드" to (building.mainUseCode ?: "-"),
            "주용도명" to (building.mainUseName ?: "-"),
            "기타용도" to (building.etcUse ?: "-"),
            "구조" to (building.structureName ?: "-"),
            "높이(m)" to (building.height?.toString() ?: "-")
        )

        fields.forEach { (label, value) ->
            val tv = TextView(requireContext()).apply {
                text = "$label : $value"
                textSize = 14f
            }
            container.addView(tv)
        }
    }
}
