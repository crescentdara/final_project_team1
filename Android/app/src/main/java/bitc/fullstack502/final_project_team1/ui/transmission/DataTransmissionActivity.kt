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
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
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
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = adapter

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                // TODO: 정렬 로직 구현
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
