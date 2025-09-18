package bitc.fullstack502.final_project_team1.ui.transmission

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class TransmissionCompleteActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spinnerFilter: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CompletedListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transmission_complete)

        initViews()
        setupToolbar()
        setupSpinner()
        setupRecycler()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        recyclerView = findViewById(R.id.recyclerCompletedList)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 좌측 햄버거 → 카테고리 팝업
        toolbar.setNavigationOnClickListener { anchor ->
            showCategoryPopup(anchor)
        }

        // 로고 클릭 → 메인으로 이동
        toolbar.findViewById<ImageView>(R.id.imgLogo).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // 로그아웃 클릭
        toolbar.findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            Toast.makeText(this, "로그아웃 완료", Toast.LENGTH_SHORT).show()
            AuthManager.clear(this)
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun setupSpinner() {
        val options = arrayOf("전체", "결재완료", "처리중")
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerFilter.adapter = spinnerAdapter
    }

    private fun setupRecycler() {
        val dataList = listOf(
            "경상남도 김해시 강동 177-1",
            "경상남도 김해시 강동 179-179"
        )
        adapter = CompletedListAdapter(dataList) { address ->
            SurveyResultDialog(this, address).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun showCategoryPopup(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.modal_category, null)

        val displayMetrics = resources.displayMetrics
        val popupWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val popupHeight = resources.getDimensionPixelSize(R.dimen.category_popup_height)

        val popup = PopupWindow(popupView, popupWidth, popupHeight, true)

        popupView.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener { popup.dismiss() }

        popupView.findViewById<MaterialButton>(R.id.btnSurveyScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyStatusActivity::class.java)); popup.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnResurveyTarget)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java)); popup.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            startActivity(Intent(this, TransmissionCompleteActivity::class.java)); popup.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java)); popup.dismiss()
        }

        popup.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) true else super.onOptionsItemSelected(item)
    }
}