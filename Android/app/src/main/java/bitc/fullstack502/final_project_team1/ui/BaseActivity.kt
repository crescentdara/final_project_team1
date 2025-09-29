// bitc/fullstack502/final_project_team1/ui/BaseActivity.kt
package bitc.fullstack502.final_project_team1.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.MainActivity
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.ReinspectListActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import bitc.fullstack502.final_project_team1.ui.transmission.TransmissionCompleteActivity

abstract class BaseActivity : AppCompatActivity() {

    abstract fun bottomNavSelectedItemId(): Int

    /** 툴바 제목만 넘기면, 오른쪽 메뉴(LOGOUT/알림) 공통 처리됨 */
    protected fun initHeader(title: String? = null) {
        findViewById<MaterialToolbar?>(R.id.toolbar)?.let { tb ->
            title?.let { tb.title = it }

            tb.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_logout -> {
                        AuthManager.clear(this)
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                        true
                    }
                    R.id.ivBell -> { // ← 메뉴 아이템 그대로 사용
                        Toast.makeText(this, "알림 페이지는 준비 중입니다.", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // 하단 탭 공통
        findViewById<BottomNavigationView?>(R.id.bottomNav)?.let { bottom ->
            bottom.selectedItemId = bottomNavSelectedItemId()
            bottom.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_survey_list -> {
                        if (this !is SurveyListActivity) {
                            startActivity(Intent(this, SurveyListActivity::class.java))
                            overridePendingTransition(0, 0); finish()
                        }; true
                    }
                    R.id.nav_reinspect -> {
                        if (this !is ReinspectListActivity) {
                            startActivity(Intent(this, ReinspectListActivity::class.java))
                            overridePendingTransition(0, 0); finish()
                        }; true
                    }
                    R.id.nav_home -> {
                        if (this !is MainActivity) {
                            startActivity(Intent(this, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            overridePendingTransition(0, 0); finish()
                        }; true
                    }
                    R.id.nav_history -> {
                        if (this !is TransmissionCompleteActivity) {
                            startActivity(Intent(this, TransmissionCompleteActivity::class.java))
                            overridePendingTransition(0, 0); finish()
                        }; true
                    }
                    R.id.nav_not_transmitted -> {
                        if (this !is DataTransmissionActivity) {
                            startActivity(Intent(this, DataTransmissionActivity::class.java))
                            overridePendingTransition(0, 0); finish()
                        }; true
                    }
                    else -> false
                }
            }
        }
    }
}
