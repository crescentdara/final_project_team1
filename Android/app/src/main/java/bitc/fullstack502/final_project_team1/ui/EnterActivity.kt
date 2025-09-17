package bitc.fullstack502.final_project_team1.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R

class EnterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)

        // 뒤로가기 버튼 이벤트
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            // SurveyResultActivity로 이동
            val intent = Intent(this, SurveyResultActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        val photoButton = findViewById<Button>(R.id.photo_button)
        photoButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)

        }

        val nextButton = findViewById<Button>(R.id.next_button)
        nextButton.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        }

        val btnInvestigation = findViewById<Button>(R.id.btn_investigation)
        btnInvestigation.setOnClickListener {
            val intent = Intent(this, InvestigationActivity::class.java)
            startActivity(intent)
        }

        val btnPurpose = findViewById<Button>(R.id.btn_purpose) // "행정목적 활용여부" 버튼 id 추가
        btnPurpose.setOnClickListener {
            val intent = Intent(this, PurposeActivity::class.java)
            startActivity(intent)
        }

        val btnIdleRatio = findViewById<Button>(R.id.btn_idle_ratio) // "유휴비율" 버튼 id 추가
        btnIdleRatio.setOnClickListener {
            val intent = Intent(this, IdleRatioActivity::class.java)
            startActivity(intent)
        }

        val btnSafety = findViewById<Button>(R.id.btn_safety) // "안전등급" 버튼 id 추가
        btnSafety.setOnClickListener {
            val intent = Intent(this, SafetyGradeActivity::class.java)
            startActivity(intent)
        }

        val btnExternal = findViewById<Button>(R.id.btn_external_status) // 외부상태 버튼
        btnExternal.setOnClickListener {
            val intent = Intent(this, ExternalStatusActivity::class.java)
            startActivity(intent)
        }

        val btnInternal = findViewById<Button>(R.id.btn_internal_status) // 내부상태 버튼
        btnInternal.setOnClickListener {
            val intent = Intent(this, InternalStatusActivity::class.java)
            startActivity(intent)
        }
    }
}