package bitc.fullstack502.final_project_team1.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.MainActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.LoginRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etId: TextInputEditText
    private lateinit var etPw: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvError: TextView
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etId = findViewById(R.id.etId)
        etPw = findViewById(R.id.etPw)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)
        progress = findViewById(R.id.progress)

        etPw.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else false
        }
        btnLogin.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        val id = etId.text?.toString()?.trim().orEmpty()
        val pw = etPw.text?.toString().orEmpty()

        tvError.isVisible = false

        if (id.isEmpty() || pw.isEmpty()) {
            tvError.isVisible = true
            tvError.text = getString(R.string.login_error_empty)
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val res = ApiClient.service.login(LoginRequest(id, pw))
                if (res.isSuccessful) {
                    val body = res.body()
                    if (body != null && body.success && body.token != null && body.user != null) {
                        // 토큰 및 사용자 정보 저장 (SharedPreferences)
                        getSharedPreferences("auth", MODE_PRIVATE)
                            .edit()
                            .putString("token", body.token)
                            .putLong("user_id", body.user.id)
                            .putString("username", body.user.username)
                            .putString("name", body.user.name)
                            .putString("role", body.user.role)
                            .putString("email", body.user.email ?: "")
                            .putString("phone", body.user.phone ?: "")
                            .putLong("login_time", System.currentTimeMillis())
                            .apply()

                        Toast.makeText(this@LoginActivity, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        showError(body?.message ?: "서버에서 유효하지 않은 응답을 받았습니다.")
                    }
                } else {
                    when (res.code()) {
                        401 -> showError("아이디 또는 비밀번호가 올바르지 않습니다.")
                        403 -> showError("접근이 거부되었습니다.")
                        404 -> showError("서버를 찾을 수 없습니다.")
                        500 -> showError("서버 내부 오류가 발생했습니다.")
                        else -> showError("로그인 실패: ${res.code()}")
                    }
                }
            } catch (e: java.net.ConnectException) {
                showError("서버에 연결할 수 없습니다. 네트워크를 확인해주세요.")
            } catch (e: java.net.SocketTimeoutException) {
                showError("서버 응답 시간이 초과되었습니다.")
            } catch (e: Exception) {
                showError("네트워크 오류: ${e.message ?: "알 수 없는 오류"}")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        etId.isEnabled = !loading
        etPw.isEnabled = !loading
    }

    private fun showError(msg: String) {
        tvError.isVisible = true
        tvError.text = msg
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}