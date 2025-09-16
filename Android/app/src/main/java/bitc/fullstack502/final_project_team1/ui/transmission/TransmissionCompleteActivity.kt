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
import bitc.fullstack502.final_project_team1.ui.category.CategoryActivity
import bitc.fullstack502.final_project_team1.ui.survey.SurveyResultDialog
import com.google.android.material.appbar.MaterialToolbar
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
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
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
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                // TODO: 필터 로직 구현
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showSurveyResultDialog(address: String) {
        val dialog = SurveyResultDialog(this, address)
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, CategoryActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
