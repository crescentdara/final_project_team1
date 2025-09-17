package bitc.fullstack502.final_project_team1.ui.transmission

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.ui.survey.SurveyResultDialog
import bitc.fullstack502.final_project_team1.ui.survey.SurveyStatusActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

class TransmissionCompleteActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spinnerFilter: Spinner
    private lateinit var btnSurveyResult1: MaterialButton
    private lateinit var btnApprovalComplete1: MaterialButton
    private lateinit var btnSurveyResult2: MaterialButton
    private lateinit var btnApprovalComplete2: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transmission_complete)

        initViews()
        setupClickListeners()
        setupSpinner()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        btnSurveyResult1 = findViewById(R.id.btnSurveyResult1)
        btnApprovalComplete1 = findViewById(R.id.btnApprovalComplete1)
        btnSurveyResult2 = findViewById(R.id.btnSurveyResult2)
        btnApprovalComplete2 = findViewById(R.id.btnApprovalComplete2)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu) // 햄버거 아이콘
    }

    private fun setupClickListeners() {
        // ✅ 햄버거 → 모달 카테고리 열기
        toolbar.setNavigationOnClickListener {
            showCategoryModal()
        }

        btnSurveyResult1.setOnClickListener {
            showSurveyResultDialog("부산시 남구 대연동 112-12")
        }

        btnApprovalComplete1.setOnClickListener {
            Toast.makeText(this, "결재 완료", Toast.LENGTH_SHORT).show()
        }

        btnSurveyResult2.setOnClickListener {
            showSurveyResultDialog("부산시 남구 대연동 112-12")
        }

        btnApprovalComplete2.setOnClickListener {
            Toast.makeText(this, "결재 완료", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinner() {
        val filterOptions = arrayOf("전체", "결재완료", "처리중")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                // TODO: 필터 로직 구현
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showSurveyResultDialog(address: String) {
        val dialog = SurveyResultDialog(this, address)
        dialog.show()
    }

    // ✅ 카테고리 모달 (BottomSheet)
    private fun showCategoryModal() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.modal_category, null)
        dialog.setContentView(view)

        view.findViewById<MaterialButton>(R.id.btnSurveyScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyStatusActivity::class.java))
            dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnResurveyTarget)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
            dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            Toast.makeText(this, "조사결과 입력 페이지 준비중", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            showCategoryModal()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}