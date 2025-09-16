package bitc.fullstack502.final_project_team1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import bitc.fullstack502.final_project_team1.ui.category.CategoryActivity
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 로그인 상태 확인
        checkLoginStatus()

        // 레이아웃을 붙여줘야 activity_main.xml 이 화면에 보입니다.
        setContentView(R.layout.activity_main)

        // 로그아웃 버튼 설정
        setupLogoutButton()
        
        // 툴바 설정
        setupToolbar()

        // 환영 메시지 표시
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userName = prefs.getString("name", "사용자")
        Toast.makeText(this@MainActivity, userName + "님, 환영합니다!", Toast.LENGTH_SHORT).show()
    }

    private fun checkLoginStatus() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val loginTime = prefs.getLong("login_time", 0)
        
        // 토큰이 없거나 로그인 시간이 24시간 이상 지났으면 재로그인 요구
        if (token == null || token.isEmpty() || 
            (System.currentTimeMillis() - loginTime) > 24 * 60 * 60 * 1000) {
            // 로그인되지 않은 경우 로그인 화면으로 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupLogoutButton() {
        // 로그아웃 버튼 찾기 (마지막 버튼이 로그아웃 버튼)
        val logoutButton = findViewById<MaterialButton>(R.id.logoutButton)
        logoutButton?.setOnClickListener {
            logout()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }
    }

    private fun logout() {
        // 저장된 인증 정보 삭제
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // 로그인 화면으로 이동
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
