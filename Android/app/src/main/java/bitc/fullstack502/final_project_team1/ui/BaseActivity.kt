package bitc.fullstack502.final_project_team1.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.MainActivity
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.MessageApiClient
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.message.MessageInboxActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.ReinspectListActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import bitc.fullstack502.final_project_team1.ui.transmission.TransmissionCompleteActivity
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {

    abstract fun bottomNavSelectedItemId(): Int

    private var inboxBadgeView: TextView? = null
    private var inboxButtonView: View? = null

    protected fun initHeader(title: String? = null) {
        findViewById<MaterialToolbar?>(R.id.toolbar)?.let { tb ->
            title?.let { tb.title = it }

            // 메뉴에 커스텀 액션뷰(알림 아이콘+배지)가 붙은 아이템(id = ivBell) 가져오기
            val inboxItem = tb.menu.findItem(R.id.ivBell)

            // 액션뷰가 있다면 내부에서 버튼/배지를 찾아서 참조 저장
            inboxItem?.actionView?.let { actionView ->
                inboxButtonView = actionView.findViewById(R.id.ivBell) ?: actionView
                inboxBadgeView  = actionView.findViewById(R.id.tvReadBadge)

                // 아이콘 터치 시 메뉴 아이템 클릭과 동일하게 동작시키기
                inboxButtonView?.setOnClickListener {
                    // ⚠️ Java 메서드 — named argument 쓰지 말 것
                    tb.menu.performIdentifierAction(inboxItem.itemId, 0)
                }
            } ?: run {
                // 액션뷰가 없으면(아이콘만 있는 경우) 배지뷰는 없음
                inboxBadgeView = null
                inboxButtonView = null
            }

            // 공통 메뉴 클릭 처리
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
                    R.id.ivBell -> {  // 알림
                        onClickInbox()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshInboxBadge()
    }



    /** 알림 아이콘 클릭: 메시지 보관함으로 이동 */
    protected open fun onClickInbox() {
        startActivity(Intent(this, MessageInboxActivity::class.java))
    }

    /** 배지 숫자 반영 */
    protected fun updateInboxBadge(count: Int) {
        inboxBadgeView?.apply {
            if (count > 0) {
                text = if (count > 99) "99+" else count.toString()
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    /** 서버에서 미읽음 카운트 로드 → 배지 갱신 */
    protected fun refreshInboxBadge() {
        val uid = AuthManager.userId(this) ?: return   // ← 위치 인자로 호출
        lifecycleScope.launch {
            try {
                val res = MessageApiClient.service.getUnreadCount(uid)  // ← named arg 쓰지 않음
                val countLong = if (res.isSuccessful) (res.body()?.unreadCount ?: 0L) else 0L
                updateInboxBadge(countLong.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()) // ← Int로 변환
            } catch (_: Exception) {
                updateInboxBadge(0)
            }
        }
    }

    /** 화면에 다시 올 때마다 배지 갱신(원하는 위치로 옮겨도 됨) */
    override fun onStart() {
        super.onStart()
        refreshInboxBadge()
    }

    // ─── 홈 네비게이션 공통 ───────────────────────────────────────────────
    fun navigateHomeOrFinish() {
        if (this !is MainActivity) {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
            overridePendingTransition(0, 0)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // ─── 하단 탭 공통 설정 ────────────────────────────────────────────────
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val bottom = findViewById<BottomNavigationView?>(R.id.bottomNav) ?: return

        bottom.selectedItemId = bottomNavSelectedItemId()
        bottom.setOnItemReselectedListener { /* no-op */ }

        bottom.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNavSelectedItemId()) {
                return@setOnItemSelectedListener true
            }
            when (item.itemId) {
                R.id.nav_survey_list -> {
                    startActivity(Intent(this, SurveyListActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    overridePendingTransition(0, 0); true
                }
                R.id.nav_reinspect -> {
                    startActivity(Intent(this, ReinspectListActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    overridePendingTransition(0, 0); true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    overridePendingTransition(0, 0); true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, TransmissionCompleteActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    overridePendingTransition(0, 0); true
                }
                R.id.nav_not_transmitted -> {
                    startActivity(Intent(this, DataTransmissionActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    overridePendingTransition(0, 0); true
                }
                else -> false
            }
        }
    }
}
