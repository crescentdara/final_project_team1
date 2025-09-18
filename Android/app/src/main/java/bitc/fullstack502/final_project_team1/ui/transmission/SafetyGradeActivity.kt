package bitc.fullstack502.final_project_team1.ui.transmission

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R

class SafetyGradeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_grade)

        // 뒤로가기 버튼 연결
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            finish() // 현재 액티비티 종료 → 이전 화면으로 돌아감
        }
    }
}

