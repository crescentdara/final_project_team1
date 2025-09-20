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
import bitc.fullstack502.final_project_team1.ui.surveyList.ReinspectListActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import bitc.fullstack502.final_project_team1.ui.transmission.TransmissionCompleteActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 로그인 체크: 로그인 안 했거나 토큰 만료되면 로그인 화면으로 이동
        if (!AuthManager.isLoggedIn(this) || AuthManager.isExpired(this)) {
            gotoLoginAndFinish()
            return
        }

        // ✅ 메인 레이아웃 연결
        setContentView(R.layout.activity_main)

        // ✅ 상단 툴바 초기화
        setupToolbar()

        // ✅ "조사목록 보기" 버튼 클릭 시 → SurveyListActivity 이동
        findViewById<MaterialButton>(R.id.btnSurveyList)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
        }

        // ✅ 사용자 이름 + 사번 표시
        val userName = AuthManager.name(this) ?: "조사원"
        val empNo = AuthManager.empNo(this) ?: "-"   // 🔹 AuthManager에서 사번 가져오기

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvEmpNo = findViewById<TextView>(R.id.tvEmpNo)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)
        val tvTotalCount = findViewById<TextView>(R.id.tvTotalCount)
        val tvTodayCount = findViewById<TextView>(R.id.tvTodayCount)

        tvUserName.text = "${userName} 조사원님"
        tvEmpNo.text = "사번 : $empNo"

        // ✅ 통계 데이터 표시 (추후 서버 연동 시 실제 데이터로 교체)
        tvProgress.text = "65%"
        tvTotalCount.text = "24"
        tvTodayCount.text = "3"

        // ✅ 환영 토스트 메시지 출력
        Toast.makeText(this, "${userName}님, 환영합니다!", Toast.LENGTH_SHORT).show()
    }

    private fun setupToolbar() {
        // ✅ 햄버거 메뉴 클릭 → 카테고리 팝업 열기
        findViewById<ImageView>(R.id.ivHamburger)?.setOnClickListener { view ->
            showCategoryPopup(view)
        }

        // ✅ 로그아웃 버튼 클릭 → 로그아웃 처리
        findViewById<TextView>(R.id.tvLogout)?.setOnClickListener {
            // ✅ 로그아웃 시 인증정보 삭제 후 로그인 화면으로 이동
            AuthManager.clear(this)
            gotoLoginAndFinish()
        }
    }

    // ✅ 카테고리 팝업 (햄버거 위치에서 열림, 화면 너비의 60%로 표시)
    private fun showCategoryPopup(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.modal_category, null)

        // ✅ 화면 크기 계산
        val displayMetrics = resources.displayMetrics
        val popupWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val popupHeight = resources.getDimensionPixelSize(R.dimen.category_popup_height)

        val popupWindow = PopupWindow(
            popupView,
            popupWidth,
            popupHeight,
            true
        )

        // ✅ 닫기 버튼 → 팝업 닫기
        popupView.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener {
            popupWindow.dismiss()
        }

        // ✅ 메뉴 버튼들
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

        // ✅ 팝업을 햄버거(anchor) 기준 좌측 상단에 표시
        popupWindow.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    // ✅ 로그인 화면으로 이동하고 MainActivity 종료
    private fun gotoLoginAndFinish() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}