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
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.ReinspectListActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import bitc.fullstack502.final_project_team1.ui.transmission.TransmissionCompleteActivity
import com.google.android.material.button.MaterialButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var camOutputUri: Uri? = null
    private var camOutputFile: File? = null

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