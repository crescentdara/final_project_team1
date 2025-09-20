package bitc.fullstack502.final_project_team1.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.MainActivity
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.databinding.ActivityLoginBinding
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 이미 로그인되어 있으면 즉시 메인
        if (AuthManager.isLoggedIn(this) && !AuthManager.isExpired(this)) {
            goMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            tvError.isVisible = false

            etPw.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) { doLogin(); true } else false
            }
            btnLogin.setOnClickListener { doLogin() }
        }
    }

    private fun doLogin() = with(binding) {
        val id = etId.text?.toString()?.trim().orEmpty()
        val pw = etPw.text?.toString()?.trim().orEmpty()

        if (id.isEmpty() || pw.isEmpty()) {
            showError("아이디와 비밀번호를 입력하세요.")
            return
        }

        tvError.isVisible = false
        setLoading(true)

        lifecycleScope.launch {
            try {
                val resp = ApiClient.service.login(LoginRequest(id = id, pw = pw))
                if (!resp.isSuccessful) {
                    showError("서버 오류: ${resp.code()}")
                    return@launch
                }
                val body = resp.body()
                if (body == null) {
                    showError("응답이 비어있습니다.")
                    return@launch
                }
                if (body.success) {
                    AuthManager.save(this@LoginActivity, body)
                    Toast.makeText(this@LoginActivity, "${body.name}님 환영합니다!", Toast.LENGTH_SHORT).show()
                    goMain()
                } else {
                    showError(body.message.ifEmpty { "로그인 실패" })
                }
            } catch (e: Exception) {
                showError("네트워크 오류: ${e.localizedMessage}")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun goMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }

    private fun setLoading(loading: Boolean) = with(binding) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        etId.isEnabled = !loading
        etPw.isEnabled = !loading
    }

    private fun showError(msg: String) = with(binding) {
        tvError.isVisible = true
        tvError.text = msg
        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
    }
}
