// ui/surveyList/BuildingInfoBottomSheet.kt
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
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.BuildingDetailDto
import bitc.fullstack502.final_project_team1.ui.SurveyActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import retrofit2.HttpException


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
<<<<<<< HEAD
    private var surveyId: Long = -1
    private var mode: String = "NEW"

    private var lotAddress: String? = null // 서버에서 받은 번지주소 캐시
=======
    private var lotAddress: String? = null   // ✅ 인텐트로 넘길 주소 캐시
>>>>>>> origin/app/hsm/ResultDign

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

<<<<<<< HEAD
        // (선택) 반려정보 영역이 레이아웃에 있으면 채워줌(없어도 에러 안 남)
        view.findViewById<TextView?>(R.id.tvRejectReason)?.let { tv ->
            val reason = arguments?.getString(ARG_REJECT_REASON).orEmpty()
            if (reason.isNotBlank()) tv.text = getString(R.string.reject_reason_fmt, reason)
        }
        view.findViewById<TextView?>(R.id.tvRejectedAt)?.let { tv ->
            val dt = arguments?.getString(ARG_REJECTED_AT).orEmpty()
            if (dt.isNotBlank()) tv.text = getString(R.string.rejected_at_fmt, dt)
        }

        // 버튼 라벨/동작 분기
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
=======
        // 🔹 조사 시작 → SurveyActivity로 이동 (ID + 주소 같이 전달)
        btnStart.setOnClickListener {
            val intent = Intent(requireContext(), SurveyActivity::class.java).apply {
                putExtra("buildingId", buildingId)
                putExtra("lotAddress", lotAddress ?: "") // ✅ 주소 전달 (없으면 빈 문자열)
            }
            startActivity(intent)
            dismiss()
        }

        // 🔹 건물 정보 불러오기
        CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                ApiClient.service.getBuildingDetail(buildingId)
            }.onSuccess { building ->
                // ✅ 주소 캐시 (인텐트에서 사용)
                lotAddress = building.lotAddress
                showBuildingInfo(infoContainer, building)
            }.onFailure {
                val tv = TextView(requireContext()).apply {
                    text = "건물 정보를 불러오지 못했습니다: ${it.message}"
>>>>>>> origin/app/hsm/ResultDign
                }
                .onFailure {
                    info.addView(TextView(requireContext()).apply {
                        text = getString(R.string.building_load_failed_fmt, it.message ?: "")
                    })
                }
        }
    }

    private fun startReinspectThenOpenEditor() {
        if (surveyId <= 0) {
            Toast.makeText(requireContext(), R.string.invalid_survey, Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1) 토큰 말고 uid 사용
                val uid = AuthManager.userId(requireContext())
                if (uid <= 0) {
                    Toast.makeText(requireContext(), R.string.login_required, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2) 헤더: X-USER-ID(Int)
                ApiClient.service.startRedo(uid, surveyId)

                // 3) 성공 → 에디터 진입
                val intent = Intent(requireContext(), SurveyActivity::class.java).apply {
                    putExtra("surveyId", surveyId)
                    putExtra("buildingId", buildingId)
                    putExtra("lotAddress", lotAddress ?: arguments?.getString(ARG_ADDRESS).orEmpty())
                }
                startActivity(intent)
                dismiss()
            } catch (e: Exception) {
                val msg = if (e is retrofit2.HttpException) {
                    "HTTP ${e.code()}"
                } else e.message ?: ""
                Toast.makeText(
                    requireContext(),
                    getString(R.string.reinspect_start_failed_fmt, msg),
                    Toast.LENGTH_SHORT
                ).show()
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
