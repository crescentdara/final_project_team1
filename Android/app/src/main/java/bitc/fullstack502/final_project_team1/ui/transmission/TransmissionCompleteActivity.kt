package bitc.fullstack502.final_project_team1.ui.transmission

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.final_project_team1.MainActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.SurveyResultResponse
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.survey.SurveyResultDialog
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 📋 조사내역 조회 페이지
 * - 서버에서 상태별(SENT/APPROVED) 조사 목록 조회
 * - 필터: 전체(null), 결재완료(APPROVED), 처리중(SENT)
 */
class TransmissionCompleteActivity : AppCompatActivity() {

    // UI
    private lateinit var spinnerFilter: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var adapter: CompletedListAdapter

    // 데이터
    private val allDataList = mutableListOf<CompletedSurveyItem>()
    private val filteredDataList = mutableListOf<CompletedSurveyItem>()

    // ✅ 반려 제거
    private val filterOptions = arrayOf("전체", "결재완료", "처리중")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transmission_complete)

        initViews()
        setupToolbar()
        setupFilter()
        setupRecyclerView()

        // 최초 로드: "전체"
        applyFilter("전체")
    }

    private fun initViews() {
        spinnerFilter = findViewById(R.id.spinnerFilter)
        recyclerView = findViewById(R.id.recyclerCompletedList)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabBack)
            ?.setOnClickListener { onBackPressed() }
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.ivHamburger)?.setOnClickListener { view ->
            showCategoryPopup(view)
        }
        findViewById<ImageView>(R.id.ivLogo)?.setOnClickListener { navigateToMain() }
        findViewById<TextView>(R.id.tvLogout)?.setOnClickListener { performLogout() }
    }

    private fun setupFilter() {
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterOptions
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerFilter.adapter = spinnerAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilter(filterOptions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                applyFilter("전체")
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CompletedListAdapter(filteredDataList) { item ->
            SurveyResultDialog(this, item.address) { /* 필요시 추가 액션 */ }.show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /** 필터 적용 + 서버 재조회 */
    private fun applyFilter(filterType: String) {
        val statusCode: String? = when (filterType) {
            "결재완료" -> "APPROVED"
            "처리중"   -> "SENT"
            else       -> null // 전체
        }
        loadFromServer(statusCode, filterType)
    }

    /** 서버에서 상태별 목록 조회 */
    private fun loadFromServer(statusCode: String?, filterLabel: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val uid = AuthManager.userId(this@TransmissionCompleteActivity)
                val resp = ApiClient.service.getSurveyResults(
                    userId = uid,
                    status = statusCode,
                    page = 0,
                    size = 50
                )

                if (!resp.isSuccessful) {
                    Toast.makeText(this@TransmissionCompleteActivity, "서버 오류: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val pageData = resp.body()
                val items: List<SurveyResultResponse> = pageData?.content ?: emptyList()

                // 화면용 데이터로 매핑
                allDataList.clear()
                allDataList.addAll(
                    items.map {
                        CompletedSurveyItem(
                            id = it.id,
                            address = it.buildingAddress ?: "(주소 없음)",
                            completedDate = it.updatedAt ?: "",
                            status = when (it.status) {
                                "APPROVED" -> "결재완료"
                                "SENT"     -> "처리중"
                                else       -> "기타"
                            }
                        )
                    }
                )

                // 선택된 필터 라벨에 맞춰 리스트 구성
                filteredDataList.clear()
                when (filterLabel) {
                    "전체"     -> filteredDataList.addAll(allDataList)
                    "결재완료" -> filteredDataList.addAll(allDataList.filter { it.status == "결재완료" })
                    "처리중"   -> filteredDataList.addAll(allDataList.filter { it.status == "처리중" })
                }
                updateUI()
            } catch (e: Exception) {
                Toast.makeText(this@TransmissionCompleteActivity, "목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        if (filteredDataList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun performLogout() {
        AuthManager.clear(this)
        Toast.makeText(this, "로그아웃 완료", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun showCategoryPopup(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.modal_category, null)
        val displayMetrics = resources.displayMetrics
        val popupWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val popupHeight = resources.getDimensionPixelSize(R.dimen.category_popup_height)
        val popup = PopupWindow(popupView, popupWidth, popupHeight, true)
        setupPopupMenuItems(popupView, popup)
        popup.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    private fun setupPopupMenuItems(popupView: View, popup: PopupWindow) {
        popupView.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener { popup.dismiss() }
        popupView.findViewById<MaterialButton>(R.id.btnSurveyScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java)); popup.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnResurveyTarget)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java)); popup.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            popup.dismiss() // 현재 화면
        }
        popupView.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java)); popup.dismiss()
        }
    }

    /** 조사내역 아이템 */
    data class CompletedSurveyItem(
        val id: Long,
        val address: String,
        val completedDate: String,
        val status: String // "결재완료", "처리중"
    )
}
