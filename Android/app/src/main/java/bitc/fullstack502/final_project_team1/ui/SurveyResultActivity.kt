package bitc.fullstack502.final_project_team1.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.MainActivity

class SurveyResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_result)

        // 조사결과 입력 버튼
        val btn1 = findViewById<Button>(R.id.input_result_button1)
        val btn2 = findViewById<Button>(R.id.input_result_button2)
        val btn3 = findViewById<Button>(R.id.input_result_button3)
        val btn4 = findViewById<Button>(R.id.input_result_button4)

        val inputClickListener = {
            val intent = Intent(this, EnterActivity::class.java)
            startActivity(intent)
        }

        btn1.setOnClickListener { inputClickListener() }
        btn2.setOnClickListener { inputClickListener() }
        btn3.setOnClickListener { inputClickListener() }
        btn4.setOnClickListener { inputClickListener() }

        // 왼쪽 하단 뒤로가기 버튼
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}
