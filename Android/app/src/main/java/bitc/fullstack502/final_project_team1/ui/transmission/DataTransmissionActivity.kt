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
 * 📤 데이터 전송 페이지 (미전송 내역)
 * - 미전송된 조사 목록 표시
 * - 정렬 기능 (최신순, 과거순)
 * - 깔끔한 UI와 유지보수 용이한 코드 구조
 */
class DataTransmissionActivity : AppCompatActivity() {

    // ✅ UI 컴포넌트들
    private lateinit var spinnerSort: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var adapter: NotTransmittedListAdapter

    // ✅ 데이터 관련
    private var allDataList = mutableListOf<String>()
    private var sortedDataList = mutableListOf<String>()

    // ✅ 정렬 옵션
    private val sortOptions = arrayOf("최신순", "과거순")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transmission)

        initViews()
        setupToolbar()
        setupSort()
        setupRecyclerView()
        loadDummyData() // 🔹 추후 실제 서버 연동으로 교체
    }

    /**
     * 🎯 UI 컴포넌트 초기화
     */
    private fun initViews() {
        spinnerSort = findViewById(R.id.spinnerSort)
        recyclerView = findViewById(R.id.recyclerNotTransmittedList)
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
     * 🎛️ 정렬 스피너 설정
     */
    private fun setupSort() {
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sortOptions
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        spinnerSort.adapter = spinnerAdapter
        
        // 정렬 선택 리스너
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applySorting(sortOptions[position])
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                applySorting("최신순")
            }
        }
    }

    /**
     * 📜 리사이클러뷰 설정
     */
    private fun setupRecyclerView() {
        adapter = NotTransmittedListAdapter(sortedDataList) { address ->
            // 아이템 클릭 시 조사결과 다이얼로그 표시
            val dialog = SurveyResultDialog(this, address) {
                // 전송 완료 후 전송완료 페이지로 이동
                startActivity(Intent(this, TransmissionCompleteActivity::class.java))
                finish()
            }
            dialog.show()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DataTransmissionActivity)
            this.adapter = this@DataTransmissionActivity.adapter
        }
    }

    /**
     * 📊 더미 데이터 로드 (추후 서버 연동으로 교체)
     */
    private fun loadDummyData() {
        allDataList.clear()
        allDataList.addAll(
            listOf(
                "부산시 남구 대연동 112-12",
                "부산시 해운대구 우동 123-45",
                "경상남도 김해시 강동 177-1",
                "경상남도 김해시 강동 179-179"
            )
        )
        
        // 초기에는 최신순으로 표시
        applySorting("최신순")
    }

    /**
     * 🔍 정렬 적용
     */
    private fun applySorting(sortType: String) {
        sortedDataList.clear()
        
        when (sortType) {
            "최신순" -> sortedDataList.addAll(allDataList.reversed())
            "과거순" -> sortedDataList.addAll(allDataList)
        }
        
        // UI 업데이트
        updateUI()
    }

    /**
     * 🔄 UI 업데이트 (리스트 또는 빈 상태 표시)
     */
    private fun updateUI() {
        if (sortedDataList.isEmpty()) {
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
            startActivity(Intent(this, TransmissionCompleteActivity::class.java))
            popup.dismiss()
        }
        
        popupView.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            // 현재 화면이므로 팝업만 닫기
            popup.dismiss()
        }
    }
}