package bitc.fullstack502.final_project_team1

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.DashboardStatsResponse
import bitc.fullstack502.final_project_team1.ui.BaseActivity
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.ReinspectListActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import bitc.fullstack502.final_project_team1.ui.transmission.TransmissionCompleteActivity
import com.google.android.material.button.MaterialButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    override fun bottomNavSelectedItemId(): Int = R.id.nav_home

    private var camOutputUri: Uri? = null
    private var camOutputFile: File? = null

    private lateinit var tvProgress: TextView
    private lateinit var tvTotalCount: TextView
    private lateinit var tvTodayCount: TextView
    private lateinit var tvBarInProgress: TextView
    private lateinit var tvBarWaiting: TextView
    private lateinit var tvBarApproved: TextView
    private lateinit var barInProgress: View
    private lateinit var barWaiting: View
    private lateinit var barApproved: View

    // 풀해상도 저장용 카메라 실행 런처
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Toast.makeText(this, "사진이 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
            // 필요하면 썸네일/미리보기 갱신 로직 추가
        } else {
            Toast.makeText(this, "촬영이 취소되었거나 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // (Q- 전용) 권한 요청 런처
    private val requestWriteExt = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toast.makeText(this, "저장 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }

    // ===== 클릭 시 바로 호출할 메서드 =====
    private fun onClickMainCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Q+ : 별도 권한 없이 진행 (MediaStore로 저장)
            openCamera()
        } else {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) requestWriteExt.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            else openCamera()
        }
    }

    // 실제 카메라 실행
    private fun openCamera() {
        camOutputUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val name = timeStampFileName()
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
            }
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            val f = createTempImageFile()
            camOutputFile = f
            FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
        }

        val uri = camOutputUri
        if (uri == null) {
            Toast.makeText(this, "저장 경로 생성 실패", Toast.LENGTH_SHORT).show()
            return
        }
        takePicture.launch(uri)   // 즉시 카메라 실행 (EXTRA_OUTPUT 사용)
    }

    // ===== 유틸 =====
    private fun createTempImageFile(): File {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())
        val dir = getExternalFilesDir(null) ?: filesDir
        return File.createTempFile("IMG_${ts}_", ".jpg", dir)
    }

    private fun timeStampFileName(): String {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())
        return "IMG_${ts}.jpg"
    }

    fun onMainCameraClick(v: View) {
        onClickMainCamera()   // 권한 체크 + 카메라 실행
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthManager.isLoggedIn(this) || AuthManager.isExpired(this)) {
            gotoLoginAndFinish()
            return
        }

        setContentView(R.layout.activity_main)
        initHeader(title = "부동산 실태조사")

        // 버튼 이동 (기존 그대로)
        findViewById<View>(R.id.btnSurveyList)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
        }
        findViewById<View>(R.id.btnReinspectShortcut)?.setOnClickListener {
            startActivity(Intent(this, ReinspectListActivity::class.java))
        }
        findViewById<View>(R.id.btnNotTransmittedShortcut)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
        }

        // 사용자 정보 표시
        val userName = AuthManager.name(this) ?: "조사원"
        val empNo = AuthManager.empNo(this) ?: "-"
        findViewById<TextView>(R.id.tvUserName).text = "${userName} 님"
        findViewById<TextView>(R.id.tvEmpNo).text = "사번 : $empNo"

        // ⬇️ 통계 뷰 바인딩 (멤버 변수에 담음)
        tvProgress      = findViewById(R.id.tvProgress)
        tvTotalCount    = findViewById(R.id.tvTotalCount)
        tvTodayCount    = findViewById(R.id.tvTodayCount)
        tvBarInProgress = findViewById(R.id.tvInProgressCount)
        tvBarWaiting    = findViewById(R.id.tvWaitingCount)
        tvBarApproved   = findViewById(R.id.tvApprovedCount)
        barInProgress   = findViewById(R.id.barInProgress)
        barWaiting      = findViewById(R.id.barWaiting)
        barApproved     = findViewById(R.id.barApproved)

        // 첫 진입 시 로드
        loadDashboardStats()

        // 자세히 보기
        findViewById<TextView>(R.id.tvDetail)?.setOnClickListener {
            startActivity(Intent(this, TransmissionCompleteActivity::class.java))
        }

        Toast.makeText(this, "${userName}님, 환영합니다!", Toast.LENGTH_SHORT).show()
    }

    private fun loadDashboardStats() {
        val userId = AuthManager.userId(this) ?: -1L
        val token  = AuthManager.token(this) ?: ""
        if (userId <= 0L || token.isBlank()) return

        lifecycleScope.launch {
            try {
                val stats: DashboardStatsResponse =
                    ApiClient.service.getDashboardStats(userId, token)

                // 숫자 갱신
                tvProgress.text      = "${stats.progressRate}%"
                tvTotalCount.text    = stats.total.toString()
                tvTodayCount.text    = stats.todayComplete.toString()
                tvBarInProgress.text = "${stats.inProgress}건"
                tvBarWaiting.text    = "${stats.waitingApproval}건"
                tvBarApproved.text   = "${stats.approved}건"

                // 막대 높이 갱신
                val maxValue   = maxOf(stats.inProgress, stats.waitingApproval, stats.approved, 1)
                val maxHeight  = resources.getDimensionPixelSize(R.dimen.dashboard_bar_max_height)

                fun setBarHeight(bar: View, value: Long) {
                    val ratio = if (maxValue > 0) value.toFloat() / maxValue else 0f
                    val lp = bar.layoutParams
                    lp.height = (maxHeight * ratio).toInt().coerceAtLeast(1)
                    bar.layoutParams = lp
                }

                setBarHeight(barInProgress, stats.inProgress)
                setBarHeight(barWaiting,   stats.waitingApproval)
                setBarHeight(barApproved,  stats.approved)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "대시보드 데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardStats()   // ✅ 항상 갱신
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        loadDashboardStats()   // ✅ singleTop/clearTop로 재진입 시 갱신
    }


    private fun gotoLoginAndFinish() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
