package bitc.fullstack502.final_project_team1

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.DashboardStatsResponse
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.ReinspectListActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import bitc.fullstack502.final_project_team1.ui.transmission.TransmissionCompleteActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 로그인 체크
        if (!AuthManager.isLoggedIn(this) || AuthManager.isExpired(this)) {
            gotoLoginAndFinish()
            return
        }

        setContentView(R.layout.activity_main)
        setupToolbar()

        // ✅ 조사목록 버튼
        findViewById<MaterialButton>(R.id.btnSurveyList)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
        }

        // ✅ 사용자 정보
        val userName = AuthManager.name(this) ?: "조사원"
        val empNo = AuthManager.empNo(this) ?: "-"

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvEmpNo = findViewById<TextView>(R.id.tvEmpNo)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)
        val tvTotalCount = findViewById<TextView>(R.id.tvTotalCount)
        val tvTodayCount = findViewById<TextView>(R.id.tvTodayCount)

        // ✅ 조사현황 뷰 (막대 + 숫자)
        val barInProgress = findViewById<View>(R.id.barInProgress)
        val barWaiting = findViewById<View>(R.id.barWaiting)
        val barApproved = findViewById<View>(R.id.barApproved)
        val tvBarInProgress = findViewById<TextView>(R.id.tvInProgressCount)
        val tvBarWaiting = findViewById<TextView>(R.id.tvWaitingCount)
        val tvBarApproved = findViewById<TextView>(R.id.tvApprovedCount)

        tvUserName.text = "${userName} 조사원님"
        tvEmpNo.text = "사번 : $empNo"

        val userId = AuthManager.userId(this) ?: -1L
        val token = AuthManager.token(this) ?: ""

        // ✅ 대시보드 데이터 불러오기
        lifecycleScope.launch {
            try {
                val stats: DashboardStatsResponse = ApiClient.service.getDashboardStats(
                    userId, token
                )

                // 활동 현황
                tvProgress.text = "${stats.progressRate}%"
                tvTotalCount.text = stats.total.toString()
                tvTodayCount.text = stats.todayComplete.toString()

                // 조사 현황 값 반영
                tvBarInProgress.text = "${stats.inProgress}건"
                tvBarWaiting.text = "${stats.waitingApproval}건"
                tvBarApproved.text = "${stats.approved}건"

                // 막대 비율 적용 (회색 틀 대비)
                val maxValue = maxOf(stats.inProgress, stats.waitingApproval, stats.approved, 1)
                val maxHeightPx = resources.getDimensionPixelSize(
                    R.dimen.dashboard_bar_max_height
                ) // 예: 100dp 정의해두기

                fun setBarHeight(bar: View, value: Long) {
                    val ratio = if (maxValue > 0) value.toFloat() / maxValue else 0f
                    val params = bar.layoutParams
                    params.height = (maxHeightPx * ratio).toInt()
                    bar.layoutParams = params
                }

                setBarHeight(barInProgress, stats.inProgress)
                setBarHeight(barWaiting, stats.waitingApproval)
                setBarHeight(barApproved, stats.approved)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "대시보드 데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ 환영 메시지
        Toast.makeText(this, "${userName}님, 환영합니다!", Toast.LENGTH_SHORT).show()
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.ivHamburger)?.setOnClickListener { view ->
            showCategoryPopup(view)
        }
        findViewById<TextView>(R.id.tvLogout)?.setOnClickListener {
            AuthManager.clear(this)
            gotoLoginAndFinish()
        }
    }

    // ✅ 카테고리 팝업
    private fun showCategoryPopup(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.modal_category, null)
        val displayMetrics = resources.displayMetrics
        val popupWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val popupHeight = resources.getDimensionPixelSize(R.dimen.category_popup_height)

        val popupWindow = PopupWindow(
            popupView,
            popupWidth,
            popupHeight,
            true
        )

        popupView.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener {
            popupWindow.dismiss()
        }

        popupView.findViewById<MaterialButton>(R.id.btnSurveyScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
            popupWindow.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnResurveyTarget)?.setOnClickListener {
            startActivity(Intent(this, ReinspectListActivity::class.java))
            popupWindow.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            startActivity(Intent(this, TransmissionCompleteActivity::class.java))
            popupWindow.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    private fun gotoLoginAndFinish() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
