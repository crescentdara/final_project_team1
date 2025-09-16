package bitc.fullstack502.final_project_team1.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R   // ✅ R 명시 임포트

class ArActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)     // ✅ res/layout/activity_ar.xml 이 있어야 함

        val btnAR: Button = findViewById(R.id.btnAR)
        val btnNormal: Button = findViewById(R.id.btnNormal)

        btnAR.setOnClickListener {
            Toast.makeText(this, "AR 모드 실행", Toast.LENGTH_SHORT).show()
            // TODO: AR 화면/로직 시작 (예: startActivity(Intent(this, ArCoreActivity::class.java)))
        }

        btnNormal.setOnClickListener {
            Toast.makeText(this, "일반 모드 실행", Toast.LENGTH_SHORT).show()
            // TODO: 일반 지도/리스트 화면으로 이동
        }
    }
}
