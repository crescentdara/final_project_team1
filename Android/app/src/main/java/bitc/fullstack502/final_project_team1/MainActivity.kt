package bitc.fullstack502.final_project_team1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.AppUserSurveyStatusResponse
import bitc.fullstack502.final_project_team1.ui.main.ActivityStats
import bitc.fullstack502.final_project_team1.ui.main.ActivityStatsLoader
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

        // ✅ 로그인 체크: 로그인 안 했거나 토큰 만료되면 로그인 화면으로 이동
        if (!AuthManager.isLoggedIn(this) || AuthManager.isExpired(this)) {
            gotoLoginAndFinish()
            return
        }

        // ✅ 메인 레이아웃 연결
        setContentView(R.layout.activity_main)

        // ✅ 상단 툴바 초기화 (로그아웃만 유지)
        setupToolbar()

        // ✅ 중간 버튼 + 하단 네비게이션 클릭 이벤트 연결
        setupNavigation()

        // ✅ 조사현황 숫자 예시 값 세팅 (서버 연동 전 임시)
        val scheduled = 245
        val reinspect = 5
        val notTransmitted = 133
        val pending = 12

        findViewById<TextView>(R.id.tvDashScheduled)?.text = scheduled.toString()
        findViewById<TextView>(R.id.tvDashReinspect)?.text = reinspect.toString()
        findViewById<TextView>(R.id.tvDashNotTransmitted)?.text = notTransmitted.toString()
        findViewById<TextView>(R.id.tvDashPendingApproval)?.text = pending.toString()

        // ✅ 막대 높이 비율 설정 (최댓값 기준)
        val maxValue = listOf(scheduled, reinspect, notTransmitted, pending).maxOrNull() ?: 1
        fun heightFor(value: Int): Int {
            // 최소 보장 높이 12dp, 최대 120dp (배경 카드를 적절히 채우도록)
            val minDp = 12
            val maxDp = 120
            val ratio = if (maxValue == 0) 0f else value.toFloat() / maxValue
            val px = (minDp + ((maxDp - minDp) * ratio)).toInt()
            // dp -> px 변환
            val density = resources.displayMetrics.density
            return (px * density).toInt()
        }

        findViewById<View>(R.id.barScheduled)?.layoutParams?.let { lp ->
            lp.height = heightFor(scheduled)
            findViewById<View>(R.id.barScheduled)?.layoutParams = lp
        }
        findViewById<View>(R.id.barReinspect)?.layoutParams?.let { lp ->
            lp.height = heightFor(reinspect)
            findViewById<View>(R.id.barReinspect)?.layoutParams = lp
        }
        findViewById<View>(R.id.barNotTransmitted)?.layoutParams?.let { lp ->
            lp.height = heightFor(notTransmitted)
            findViewById<View>(R.id.barNotTransmitted)?.layoutParams = lp
        }
        findViewById<View>(R.id.barPendingApproval)?.layoutParams?.let { lp ->
            lp.height = heightFor(pending)
            findViewById<View>(R.id.barPendingApproval)?.layoutParams = lp
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

        // ✅ 서버에서 실시간 조사현황 불러오기 (X-USER-ID 헤더 사용)
        loadSurveyStatus()

        // ✅ 활동현황(진행률/총건수/금일완료) DB 연동 로드
        loadActivityStats()

        // ✅ 환영 토스트 메시지 출력
        Toast.makeText(this, "${userName}님, 환영합니다!", Toast.LENGTH_SHORT).show()
    }

    private fun setupToolbar() {
        // ✅ 로그아웃 버튼 클릭 → 로그아웃 처리
        findViewById<TextView>(R.id.tvLogout)?.setOnClickListener {
            // ✅ 로그아웃 시 인증정보 삭제 후 로그인 화면으로 이동
            AuthManager.clear(this)
            gotoLoginAndFinish()
        }

        // ✅ 알림 버튼 클릭 → 토스트 (또는 알림 화면 연결 예정)
        findViewById<View>(R.id.ivNotification)?.setOnClickListener {
            Toast.makeText(this, "알림 기능 준비중", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ 중간 버튼들과 하단 네비 아이콘 클릭 동작 연결
    private fun setupNavigation() {
        // ----- 중간 4개 액션 버튼 -----
        // 조사예정관리
        findViewById<MaterialButton>(R.id.btnScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
        }
        // 재조사대상
        findViewById<MaterialButton>(R.id.btnReinspect)?.setOnClickListener {
            startActivity(Intent(this, ReinspectListActivity::class.java))
        }
        // 조사내역조회
        findViewById<MaterialButton>(R.id.btnHistory)?.setOnClickListener {
            startActivity(Intent(this, TransmissionCompleteActivity::class.java))
        }
        // 미전송내역
        findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
        }

        // ----- 하단 푸터 네비게이션 -----
        // 조사예정관리
        findViewById<View>(R.id.navScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
        }
        // 재조사대상
        findViewById<View>(R.id.navReinspect)?.setOnClickListener {
            startActivity(Intent(this, ReinspectListActivity::class.java))
        }
        // 메인페이지 (현재 화면이므로 동작 없음)
        findViewById<View>(R.id.navHome)?.setOnClickListener {
            // 필요 시 상단으로 스크롤 등 추가 가능
        }
        // 조사내역조회
        findViewById<View>(R.id.navHistory)?.setOnClickListener {
            startActivity(Intent(this, TransmissionCompleteActivity::class.java))
        }
        // 미전송내역
        findViewById<View>(R.id.navNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
        }
    }


    // ✅ 로그인 화면으로 이동하고 MainActivity 종료
    private fun gotoLoginAndFinish() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    // ─────────────────────────────────────────────────────────────────────
    // 서버 연동: 조사현황 실시간 로드 및 UI 반영
    // ─────────────────────────────────────────────────────────────────────
    private fun loadSurveyStatus() {
        // 한글 주석: 로그인 시 저장된 userId를 꺼내서 헤더로 보낸다
        val userId: Int = try {
            AuthManager.userIdOrThrow(this)
        } catch (e: Exception) {
            Toast.makeText(this, "로그인 정보가 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
            gotoLoginAndFinish()
            return
        }

        lifecycleScope.launch {
            try {
                // 한글 주석: 서버에서 사용자별 조사 현황(approved/rejected/sent/temp) 조회
                val status: AppUserSurveyStatusResponse = ApiClient.service.getSurveyStatus(userId)
                applyStatusToUi(status)
            } catch (e: Exception) {
                // 한글 주석: 네트워크 오류 시 기존 임시값 유지, 사용자에게 안내
                Toast.makeText(this@MainActivity, "조사현황을 불러오지 못했습니다: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyStatusToUi(status: AppUserSurveyStatusResponse) {
        // 한글 주석: 서버 필드 → 화면 지표 매핑
        // - 재조사대상 = REJECTED
        // - 미전송내역 = TEMP(임시저장)
        // - 결재대기 = SENT(제출됨)
        // - 조사예정관리 = APPROVED(임시 매핑, 필요 시 배정 수로 교체 가능)
        val scheduled: Int = status.approved.toInt()
        val reinspect: Int = status.rejected.toInt()
        val notTransmitted: Int = status.temp.toInt()
        val pending: Int = status.sent.toInt()

        // 텍스트 반영
        findViewById<TextView>(R.id.tvDashScheduled)?.text = scheduled.toString()
        findViewById<TextView>(R.id.tvDashReinspect)?.text = reinspect.toString()
        findViewById<TextView>(R.id.tvDashNotTransmitted)?.text = notTransmitted.toString()
        findViewById<TextView>(R.id.tvDashPendingApproval)?.text = pending.toString()

        // 막대 높이 반영 (최댓값 기준 비율)
        val maxValue = listOf(scheduled, reinspect, notTransmitted, pending).maxOrNull() ?: 1
        updateBarHeight(R.id.barScheduled, scheduled, maxValue)
        updateBarHeight(R.id.barReinspect, reinspect, maxValue)
        updateBarHeight(R.id.barNotTransmitted, notTransmitted, maxValue)
        updateBarHeight(R.id.barPendingApproval, pending, maxValue)
    }

    private fun updateBarHeight(barViewId: Int, value: Int, maxValue: Int) {
        val minDp = 12
        val maxDp = 120
        val ratio = if (maxValue == 0) 0f else value.toFloat() / maxValue
        val px = (minDp + ((maxDp - minDp) * ratio)).toInt()
        val density = resources.displayMetrics.density
        val heightPx = (px * density).toInt()

        findViewById<View>(barViewId)?.layoutParams?.let { lp ->
            lp.height = heightPx
            findViewById<View>(barViewId)?.layoutParams = lp
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 서버 연동: 활동현황(진행률/총건수/금일완료) 로드 및 UI 반영
    // ─────────────────────────────────────────────────────────────────────
    private fun loadActivityStats() {
        // 한글 주석: 진행률/총건수/금일완료를 서버 데이터로 계산하여 표시
        lifecycleScope.launch {
            try {
                val stats: ActivityStats = ActivityStatsLoader.fetch(this@MainActivity)
                applyActivityStatsToUi(stats)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "활동현황을 불러오지 못했습니다: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyActivityStatsToUi(stats: ActivityStats) {
        findViewById<TextView>(R.id.tvProgress)?.text = "${stats.progressPercent}%"
        findViewById<TextView>(R.id.tvTotalCount)?.text = stats.totalCount.toString()
        findViewById<TextView>(R.id.tvTodayCount)?.text = stats.todayCompleted.toString()
    }
}