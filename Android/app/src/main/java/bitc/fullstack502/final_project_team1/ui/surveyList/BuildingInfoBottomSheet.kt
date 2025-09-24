package bitc.fullstack502.final_project_team1.ui.surveyList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.BuildingDetailDto
import bitc.fullstack502.final_project_team1.ui.SurveyActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

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

    private var lotAddress: String? = null

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
        val btnReject = view.findViewById<Button>(R.id.btnRejectSurvey) // ✅ 새로 추가
        val info = view.findViewById<LinearLayout>(R.id.infoContainer)

        // ✅ 조사 거절 버튼 동작
        btnReject.setOnClickListener {
            if (buildingId <= 0) {
                Toast.makeText(requireContext(), "잘못된 건물 ID입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                runCatching { ApiClient.service.rejectAssignment(buildingId) }
                    .onSuccess { resp ->
                        if (resp.isSuccessful) {
                            Toast.makeText(requireContext(), "조사를 거절했습니다.", Toast.LENGTH_SHORT).show()
                            dismiss()
                            // ✅ 부모 액티비티에 리스트 새로고침 신호 보내기
                            (activity as? SurveyListActivity)?.refreshAssignments()
                        } else {
                            Toast.makeText(requireContext(), "거절 실패: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .onFailure {
                        Toast.makeText(requireContext(), "에러: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // 기존 조사 시작 버튼 분기
        if (mode == "REINSPECT") {
            btnStart.text = getString(R.string.reinspect_start)
            btnStart.setOnClickListener { startReinspectThenOpenEditor() }
        } else {
            btnStart.text = getString(R.string.survey_start)
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
                        text = getString(R.string.building_load_failed_fmt, it.message ?: "")
                    })
                }
        }
    }

    private fun startReinspectThenOpenEditor() {
        if (buildingId <= 0 || surveyId <= 0) {
            Toast.makeText(requireContext(), R.string.invalid_survey, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(requireContext(), SurveyActivity::class.java).apply {
            putExtra(SurveyActivity.EXTRA_MODE, "REINSPECT")
            putExtra(SurveyActivity.EXTRA_BUILDING_ID, buildingId)
            putExtra(SurveyActivity.EXTRA_SURVEY_ID, surveyId)
            putExtra("lotAddress", lotAddress ?: arguments?.getString(ARG_ADDRESS).orEmpty())
        }
        startActivity(intent)
        dismiss()
    }

    private fun openEditorNew() {
        val intent = Intent(requireContext(), SurveyActivity::class.java).apply {
            putExtra(SurveyActivity.EXTRA_MODE, "CREATE")
            putExtra(SurveyActivity.EXTRA_BUILDING_ID, buildingId)
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
        add("지번주소", b.lotAddress)
    }
}
