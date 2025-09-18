package bitc.fullstack502.final_project_team1.ui.transmission

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class DataTransmissionActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spinnerSort: Spinner
    private lateinit var recyclerNotTransmittedList: RecyclerView

    // 🔹 더미 데이터 (추후 서버 연동 시 교체)
    private val dummyList = listOf(
        "부산시 남구 대연동 112-12",
        "부산시 해운대구 우동 123-45"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transmission)

        toolbar = findViewById(R.id.toolbar)
        spinnerSort = findViewById(R.id.spinnerSort)
        recyclerNotTransmittedList = findViewById(R.id.recyclerNotTransmittedList)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        setupRecyclerView()
        setupSpinner()
        setupToolbar()
        setupHeaderActions()   // 🔹 상단 로고/로그아웃 기능
    }

    private fun setupToolbar() {
        // 햄버거 클릭 → 카테고리 팝업
        toolbar.setNavigationOnClickListener { view ->
            showCategoryPopup(view)
        }
    }

    // 🔹 상단 로고/로그아웃 버튼 기능
    private fun setupHeaderActions() {
        val imgLogo = findViewById<ImageView>(R.id.imgLogo)
        val btnLogout = findViewById<TextView>(R.id.btnLogout)

        imgLogo?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnLogout?.setOnClickListener {
            AuthManager.clear(this) // 인증정보 초기화
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun setupRecyclerView() {
        val adapter = NotTransmittedListAdapter(dummyList) { address ->
            // 🔹 아이템 클릭 시 조사결과 다이얼로그 표시
            val dialog = SurveyResultDialog(this, address) {
                // 전송 완료 후 페이지 이동
                startActivity(Intent(this, TransmissionCompleteActivity::class.java))
            }
            dialog.show()
        }
        recyclerNotTransmittedList.adapter = adapter
        recyclerNotTransmittedList.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSpinner() {
        val sortOptions = arrayOf("최신순", "과거순")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = adapter

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                // TODO: 정렬 로직 추가
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ✅ 카테고리 팝업 (전송완료 페이지와 동일 스타일)
    private fun showCategoryPopup(anchor: android.view.View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.modal_category, null)

        val displayMetrics = resources.displayMetrics
        val popupWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val popupHeight = resources.getDimensionPixelSize(R.dimen.category_popup_height)

        val popupWindow = PopupWindow(
            popupView,
            popupWidth,
            popupHeight,
            true
        )

        // 닫기 버튼
        popupView.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener {
            popupWindow.dismiss()
        }

        // 메뉴 버튼들
        popupView.findViewById<MaterialButton>(R.id.btnSurveyScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyStatusActivity::class.java))
            popupWindow.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnResurveyTarget)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
            popupWindow.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            Toast.makeText(this, "조사결과 입력 페이지 준비중", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            // 현재 화면이므로 이동 없음
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            showCategoryPopup(toolbar)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}