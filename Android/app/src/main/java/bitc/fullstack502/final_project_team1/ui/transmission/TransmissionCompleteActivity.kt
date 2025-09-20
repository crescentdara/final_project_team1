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
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.survey.SurveyResultDialog
import bitc.fullstack502.final_project_team1.ui.survey.SurveyStatusActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import com.google.android.material.button.MaterialButton

/**
 * 📋 전송 완료 페이지
 * - 전송 완료된 조사 목록 표시
 * - 필터 기능 (전체, 결재완료, 처리중)
 * - 깔끔한 UI와 유지보수 용이한 코드 구조
 */
class TransmissionCompleteActivity : AppCompatActivity() {

    // ✅ UI 컴포넌트들
    private lateinit var spinnerFilter: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var adapter: CompletedListAdapter

    // ✅ 데이터 관련
    private var allDataList = mutableListOf<CompletedSurveyItem>()
    private var filteredDataList = mutableListOf<CompletedSurveyItem>()

    // ✅ 필터 옵션
    private val filterOptions = arrayOf("전체", "결재완료", "처리중", "반려")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transmission_complete)

        initViews()
        setupToolbar()
        setupFilter()
        setupRecyclerView()
        loadDummyData() // 🔹 추후 실제 서버 연동으로 교체
    }

    /**
     * 🎯 UI 컴포넌트 초기화
     */
    private fun initViews() {
        spinnerFilter = findViewById(R.id.spinnerFilter)
        recyclerView = findViewById(R.id.recyclerCompletedList)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        
        // 플로팅 뒤로가기 버튼
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabBack)?.setOnClickListener {
            onBackPressed()
        }
    }

    /**
     * 🔧 상단 툴바 설정
     */
    private fun setupToolbar() {
        // 햄버거 메뉴 클릭 → 카테고리 팝업
        findViewById<ImageView>(R.id.ivHamburger)?.setOnClickListener { view ->
            showCategoryPopup(view)
        }

        // 로고 클릭 → 메인으로 이동
        findViewById<ImageView>(R.id.ivLogo)?.setOnClickListener {
            navigateToMain()
        }

        // 로그아웃 클릭
        findViewById<TextView>(R.id.tvLogout)?.setOnClickListener {
            performLogout()
        }
    }

    /**
     * 🎛️ 필터 스피너 설정
     */
    private fun setupFilter() {
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterOptions
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        spinnerFilter.adapter = spinnerAdapter
        
        // 필터 선택 리스너
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilter(filterOptions[position])
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때 전체 표시
                applyFilter("전체")
            }
        }
    }

    /**
     * 📜 리사이클러뷰 설정
     */
    private fun setupRecyclerView() {
        adapter = CompletedListAdapter(filteredDataList) { item ->
            // 아이템 클릭 시 상세 다이얼로그 표시
            SurveyResultDialog(this, item.address) {
                // 다이얼로그에서 추가 액션이 필요할 때 처리
            }.show()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransmissionCompleteActivity)
            this.adapter = this@TransmissionCompleteActivity.adapter
        }
    }

    /**
     * 📊 더미 데이터 로드 (추후 서버 연동으로 교체)
     */
    private fun loadDummyData() {
        allDataList.clear()
        allDataList.addAll(
            listOf(
                CompletedSurveyItem(
                    id = 1,
                    address = "경상남도 김해시 강동 177-1",
                    completedDate = "2024-01-15 14:30",
                    status = "결재완료"
                ),
                CompletedSurveyItem(
                    id = 2,
                    address = "경상남도 김해시 강동 179-179",
                    completedDate = "2024-01-15 14:30",
                    status = "처리중"
                ),
                CompletedSurveyItem(
                    id = 3,
                    address = "부산시 남구 대연동 112-12",
                    completedDate = "2024-01-14 16:20",
                    status = "결재완료"
                ),
                CompletedSurveyItem(
                    id = 4,
                    address = "부산시 해운대구 우동 123-45",
                    completedDate = "2024-01-13 09:15",
                    status = "반려"
                )
            )
        )
        
        // 초기에는 전체 데이터 표시
        applyFilter("전체")
    }

    /**
     * 🔍 필터 적용
     */
    private fun applyFilter(filterType: String) {
        filteredDataList.clear()
        
        when (filterType) {
            "전체" -> filteredDataList.addAll(allDataList)
            "결재완료" -> filteredDataList.addAll(allDataList.filter { it.status == "결재완료" })
            "처리중" -> filteredDataList.addAll(allDataList.filter { it.status == "처리중" })
            "반려" -> filteredDataList.addAll(allDataList.filter { it.status == "반려" })
        }
        
        // UI 업데이트
        updateUI()
    }

    /**
     * 🔄 UI 업데이트 (리스트 또는 빈 상태 표시)
     */
    private fun updateUI() {
        if (filteredDataList.isEmpty()) {
            // 빈 상태 표시
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            // 리스트 표시
            recyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * 🏠 메인 화면으로 이동
     */
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * 🚪 로그아웃 처리
     */
    private fun performLogout() {
        AuthManager.clear(this)
        Toast.makeText(this, "로그아웃 완료", Toast.LENGTH_SHORT).show()
        
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    /**
     * 🍔 카테고리 팝업 표시
     */
    private fun showCategoryPopup(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.modal_category, null)

        val displayMetrics = resources.displayMetrics
        val popupWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val popupHeight = resources.getDimensionPixelSize(R.dimen.category_popup_height)

        val popup = PopupWindow(popupView, popupWidth, popupHeight, true)

        // 팝업 메뉴 클릭 이벤트들
        setupPopupMenuItems(popupView, popup)

        popup.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    /**
     * 🎛️ 팝업 메뉴 아이템들 설정
     */
    private fun setupPopupMenuItems(popupView: View, popup: PopupWindow) {
        popupView.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener { 
            popup.dismiss() 
        }

        popupView.findViewById<MaterialButton>(R.id.btnSurveyScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
            popup.dismiss()
        }
        
        popupView.findViewById<MaterialButton>(R.id.btnResurveyTarget)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
            popup.dismiss()
        }
        
        popupView.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            // 현재 화면이므로 팝업만 닫기
            popup.dismiss()
        }
        
        popupView.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
            popup.dismiss()
        }
    }

    /**
     * 📋 전송 완료된 조사 아이템 데이터 클래스
     */
    data class CompletedSurveyItem(
        val id: Long,
        val address: String,
        val completedDate: String,
        val status: String // "결재완료", "처리중", "반려"
    )
}