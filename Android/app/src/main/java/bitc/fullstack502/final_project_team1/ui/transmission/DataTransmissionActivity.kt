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

class DataTransmissionActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spinnerSort: Spinner
    private lateinit var btnSurveyResult1: MaterialButton
    private lateinit var btnTransmit1: MaterialButton
    private lateinit var btnSurveyResult2: MaterialButton
    private lateinit var btnTransmit2: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transmission)

        initViews()
        setupClickListeners()
        setupSpinner()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        spinnerSort = findViewById(R.id.spinnerSort)
        btnSurveyResult1 = findViewById(R.id.btnSurveyResult1)
        btnTransmit1 = findViewById(R.id.btnTransmit1)
        btnSurveyResult2 = findViewById(R.id.btnSurveyResult2)
        btnTransmit2 = findViewById(R.id.btnTransmit2)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu) // 햄버거
    }

    private fun setupClickListeners() {
        // 🔹 햄버거 아이콘 → 카테고리 모달
        toolbar.setNavigationOnClickListener {
            showCategoryModal()
        }

        btnSurveyResult1.setOnClickListener {
            showSurveyResultDialog("부산시 남구 대연동 112-12")
        }
        btnTransmit1.setOnClickListener {
            Toast.makeText(this, "전송 완료", Toast.LENGTH_SHORT).show()
        }
        btnSurveyResult2.setOnClickListener {
            showSurveyResultDialog("부산시 남구 대연동 112-12")
        }
        btnTransmit2.setOnClickListener {
            Toast.makeText(this, "전송 완료", Toast.LENGTH_SHORT).show()
        }
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
                // TODO: 정렬 로직
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
            // 현재 화면이므로 이동 없음
            dialog.dismiss()
        }

        dialog.show()
    }

    // ←(홈) 버튼 눌렀을 때도 모달 열기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            showCategoryModal()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}