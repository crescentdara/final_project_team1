package bitc.fullstack502.final_project_team1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.ui.category.CategoryActivity
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 로그인 가드: 미로그인 or 만료 → 로그인으로 리다이렉트 후 즉시 return
        if (!AuthManager.isLoggedIn(this) || AuthManager.isExpired(this)) {
            gotoLoginAndFinish()
            return
        }

        setContentView(R.layout.activity_main)

        setupLogoutButton()
        setupToolbar()

        findViewById<MaterialButton>(R.id.btnSurveyStart)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
        }

        val userName = AuthManager.name(this) ?: "사용자"
        Toast.makeText(this, "${userName}님, 환영합니다!", Toast.LENGTH_SHORT).show()
    }

    private fun setupLogoutButton() {
        findViewById<MaterialButton>(R.id.logoutButton)?.setOnClickListener {
            AuthManager.clear(this)
            gotoLoginAndFinish()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }
    }

    private fun gotoLoginAndFinish() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
