package bitc.fullstack502.final_project_team1.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.MainActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.survey.SurveyStatusActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import com.google.android.material.button.MaterialButton

class CategoryActivity : AppCompatActivity() {

    private lateinit var btnClose: View
    private lateinit var btnSurveyStatus: MaterialButton
    private lateinit var btnSurveyList: MaterialButton
    private lateinit var btnSurveyInput: MaterialButton
    private lateinit var btnDataTransmission: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnClose = findViewById(R.id.btnClose)
        btnSurveyStatus = findViewById(R.id.btnSurveyStatus)
        btnSurveyList = findViewById(R.id.btnSurveyList)
        btnSurveyInput = findViewById(R.id.btnSurveyInput)
        btnDataTransmission = findViewById(R.id.btnDataTransmission)
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            finish()
        }

        btnSurveyStatus.setOnClickListener {
            startActivity(Intent(this, SurveyStatusActivity::class.java))
            finish()
        }

        btnSurveyList.setOnClickListener {
            // TODO: 조사목록 관리 페이지 구현
            finish()
        }

        btnSurveyInput.setOnClickListener {
            // TODO: 조사결과 입력 페이지 구현
            finish()
        }

        btnDataTransmission.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
            finish()
        }

    }
}



