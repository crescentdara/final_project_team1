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
import bitc.fullstack502.final_project_team1.ui.BaseActivity
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 📋 조사내역 조회 페이지
 * - 서버에서 상태별(결재완료/결재대기) 조사 목록 조회
 * - 필터: 전체(null), 결재완료(APPROVED), 결재대기(SENT)
 */
class TransmissionCompleteActivity : BaseActivity() {

    override fun bottomNavSelectedItemId() = R.id.nav_history

    // UI
    private lateinit var spinnerFilter: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var adapter: CompletedListAdapter

    private lateinit var tvTotalCount: TextView


    // 데이터
    private val allDataList = mutableListOf<CompletedSurveyItem>()
    private val filteredDataList = mutableListOf<CompletedSurveyItem>()

    // ✅ 옵션 수정 (기타 제거, 처리중 → 결재대기)
    private val filterOptions = arrayOf("전체", "결재완료", "결재대기")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transmission_complete)
        initHeader(title = "전송 내역")

        initViews()
        setupFilter()
        setupRecyclerView()

        // 최초 로드: "전체"
        applyFilter("전체")
    }

    private fun initViews() {
        spinnerFilter = findViewById(R.id.spinnerFilter)
        recyclerView = findViewById(R.id.recyclerCompletedList)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        tvTotalCount = findViewById(R.id.tvTotalCount)
        findViewById<FloatingActionButton>(R.id.fabBack)?.setOnClickListener {
            navigateHomeOrFinish()
        }

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
//            SurveyResultDialog(this, item.address) { /* 필요시 추가 액션 */ }.show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /** 필터 적용 + 서버 재조회 */
    private fun applyFilter(filterType: String) {
        val statusCode: String? = when (filterType) {
            "결재완료" -> "APPROVED"
            "결재대기" -> "SENT"
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

                // 화면용 데이터로 매핑 (APPROVED/SENT만)
                allDataList.clear()
                allDataList.addAll(
                    items.mapNotNull {
                        val formattedDate = formatDateOnly(it.updatedAt ?: it.createdAt)
                        when (it.status) {
                            "APPROVED" -> CompletedSurveyItem(
                                id = it.surveyId,
                                address = it.buildingAddress ?: "(주소 없음)",
                                completedDate = formattedDate,
                                status = "결재완료"
                            )
                            "SENT" -> CompletedSurveyItem(
                                id = it.surveyId,
                                address = it.buildingAddress ?: "(주소 없음)",
                                completedDate = formattedDate,
                                status = "결재대기"
                            )
                            else -> null // ✅ 기타 상태는 무시
                        }
                    }
                )

                // 선택된 필터 라벨에 맞춰 리스트 구성
                filteredDataList.clear()
                when (filterLabel) {
                    "전체"     -> filteredDataList.addAll(allDataList)
                    "결재완료" -> filteredDataList.addAll(allDataList.filter { it.status == "결재완료" })
                    "결재대기" -> filteredDataList.addAll(allDataList.filter { it.status == "결재대기" })
                }
                updateUI()
            } catch (e: Exception) {
                Toast.makeText(this@TransmissionCompleteActivity, "목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** yyyy-MM-dd 로 변환 */
    private fun formatDateOnly(datetime: String?): String {
        if (datetime.isNullOrBlank()) return ""
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val parsed = LocalDateTime.parse(datetime, inputFormatter)
            parsed.format(outputFormatter)
        } catch (e: Exception) {
            datetime.take(10) // 최소 yyyy-MM-dd 까지만
        }
    }

    private fun updateUI() {

        tvTotalCount.text = "총 ${filteredDataList.size}건"

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

    /** 조사내역 아이템 */
    data class CompletedSurveyItem(
        val id: Long,
        val address: String,
        val completedDate: String,
        val status: String // "결재완료", "결재대기"
    )
}
