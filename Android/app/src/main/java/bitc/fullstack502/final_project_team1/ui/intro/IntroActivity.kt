package bitc.fullstack502.final_project_team1.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.MainActivity
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import com.google.android.material.button.MaterialButton

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_Theme_Final_Project_Team1)
        super.onCreate(savedInstanceState)

        // 이미 로그인 & 세션 유효 → 바로 메인으로
        if (AuthManager.isLoggedIn(this) && !AuthManager.isExpired(this)) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setContentView(R.layout.activity_intro)

        findViewById<MaterialButton>(R.id.btnStartLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}