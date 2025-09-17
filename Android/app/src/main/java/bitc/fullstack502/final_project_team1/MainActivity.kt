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
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.survey.SurveyStatusActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthManager.isLoggedIn(this) || AuthManager.isExpired(this)) {
            gotoLoginAndFinish()
            return
        }

        setContentView(R.layout.activity_main)

        setupToolbar()

        findViewById<MaterialButton>(R.id.btnSurveyList)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
        }

        val userName = AuthManager.name(this) ?: "조사원"
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        tvUserName.text = "${userName} 조사원님"

        Toast.makeText(this, "${userName}님, 환영합니다!", Toast.LENGTH_SHORT).show()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        // ✅ 햄버거 클릭 → 팝업 카테고리 열기
        toolbar.setNavigationOnClickListener { view ->
            showCategoryPopup(view)
        }

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

    // ✅ 카테고리 팝업 (햄버거 위치에서 열림, 화면 너비의 70%)
    private fun showCategoryPopup(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.modal_category, null)

        // 동적으로 화면 너비의 60% 계산
        val displayMetrics = resources.displayMetrics
        val popupWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val popupHeight = resources.getDimensionPixelSize(R.dimen.category_popup_height)

        val popupWindow = PopupWindow(
            popupView,
            popupWidth,
            popupHeight,
            true
        )

        // 닫기 버튼
        popupView.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener {
            popupWindow.dismiss()
        }

        // 메뉴 버튼들
        popupView.findViewById<MaterialButton>(R.id.btnSurveyScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyStatusActivity::class.java))
            popupWindow.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnResurveyTarget)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
            popupWindow.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            Toast.makeText(this, "조사내역 조회 준비중", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
            popupWindow.dismiss()
        }

        // ✅ 햄버거 버튼(anchor) 위치 기준 좌측 상단에 표시
        popupWindow.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    private fun gotoLoginAndFinish() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}