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

    // BaseActivity.kt
    fun navigateHomeOrFinish() {
        if (this !is MainActivity) {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
            overridePendingTransition(0, 0)
        } else {
            // 메인에서 백: 앱 종료 행동을 원하면 moveTaskToBack(true) 또는 기본 동작 유지
            onBackPressedDispatcher.onBackPressed()
        }
    }


    // BaseActivity.kt
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val bottom = findViewById<BottomNavigationView?>(R.id.bottomNav) ?: return

        bottom.selectedItemId = bottomNavSelectedItemId()

        // 같은 아이템 다시 누르면 아무 것도 안 함 (선택)
        bottom.setOnItemReselectedListener { /* no-op or 스크롤탑 */ }

        bottom.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNavSelectedItemId()) {
                // 이미 현재 탭이면 이동 안 함
                return@setOnItemSelectedListener true
            }
            when (item.itemId) {
                R.id.nav_survey_list -> {
                    val i = Intent(this, SurveyListActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(i)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_reinspect -> {
                    val i = Intent(this, ReinspectListActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(i)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_home -> {
                    val i = Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(i)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_history -> {
                    val i = Intent(this, TransmissionCompleteActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(i)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_not_transmitted -> {
                    val i = Intent(this, DataTransmissionActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(i)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

}
