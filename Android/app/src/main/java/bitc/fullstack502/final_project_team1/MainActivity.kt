package bitc.fullstack502.final_project_team1

import android.content.Intent
import android.os.Bundle
import android.widget.TextView   // ✅ TextView 사용
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import bitc.fullstack502.final_project_team1.ui.survey.SurveyStatusActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 로그인 가드: 미로그인 or 만료 → 로그인으로 리다이렉트 후 즉시 return
        if (!AuthManager.isLoggedIn(this) || AuthManager.isExpired(this)) {
            gotoLoginAndFinish()
            return
        }

        setContentView(R.layout.activity_main)

        setupToolbar()

        // ✅ 조사목록 보기 버튼
        findViewById<MaterialButton>(R.id.btnSurveyList)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
        }

        // ✅ 사용자 이름 가져오기 (AuthManager → SharedPreferences 저장값)
        val userName = AuthManager.name(this) ?: "조사원"

        // ✅ 화면에 표시 (activity_main.xml → tvUserName)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        tvUserName.text = "${userName} 조사원님"

        // ✅ 환영 토스트
        Toast.makeText(this, "${userName}님, 환영합니다!", Toast.LENGTH_SHORT).show()
    }

    // ✅ Toolbar 설정 + Navigation + Logout 메뉴 클릭 이벤트
    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        // 🔹 햄버거 버튼 → 모달(카테고리) 표시
        toolbar.setNavigationOnClickListener {
            showCategoryModal()
        }

        // Toolbar 메뉴 이벤트 처리 (menu_main.xml → action_logout)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    AuthManager.clear(this)
                    gotoLoginAndFinish()
                    true
                }

                else -> false
            }
        }
    }

    // ✅ 카테고리 모달 (BottomSheetDialog)
    private fun showCategoryModal() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.modal_category, null)
        dialog.setContentView(view)

        // 메뉴 버튼 클릭 이벤트
        view.findViewById<MaterialButton>(R.id.btnSurveyStatus)?.setOnClickListener {
            startActivity(Intent(this, SurveyStatusActivity::class.java))
            dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnSurveyList)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
            dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnSurveyInput)?.setOnClickListener {
            Toast.makeText(this, "조사결과 입력 페이지 준비중", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnDataTransmission)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
            dialog.dismiss()
        }

        dialog.show()
    }

    // ✅ 로그인 화면으로 이동 + 현재 액티비티 종료
    private fun gotoLoginAndFinish() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}