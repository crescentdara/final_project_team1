// ui/surveyList/BuildingInfoBottomSheet.kt
package bitc.fullstack502.final_project_team1.ui.surveyList

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.BuildingDetailDto
import bitc.fullstack502.final_project_team1.ui.SurveyActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.core.os.bundleOf

class BuildingInfoBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_BUILDING_ID   = "buildingId"
        private const val ARG_SURVEY_ID     = "surveyId"
        private const val ARG_MODE          = "mode"            // "REINSPECT" | "NEW"
        private const val ARG_ADDRESS       = "address"
        private const val ARG_BUILDING_NAME = "buildingName"
        private const val ARG_REJECT_REASON = "rejectReason"
        private const val ARG_REJECTED_AT   = "rejectedAt"

        fun newInstanceForReinspect(
            surveyId: Long,
            buildingId: Long,
            address: String?,
            buildingName: String?,
            rejectReason: String?,
            rejectedAt: String?
        ): BuildingInfoBottomSheet = BuildingInfoBottomSheet().apply {
            arguments = bundleOf(
                ARG_SURVEY_ID to surveyId,
                ARG_BUILDING_ID to buildingId,
                ARG_MODE to "REINSPECT",
                ARG_ADDRESS to address,
                ARG_BUILDING_NAME to buildingName,
                ARG_REJECT_REASON to rejectReason,
                ARG_REJECTED_AT to rejectedAt
            )
        }

        // (참고) 신규 조사 용도로도 재사용하고 싶으면:
        fun newInstanceForNew(buildingId: Long): BuildingInfoBottomSheet =
            BuildingInfoBottomSheet().apply {
                arguments = bundleOf(
                    ARG_BUILDING_ID to buildingId,
                    ARG_MODE to "NEW"
                )
            }
    }

    private var buildingId: Long = -1
    private var surveyId: Long = -1
    private var mode: String = "NEW"

    private var lotAddress: String? = null // 서버에서 받은 번지주소 캐시

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            buildingId = it.getLong(ARG_BUILDING_ID, -1)
            surveyId   = it.getLong(ARG_SURVEY_ID, -1)
            mode       = it.getString(ARG_MODE, "NEW")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottomsheet_building_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnStart = view.findViewById<Button>(R.id.btnStartSurvey)
        val info = view.findViewById<LinearLayout>(R.id.infoContainer)

        // 상단에 반려사유/반려일자 간단히 표시 (레이아웃에 TextView 두 개 추가해두면 좋음)
        val tvReason = view.findViewById<TextView?>(R.id.tvRejectReason)
        val tvDate   = view.findViewById<TextView?>(R.id.tvRejectedAt)

        tvReason?.text = arguments?.getString(ARG_REJECT_REASON)?.let { "반려사유: $it" } ?: ""
        tvDate?.text   = arguments?.getString(ARG_REJECTED_AT)?.let { "반려일자: $it" } ?: ""

        // 버튼 라벨/동작 분기
        if (mode == "REINSPECT") {
            btnStart.text = "재조사 시작"
            btnStart.setOnClickListener { startReinspectThenOpenEditor() }
        } else {
            btnStart.text = "조사 시작"
            btnStart.setOnClickListener { openEditorNew() }
        }

        // 건물 상세 불러오기
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { ApiClient.service.getBuildingDetail(buildingId) }
                .onSuccess { building ->
                    lotAddress = building.lotAddress
                    renderBuilding(info, building)
                }
                .onFailure {
                    info.addView(TextView(requireContext()).apply {
                        text = "건물 정보를 불러오지 못했습니다: ${it.message}"
                    })
                }
        }
    }

    private fun startReinspectThenOpenEditor() {
        if (surveyId <= 0) {
            Toast.makeText(requireContext(), "유효하지 않은 설문입니다.", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = AuthManager.getTokenOrThrow()
                ApiClient.service.startRedo("Bearer $token", surveyId) // TEMP 전환
                // 같은 surveyId로 수정 모드 진입
                val intent = Intent(requireContext(), SurveyActivity::class.java).apply {
                    putExtra("surveyId", surveyId)
                    putExtra("buildingId", buildingId)
                    putExtra("lotAddress", lotAddress ?: arguments?.getString(ARG_ADDRESS).orEmpty())
                }
                startActivity(intent)
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "재조사 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openEditorNew() {
        val intent = Intent(requireContext(), SurveyActivity::class.java).apply {
            putExtra("buildingId", buildingId)
            putExtra("lotAddress", lotAddress ?: arguments?.getString(ARG_ADDRESS).orEmpty())
        }
        startActivity(intent)
        dismiss()
    }

    private fun renderBuilding(container: LinearLayout, b: BuildingDetailDto) {
        container.removeAllViews()
        fun add(label: String, value: String?) {
            container.addView(TextView(requireContext()).apply {
                text = "$label : ${value ?: "-"}"
                textSize = 14f
            })
        }
        add("번지주소", b.lotAddress)
        add("도로명주소", b.roadAddress)
        add("건물명", b.buildingName)
        add("지상층수", b.groundFloors?.toString())
        add("지하층수", b.basementFloors?.toString())
        add("연면적", b.totalFloorArea?.toString())
        add("대지면적", b.landArea?.toString())
        add("주용도코드", b.mainUseCode)
        add("주용도명", b.mainUseName)
        add("기타용도", b.etcUse)
        add("구조", b.structureName)
        add("높이(m)", b.height?.toString())
    }
}
