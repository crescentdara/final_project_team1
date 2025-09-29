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

class BuildingInfoBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_BUILDING_ID = "buildingId"
        private const val ARG_SURVEY_ID = "surveyId"
        private const val ARG_MODE = "mode"          // "REINSPECT" | "NEW" | "TEMP_DETAIL"
        private const val ARG_ADDRESS = "address"
        private const val ARG_BUILDING_NAME = "buildingName"
        private const val ARG_REJECT_REASON = "rejectReason"
        private const val ARG_REJECTED_AT = "rejectedAt"

        @JvmStatic
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

        @JvmStatic
        fun newInstanceForNew(buildingId: Long): BuildingInfoBottomSheet =
            BuildingInfoBottomSheet().apply {
                arguments = bundleOf(
                    ARG_BUILDING_ID to buildingId,
                    ARG_MODE to "NEW"
                )
            }

        @JvmStatic
        fun newInstanceForTempDetail(
            surveyId: Long,
            buildingId: Long,
            address: String
        ): BuildingInfoBottomSheet = BuildingInfoBottomSheet().apply {
            arguments = bundleOf(
                ARG_MODE to "TEMP_DETAIL",
                ARG_SURVEY_ID to surveyId,
                ARG_BUILDING_ID to buildingId,
                ARG_ADDRESS to address
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
            surveyId = it.getLong(ARG_SURVEY_ID, -1)
            mode = it.getString(ARG_MODE, "NEW")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_building_info, container, false)

    /** 바텀시트에 전달된 returnTo가 있으면 그대로, 없으면 mode별 기본값으로 */
    private fun resolveReturnTo(): String {
        val arg = arguments?.getString(SurveyActivity.EXTRA_RETURN_TO)
        if (!arg.isNullOrBlank()) return arg
        return when (mode) {
            "REINSPECT" -> SurveyActivity.RETURN_REINSPECT       // 재조사 목록으로
            "TEMP_DETAIL" -> SurveyActivity.RETURN_NOT_TRANSMITTED  // 미전송으로
            else -> SurveyActivity.RETURN_SURVEY_LIST      // 기본: 조사목록
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnStart  = view.findViewById<Button>(R.id.btnStartSurvey)
        val info      = view.findViewById<LinearLayout>(R.id.infoContainer)
        val tempBar   = view.findViewById<LinearLayout>(R.id.layoutTempActions)
        val btnEdit   = view.findViewById<Button>(R.id.btnEditResult)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitResult)
        val btnReject = view.findViewById<Button>(R.id.btnRejectSurvey)

        // ✅ 어디서 열렸는지 판단 (명시값 없으면 mode 기반 기본값)
        val returnTo = arguments?.getString(SurveyActivity.EXTRA_RETURN_TO) ?: resolveReturnTo()

        // ✅ 재조사/미전송에서 열리면 '조사 거절' 숨김
        val shouldHideReject =
            (mode == "REINSPECT" || mode == "TEMP_DETAIL") ||
                    (returnTo == SurveyActivity.RETURN_REINSPECT || returnTo == SurveyActivity.RETURN_NOT_TRANSMITTED)

        if (shouldHideReject) {
            btnReject.visibility = View.GONE
        } else {
            btnReject.visibility = View.VISIBLE
            // ▼ 보일 때만 리스너 연결
            btnReject.setOnClickListener { v ->
                if (buildingId <= 0) {
                    Toast.makeText(requireContext(), "잘못된 건물 ID입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val uid = AuthManager.userId(requireContext())
                if (uid <= 0) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                v.isEnabled = false
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching { ApiClient.service.rejectAssignment(uid, buildingId) }
                        .onSuccess { resp ->
                            if (resp.isSuccessful) {
                                Toast.makeText(requireContext(), "조사를 거절했습니다.", Toast.LENGTH_SHORT).show()
                                dismiss()
                                (activity as? SurveyListActivity)?.refreshAssignments()
                            } else {
                                Toast.makeText(requireContext(), "거절 실패: ${resp.code()}", Toast.LENGTH_SHORT).show()
                                v.isEnabled = true
                            }
                        }
                        .onFailure { e ->
                            Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                            v.isEnabled = true
                        }
                }
            }
        }


    // 반려 정보: 기존 로직 유지
        view.findViewById<TextView>(R.id.tvRejectReason)
            .setTextOrGone(arguments?.getString(ARG_REJECT_REASON), R.string.reject_reason_fmt)

        when (mode) {
            "REINSPECT" -> {
                tempBar.visibility = View.GONE
                btnStart.visibility = View.VISIBLE
                btnStart.text = getString(R.string.reinspect_start)
                btnStart.setOnClickListener { startReinspectThenOpenEditor() }
                fetchAndRenderBuilding(info)
            }

            "NEW" -> {
                tempBar.visibility = View.GONE
                btnStart.visibility = View.VISIBLE
                btnStart.text = getString(R.string.survey_start)
                btnStart.setOnClickListener { openEditorNew() }
                fetchAndRenderBuilding(info)
            }

            "TEMP_DETAIL" -> {
                tempBar.visibility = View.VISIBLE
                btnStart.visibility = View.GONE

//                // ✅ 조사 거절 버튼 동작
//                btnReject.setOnClickListener {
//                    if (buildingId <= 0) {
//                        Toast.makeText(requireContext(), "잘못된 건물 ID입니다.", Toast.LENGTH_SHORT).show()
//                        return@setOnClickListener
//                    }
//
//                    viewLifecycleOwner.lifecycleScope.launch {
//                        runCatching { ApiClient.service.rejectAssignment(buildingId) }
//                            .onSuccess { resp ->
//                                if (resp.isSuccessful) {
//                                    Toast.makeText(
//                                        requireContext(),
//                                        "조사를 거절했습니다.",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    dismiss()
//                                    // ✅ 부모 액티비티에 리스트 새로고침 신호 보내기
//                                    (activity as? SurveyListActivity)?.refreshAssignments()
//                                } else {
//                                    Toast.makeText(
//                                        requireContext(),
//                                        "거절 실패: ${resp.code()}",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                            .onFailure {
//                                Toast.makeText(
//                                    requireContext(),
//                                    "에러: ${it.message}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                    }
//                }


                // 기존 조사 시작 버튼 분기
                if (mode == "REINSPECT") {
                    btnStart.text = getString(R.string.reinspect_start)
                    btnStart.setOnClickListener { startReinspectThenOpenEditor() }
                } else {
                    btnStart.text = getString(R.string.survey_start)
                    btnStart.setOnClickListener { openEditorNew() }
                }

                // 주소 헤더
                info.removeAllViews()
                info.addView(TextView(requireContext()).apply {
                    text = (arguments?.getString(ARG_ADDRESS) ?: "-")
                    setTextColor(resources.getColor(R.color.text_primary, null))
                    textSize = 16f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setPadding(0, 0, 0, 8)
                })

                fetchAndRenderSurveyResult(info)

                btnEdit.setOnClickListener {
                    openEditorForEdit(autoSubmit = false)
                    dismiss()
                }
                btnSubmit.setOnClickListener {
                    openEditorForEdit(autoSubmit = true)
                    dismiss()
                }
            }
        }
    }

    private fun fetchAndRenderBuilding(container: LinearLayout) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { ApiClient.service.getBuildingDetail(buildingId) }
                .onSuccess { b ->
                    lotAddress = b.lotAddress
                    container.removeAllViews()
                    fun add(label: String, value: String?) {
                        container.addView(TextView(requireContext()).apply {
                            text = "$label : ${value ?: "-"}"
                            textSize = 14f
                            setPadding(0, 6, 0, 6)
                        })
                    }
                    add("지번주소", b.lotAddress)
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
                .onFailure {
                    container.addView(TextView(requireContext()).apply {
                        text = getString(R.string.building_load_failed_fmt, it.message ?: "")
                    })
                }
        }
    }

    private fun fetchAndRenderSurveyResult(container: LinearLayout) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val uid = AuthManager.userId(requireContext())
                ApiClient.service.getSurveyDetail(uid, surveyId)
            }.onSuccess { d ->
                fun add(label: String, value: String?) {
                    container.addView(TextView(requireContext()).apply {
                        text = "$label : ${value ?: "-"}"
                        textSize = 14f
                        setPadding(0, 6, 0, 6)
                    })
                }

                fun opt(i: Int?, vararg labels: String) =
                    i?.takeIf { it in 1..labels.size }?.let { labels[it - 1] }

                add("조사불가", if (d.possible == 2) "불가" else "가능")
                add("행정목적", opt(d.adminUse, "활용", "일부 활용", "미활용"))
                add("유휴비율", opt(d.idleRate, "0~10%", "10~30%", "30~50%", "50% 이상"))
                add("안전등급", opt(d.safety, "A", "B", "C", "D", "E"))
                add("외벽", opt(d.wall, "양호", "보통", "불량"))
                add("옥상", opt(d.roof, "양호", "보통", "불량"))
                add("창호", opt(d.windowState, "양호", "보통", "불량"))
                add("주차", opt(d.parking, "가능", "불가"))
                add("현관", opt(d.entrance, "양호", "보통", "불량"))
                add("천장", opt(d.ceiling, "양호", "보통", "불량"))
                add("바닥", opt(d.floor, "양호", "보통", "불량"))
                add("외부 기타", d.extEtc)
                add("내부 기타", d.intEtc)
            }.onFailure {
                container.addView(TextView(requireContext()).apply {
                    text = "조사결과 로드 실패: ${it.message}"
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
            putExtra(SurveyActivity.EXTRA_RETURN_TO, resolveReturnTo())
            putExtra("lotAddress", lotAddress ?: arguments?.getString(ARG_ADDRESS).orEmpty())
        }
        startActivity(intent)
        dismiss()
    }

    private fun TextView.setTextOrGone(textRaw: String?, prefixRes: Int? = null) {
        val t = textRaw?.trim().orEmpty()
        if (t.isEmpty()) {
            text = ""
            visibility = View.GONE
        } else {
            text = if (prefixRes != null) getString(prefixRes, t) else t
            visibility = View.VISIBLE
        }
    }

    private fun openEditorNew() {
        val i = Intent(requireContext(), SurveyActivity::class.java).apply {
            putExtra(SurveyActivity.EXTRA_MODE, "CREATE")
            putExtra(SurveyActivity.EXTRA_BUILDING_ID, buildingId)
            putExtra(SurveyActivity.EXTRA_RETURN_TO, resolveReturnTo())
            putExtra("lotAddress", lotAddress ?: arguments?.getString(ARG_ADDRESS).orEmpty())
        }
        startActivity(i)
        dismiss()
    }

    private fun openEditorForEdit(autoSubmit: Boolean) {
        val i = Intent(requireContext(), SurveyActivity::class.java).apply {
            putExtra(SurveyActivity.EXTRA_MODE, "EDIT")
            putExtra(SurveyActivity.EXTRA_BUILDING_ID, buildingId)
            putExtra(SurveyActivity.EXTRA_SURVEY_ID, surveyId)
            putExtra(SurveyActivity.EXTRA_AUTO_SUBMIT, autoSubmit)
            putExtra(SurveyActivity.EXTRA_RETURN_TO, resolveReturnTo())
            putExtra("lotAddress", arguments?.getString(ARG_ADDRESS).orEmpty())
        }
        startActivity(i)
    }

    private fun renderBuilding(container: LinearLayout, b: BuildingDetailDto) {
        container.removeAllViews()
        fun add(label: String, value: String?) {
            container.addView(TextView(requireContext()).apply {
                text = "$label : ${value ?: "-"}"
                textSize = 14f
            })
        }
        add("지번주소", b.lotAddress)
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
