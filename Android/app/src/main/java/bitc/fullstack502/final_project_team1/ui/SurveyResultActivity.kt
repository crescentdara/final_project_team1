package bitc.fullstack502.final_project_team1.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R

class SurveyResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_result)

        val btn1 = findViewById<Button>(R.id.input_result_button1)
        val btn2 = findViewById<Button>(R.id.input_result_button2)
        val btn3 = findViewById<Button>(R.id.input_result_button3)
        val btn4 = findViewById<Button>(R.id.input_result_button4)

        val clickListener = {
            val intent = Intent(this, EnterActivity::class.java)
            startActivity(intent)
        }

        btn1.setOnClickListener { clickListener() }
        btn2.setOnClickListener { clickListener() }
        btn3.setOnClickListener { clickListener() }
        btn4.setOnClickListener { clickListener() }
    }
}
