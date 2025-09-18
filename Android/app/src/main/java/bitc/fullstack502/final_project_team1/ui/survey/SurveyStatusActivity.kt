package bitc.fullstack502.final_project_team1.ui.survey

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.ui.category.CategoryActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import bitc.fullstack502.final_project_team1.ui.transmission.TransmissionCompleteActivity

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import kotlin.jvm.java

class SurveyStatusActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var cardSurveyScheduled: MaterialCardView
    private lateinit var cardResurveyTarget: MaterialCardView
    private lateinit var cardTransmissionComplete: MaterialCardView
    private lateinit var cardNotTransmitted: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_status)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        cardSurveyScheduled = findViewById(R.id.cardSurveyScheduled)
        cardResurveyTarget = findViewById(R.id.cardResurveyTarget)
        cardTransmissionComplete = findViewById(R.id.cardTransmissionComplete)
        cardNotTransmitted = findViewById(R.id.cardNotTransmitted)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        cardSurveyScheduled.setOnClickListener {
            // TODO: 조사목록 관리 페이지로 이동
        }

        cardResurveyTarget.setOnClickListener {
            // TODO: 재조사 대상 페이지로 이동
        }

        cardTransmissionComplete.setOnClickListener {
            startActivity(Intent(this, TransmissionCompleteActivity::class.java))
        }

        cardNotTransmitted.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
        }
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
