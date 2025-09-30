package bitc.fullstack502.final_project_team1.ui

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.SurveyResultRequest
import bitc.fullstack502.final_project_team1.ui.transmission.EditActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale
import android.util.Log


class SurveyActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"            // "CREATE" | "EDIT" | "REINSPECT"
        const val EXTRA_SURVEY_ID = "surveyId"   // Long? (EDIT에서 사용)
        const val EXTRA_BUILDING_ID = "buildingId" // Long (모든 모드 기본)
        const val EXTRA_AUTO_SUBMIT = "auto_submit"
        const val EXTRA_RETURN_TO = "return_to"           // "NOT_TRANSMITTED" | "SURVEY_LIST"
        const val RETURN_NOT_TRANSMITTED = "NOT_TRANSMITTED"
        const val RETURN_SURVEY_LIST = "SURVEY_LIST"
        const val RETURN_REINSPECT = "REINSPECT"

    }

    private var mode: String = "CREATE"
    private var editingSurveyId: Long? = null

    // ---- request codes ----
    private val REQ_CAPTURE_EXT = 101
    private val REQ_CAPTURE_INT = 102
    private val REQ_EDIT_EXT = 201
    private val REQ_EDIT_INT = 202
    private val REQ_PICK_EXT = 111
    private val REQ_PICK_INT = 112

    // ▼ 서버 저장된 이미지 URL 보관 (원본/편집 각각)
    private var extPhotoUrl: String? = null
    private var extEditPhotoUrl: String? = null
    private var intPhotoUrl: String? = null
    private var intEditPhotoUrl: String? = null

    // 전달값
    private var assignedBuildingId: Long = -1L
    private lateinit var tvAddress: TextView

    // 하단 액션 버튼
    private lateinit var submitButton: Button
    private lateinit var tempButton: Button

    // 사진 파일(촬영 2장 + 편집본 2장)
    private var extPhotoFile: File? = null
    private var extEditPhotoFile: File? = null
    private var intPhotoFile: File? = null
    private var intEditPhotoFile: File? = null

    // 서버에 기존 사진이 있는지 플래그
    private var hasExtPhotoRemote = false
    private var hasExtEditPhotoRemote = false
    private var hasIntPhotoRemote = false
    private var hasIntEditPhotoRemote = false

    // 카메라 촬영 시 output 파일 임시 보관
    private var pendingOutputFile: File? = null

    // ===== 아코디언 보조 타입 =====
    private data class Accordion(
        val headerId: Int,
        val contentId: Int,
        val badgeId: Int,
        val chevronId: Int,
        val isCompleted: () -> Boolean
    )

    private val accordions by lazy {
        listOf(
            Accordion(
                headerId = R.id.header_investigation,
                contentId = R.id.content_investigation,
                badgeId = R.id.badge_investigation,
                chevronId = R.id.chevron_investigation
            ) {
                // 조사불가 여부만 체크되면 완료로 간주
                findViewById<RadioGroup>(R.id.radioGroup_possible).checkedRadioButtonId != -1
            },
            Accordion(
                headerId = R.id.header_purpose,
                contentId = R.id.content_purpose,
                badgeId = R.id.badge_purpose,
                chevronId = R.id.chevron_purpose
            ) {
                findViewById<RadioGroup>(R.id.radioGroup_adminUse).checkedRadioButtonId != -1
            },
            Accordion(
                headerId = R.id.header_idle,
                contentId = R.id.content_idle,
                badgeId = R.id.badge_idle,
                chevronId = R.id.chevron_idle
            ) {
                findViewById<RadioGroup>(R.id.radioGroup_idleRate).checkedRadioButtonId != -1
            },
            Accordion(
                headerId = R.id.header_safety,
                contentId = R.id.content_safety,
                badgeId = R.id.badge_safety,
                chevronId = R.id.chevron_safety
            ) {
                findViewById<RadioGroup>(R.id.radioGroup_safety).checkedRadioButtonId != -1
            },
            Accordion(
                headerId = R.id.header_external,
                contentId = R.id.content_external,
                badgeId = R.id.badge_external,
                chevronId = R.id.chevron_external
            ) {
                val wallOk = findViewById<RadioGroup>(R.id.radioGroup_wall).checkedRadioButtonId != -1
                val roofOk = findViewById<RadioGroup>(R.id.radioGroup_roof).checkedRadioButtonId != -1
                val winOk  = findViewById<RadioGroup>(R.id.radioGroup_window).checkedRadioButtonId != -1
                val parkOk = findViewById<RadioGroup>(R.id.radioGroup_parking).checkedRadioButtonId != -1
                val photoOk = (extPhotoFile != null || hasExtPhotoRemote) &&
                        (extEditPhotoFile != null || hasExtEditPhotoRemote)
                wallOk && roofOk && winOk && parkOk && photoOk
            },
            Accordion(
                headerId = R.id.header_internal,
                contentId = R.id.content_internal,
                badgeId = R.id.badge_internal,
                chevronId = R.id.chevron_internal
            ) {
                val entOk = findViewById<RadioGroup>(R.id.radioGroup_entrance).checkedRadioButtonId != -1
                val ceilOk = findViewById<RadioGroup>(R.id.radioGroup_ceiling).checkedRadioButtonId != -1
                val floorOk= findViewById<RadioGroup>(R.id.radioGroup_floor).checkedRadioButtonId != -1
                val photoOk = (intPhotoFile != null || hasIntPhotoRemote) &&
                        (intEditPhotoFile != null || hasIntEditPhotoRemote)
                entOk && ceilOk && floorOk && photoOk
            }
        )
    }

    private fun setupAccordions() {
        // 헤더 클릭 토글 + 최초 상태 반영
        accordions.forEach { acc ->
            findViewById<View>(acc.headerId).setOnClickListener {
                toggleContent(acc)
            }
        }
        refreshAccordions(autoCollapse = false) // 최초에는 펼친 상태로 시작
    }

    // 완료 → 자동 접힘 / 배지 표시 / 체브론 회전
    private fun refreshAccordions(autoCollapse: Boolean = true) {
        accordions.forEach { acc ->
            val content = findViewById<View>(acc.contentId)
            val badge = findViewById<View>(acc.badgeId)
            val chevron = findViewById<View>(acc.chevronId)

            val done = acc.isCompleted()
            badge.visibility = if (done) View.VISIBLE else View.GONE

            if (autoCollapse && done && content.visibility == View.VISIBLE) {
                collapse(content) { chevron.rotation = 0f } // 접힘: 0°
            } else if (!done && content.visibility == View.GONE) {
                expand(content) { chevron.rotation = 90f }  // 펼침: 90°
            } else {
                // 상태 유지 + 회전 동기화
                chevron.rotation = if (content.visibility == View.VISIBLE) 90f else 0f
            }
        }
    }

    private fun toggleContent(acc: Accordion) {
        val content = findViewById<View>(acc.contentId)
        val chevron = findViewById<View>(acc.chevronId)
        if (content.visibility == View.VISIBLE) {
            collapse(content) { chevron.rotation = 0f }
        } else {
            expand(content) { chevron.rotation = 90f }
        }
    }

    // 부드러운 펼침/접힘 애니메이션 (height 애니메이션)
    private fun expand(v: View, end: (() -> Unit)? = null) {
        v.measure(
            View.MeasureSpec.makeMeasureSpec((v.parent as View).width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val target = v.measuredHeight
        v.layoutParams.height = 0
        v.visibility = View.VISIBLE

        val anim = android.animation.ValueAnimator.ofInt(0, target)
        anim.duration = 160
        anim.addUpdateListener {
            v.layoutParams.height = it.animatedValue as Int
            v.requestLayout()
        }
        anim.doOnEnd { v.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT; end?.invoke() }
        anim.start()
    }

    private fun collapse(v: View, end: (() -> Unit)? = null) {
        val initial = v.height
        val anim = android.animation.ValueAnimator.ofInt(initial, 0)
        anim.duration = 160
        anim.addUpdateListener {
            v.layoutParams.height = it.animatedValue as Int
            v.requestLayout()
        }
        anim.doOnEnd { v.visibility = View.GONE; end?.invoke() }
        anim.start()
    }

    // Kotlin 확장(애니 끝 콜백)
    private inline fun android.animation.ValueAnimator.doOnEnd(crossinline block: () -> Unit) {
        addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) { block() }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = intent.getStringExtra(EXTRA_MODE) ?: "CREATE"
        assignedBuildingId = intent.getLongExtra(EXTRA_BUILDING_ID, -1L)
            .takeIf { it > 0 } ?: intent.getLongExtra("buildingId", -1L)

        editingSurveyId = if (intent.hasExtra(EXTRA_SURVEY_ID))
            intent.getLongExtra(EXTRA_SURVEY_ID, -1L).takeIf { it > 0 } else null

        setContentView(R.layout.activity_survey)
        setupAccordions()

        // 주소 표시
        tvAddress = findViewById(R.id.tv_address)
        val lotAddress = intent.getStringExtra("lotAddress") ?: ""
        tvAddress.text =
            if (lotAddress.isNotBlank())
                if (assignedBuildingId > 0) "조사중: $lotAddress (ID: $assignedBuildingId)"
                else "조사중: $lotAddress"
            else "조사중: -"

        // 하단 버튼
        submitButton = findViewById(R.id.submit_button)
        tempButton = findViewById(R.id.save_temp_button)
        submitButton.isEnabled = false
        submitButton.alpha = 0.5f
        tempButton.isEnabled = true
        tempButton.alpha = 1f
        submitButton.setOnClickListener { submitSurvey() }
        tempButton.setOnClickListener { saveTemp() }

        // 라디오 변경 시 제출 버튼 상태 갱신
        allRadioGroups().forEach { rg ->
            rg.setOnCheckedChangeListener { _, _ -> updateSubmitState() }
        }

        // 조사불가 여부 변화 시 즉시 토글
        findViewById<RadioGroup>(R.id.radioGroup_possible).setOnCheckedChangeListener { _, _ ->
            applyImpossibleModeIfNeeded()
        }

        // ===== 사진 버튼 =====
        findViewById<ImageButton>(R.id.btn_extPhoto).setOnClickListener {
            startCamera(
                REQ_CAPTURE_EXT
            )
        }

        findViewById<ImageButton>(R.id.btn_intPhoto).setOnClickListener {
            startCamera(REQ_CAPTURE_INT)
        }

        findViewById<ImageButton>(R.id.btn_extEditPhoto).setOnClickListener {
            lifecycleScope.launch {
                val src = pickSrcFileForEdit(extEditPhotoFile, extEditPhotoUrl, extPhotoFile, extPhotoUrl)
                if (src == null) {
                    Toast.makeText(this@SurveyActivity, "외부 사진이 없습니다. 촬영/선택 후 편집하세요.", Toast.LENGTH_SHORT).show()
                } else {
                    startEdit(REQ_EDIT_EXT, src)
                }
            }
        }
        findViewById<ImageButton>(R.id.btn_intEditPhoto).setOnClickListener {
            lifecycleScope.launch {
                val src = pickSrcFileForEdit(intEditPhotoFile, intEditPhotoUrl, intPhotoFile, intPhotoUrl)
                if (src == null) {
                    Toast.makeText(this@SurveyActivity, "내부 사진이 없습니다. 촬영/선택 후 편집하세요.", Toast.LENGTH_SHORT).show()
                } else {
                    startEdit(REQ_EDIT_INT, src)
                }
            }
        }
        findViewById<ImageButton>(R.id.btn_extGallery).setOnClickListener { openGallery(REQ_PICK_EXT) }
        findViewById<ImageButton>(R.id.btn_intGallery).setOnClickListener { openGallery(REQ_PICK_INT) }


        updateSubmitState()
        prefillIfPossible()
    }

    private fun loadInto(ivId: Int, maybeUrl: String?) {
        val url = normalizeUrlMaybe(maybeUrl) ?: return
        val iv = findViewById<ImageView>(ivId)
        com.bumptech.glide.Glide.with(this)
            .load(url)
            .placeholder(android.R.color.darker_gray)
            .error(android.R.color.darker_gray)
            .into(iv)
    }

    // 3-1) 공용 소스 선택기
    private suspend fun pickSrcFileForEdit(
        localEdited: File?, remoteEditedUrl: String?,
        localOriginal: File?, remoteOriginalUrl: String?
    ): File? {
        if (localEdited?.exists() == true) return localEdited        // 1) 로컬 편집본
        if (localOriginal?.exists() == true) return localOriginal    // 2) 로컬 원본
        if (!remoteEditedUrl.isNullOrBlank())                        // 3) 원격 편집본
            downloadRemoteToTemp(remoteEditedUrl)?.let { return it }
        if (!remoteOriginalUrl.isNullOrBlank())                      // 4) 원격 원본
            downloadRemoteToTemp(remoteOriginalUrl)?.let { return it }
        return null
    }


    // 기존 함수 교체
    private suspend fun downloadRemoteToTemp(urlRaw: String): File? =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = normalizeUrlMaybe(urlRaw) ?: return@withContext null
                val ts =
                    java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                val out = File.createTempFile("REMOTE_${ts}_", ".jpg", dir)
                java.net.URL(url).openStream().use { input ->
                    out.outputStream().use { output -> input.copyTo(output) }
                }
                out
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SurveyDL", "download fail for url=$urlRaw", e)

                null
            }
        }


    private fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            // 다중 선택 허용 안 함(필요시 putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true))
        }
        startActivityForResult(intent, requestCode)
    }

    private fun copyUriToTempFile(uri: Uri): File? {
        return try {
            // 임시 파일 생성
            val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val outFile = File.createTempFile("GALLERY_${ts}_", ".jpg", dir)

            // 퍼시스턴트 권한(재부팅/프로세스 재시작 후에도 접근하려면 필요)
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            contentResolver.openInputStream(uri).use { input ->
                outFile.outputStream().use { output ->
                    if (input != null) input.copyTo(output)
                }
            }
            outFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // ✅ 호출 위치에 따라 적절한 화면으로 복귀
    private fun goBackToOrigin() {
        val target = intent.getStringExtra(EXTRA_RETURN_TO) ?: RETURN_NOT_TRANSMITTED
        val targetCls = when (target) {
            RETURN_SURVEY_LIST -> bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity::class.java
            RETURN_REINSPECT -> bitc.fullstack502.final_project_team1.ui.surveyList.ReinspectListActivity::class.java // ★ 추가
            else -> bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity::class.java
        }
        startActivity(Intent(this, targetCls).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        setResult(Activity.RESULT_OK)
        finish()
    }


    // === 불가 모드 판단 ===
    private fun isImpossible(): Boolean =
        idxOfChecked(R.id.radioGroup_possible) == 2   // 1=가능, 2=불가

    private val otherRadioIds = listOf(
        R.id.radioGroup_adminUse,
        R.id.radioGroup_idleRate,
        R.id.radioGroup_safety,
        R.id.radioGroup_wall,
        R.id.radioGroup_roof,
        R.id.radioGroup_window,
        R.id.radioGroup_parking,
        R.id.radioGroup_entrance,
        R.id.radioGroup_ceiling,
        R.id.radioGroup_floor
    )
    private val editTextIds = listOf(R.id.input_extEtc, R.id.input_intEtc)
    private val photoButtonIds =
        listOf(R.id.btn_extPhoto, R.id.btn_extEditPhoto, R.id.btn_intPhoto, R.id.btn_intEditPhoto)
    private val photoImageIds = listOf(R.id.img_extPhoto, R.id.img_intPhoto)

    // === 불가 선택 시/해제 시 UI 토글 ===
    // ▼ 기존 applyImpossibleModeIfNeeded() 완전히 교체
    private fun applyImpossibleModeIfNeeded() {
        val impossible = isImpossible()

        // 라디오 그룹들: 체크 해제 + 자식까지 비활성/활성
        otherRadioIds.forEach { id ->
            val rg = findViewById<RadioGroup>(id)
            if (impossible) rg.clearCheck()
            setEnabledDeep(rg, !impossible)
        }

        // 입력창
        editTextIds.forEach { id ->
            val et = findViewById<EditText>(id)
            if (impossible) et.setText("")
            et.isEnabled = !impossible
        }

        // 사진 버튼/이미지
        photoButtonIds.forEach { id ->
            findViewById<View>(id).isEnabled = !impossible
        }
        if (impossible) {
            // 파일/리모트 플래그/미리보기 리셋
            extPhotoFile = null
            extEditPhotoFile = null
            intPhotoFile = null
            intEditPhotoFile = null
            hasExtPhotoRemote = false
            hasExtEditPhotoRemote = false
            hasIntPhotoRemote = false
            hasIntEditPhotoRemote = false

            photoImageIds.forEach { id ->
                findViewById<ImageView>(id).setImageDrawable(null)
            }
        }

        updateSubmitState()
    }


    // === 제출 버튼 활성/비활성 ===
    private fun updateSubmitState() {
        val enabled = allCompleted()
        submitButton.isEnabled = enabled
        submitButton.alpha = if (enabled) 1f else 0.5f
        tempButton.isEnabled = true
        tempButton.alpha = 1f
        refreshAccordions(autoCollapse = true)
    }

    // === 완료 조건 ===
    private fun allCompleted(): Boolean {
        // 불가면 다른 항목 없이도 제출 가능
        if (isImpossible()) return true

        val requiredGroups = listOf(
            R.id.radioGroup_possible,
            R.id.radioGroup_adminUse,
            R.id.radioGroup_idleRate,
            R.id.radioGroup_safety,
            R.id.radioGroup_wall,
            R.id.radioGroup_roof,
            R.id.radioGroup_window,
            R.id.radioGroup_parking,
            R.id.radioGroup_entrance,
            R.id.radioGroup_ceiling,
            R.id.radioGroup_floor
        )
        val radiosOk = requiredGroups.all { rgId ->
            findViewById<RadioGroup>(rgId).checkedRadioButtonId != -1
        }
        val photosOk =
            (extPhotoFile != null || hasExtPhotoRemote) &&
                    (extEditPhotoFile != null || hasExtEditPhotoRemote) &&
                    (intPhotoFile != null || hasIntPhotoRemote) &&
                    (intEditPhotoFile != null || hasIntEditPhotoRemote)

        return radiosOk && photosOk
    }

    // === DTO 생성: 불가면 숫자 0, 문자열 ""로 보냄 ===
// desiredStatus: "TEMP" 또는 "SENT"
    // desiredStatus: "TEMP" 또는 "SENT"
    private fun buildDtoForSubmitOrTemp(desiredStatus: String): SurveyResultRequest {
        val impossible = isImpossible()

        fun vOrZero(id: Int): Int =
            if (impossible) 0 else idxOfChecked(id).takeIf { it > 0 } ?: 0

        // 편집/재조사로 진입한 경우, 수정 대상 survey PK 확보
        val targetId: Long? = editingSurveyId
            ?: intent.getLongExtra(EXTRA_SURVEY_ID, -1L).takeIf { it > 0 }

        return SurveyResultRequest(
            surveyId = targetId,  // ★ 여기!
            possible = idxOfChecked(R.id.radioGroup_possible),
            adminUse = vOrZero(R.id.radioGroup_adminUse),
            idleRate = vOrZero(R.id.radioGroup_idleRate),
            safety = vOrZero(R.id.radioGroup_safety),
            wall = vOrZero(R.id.radioGroup_wall),
            roof = vOrZero(R.id.radioGroup_roof),
            windowState = vOrZero(R.id.radioGroup_window),
            parking = vOrZero(R.id.radioGroup_parking),
            entrance = vOrZero(R.id.radioGroup_entrance),
            ceiling = vOrZero(R.id.radioGroup_ceiling),
            floor = vOrZero(R.id.radioGroup_floor),
            extEtc = if (impossible) "" else findViewById<EditText>(R.id.input_extEtc).text.toString(),
            intEtc = if (impossible) "" else findViewById<EditText>(R.id.input_intEtc).text.toString(),
            buildingId = assignedBuildingId.takeIf { it > 0 } ?: 1L,
            userId = bitc.fullstack502.final_project_team1.core.AuthManager.userId(this),
            status = desiredStatus
        )
    }

    /** /upload/… 같은 상대경로를 절대 URL로 보정 */
    private fun normalizeUrlMaybe(url: String?): String? {
        val raw = url?.trim().orEmpty()
        if (raw.isEmpty()) return null
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
        val origin = ApiClient.originBaseUrl() // 예: http://10.0.2.2:8080
        val path = if (raw.startsWith("/")) raw else "/$raw"
        return origin + path
    }


    // ===== 유틸 =====
    private fun <T> View.findViewsByType(clazz: Class<T>): List<T> {
        val out = mutableListOf<T>()
        val q: ArrayDeque<View> = ArrayDeque()
        q.add(this)
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            if (clazz.isInstance(v)) out.add(clazz.cast(v)!!)
            if (v is ViewGroup) for (i in 0 until v.childCount) q.add(v.getChildAt(i))
        }
        return out
    }

    private fun allRadioGroups(): List<RadioGroup> {
        val root = findViewById<FrameLayout>(android.R.id.content)
        return root.findViewsByType(RadioGroup::class.java)
    }

    // ===== 카메라 & 편집 =====
    private fun startCamera(requestCode: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // 1) 출력 파일 준비
        val file = createImageFile()
        pendingOutputFile = file
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

        // 2) 카메라 앱이 쓸 수 있게 임시 권한 부여
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // 3) 실제로 해당 인텐트를 받을 모든 카메라 앱에 URI 권한 그랜트
        val resInfoList = packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resInfoList) {
            val pkg = resolveInfo.activityInfo.packageName
            grantUriPermission(
                pkg,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        // (선택) 디버그 확인
//         Toast.makeText(this, "capture to: $uri", Toast.LENGTH_SHORT).show()

        startActivityForResult(intent, requestCode)
    }


    private fun startEdit(requestCode: Int, srcFile: File) {
        val i = Intent(this, EditActivity::class.java).apply {
            putExtra(EditActivity.EXTRA_IMAGE_URI, Uri.fromFile(srcFile).toString())
        }
        startActivityForResult(i, requestCode)
    }

    private fun createImageFile(): File {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${ts}_", ".jpg", dir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQ_CAPTURE_EXT -> {
                extPhotoFile = pendingOutputFile
                pendingOutputFile = null
                extPhotoFile?.absolutePath?.let {
                    BitmapFactory.decodeFile(it)?.also { bmp ->
                        findViewById<ImageView>(R.id.img_extPhoto).setImageBitmap(bmp)
                    }
                }
            }

            REQ_CAPTURE_INT -> {
                intPhotoFile = pendingOutputFile
                pendingOutputFile = null
                intPhotoFile?.absolutePath?.let {
                    BitmapFactory.decodeFile(it)?.also { bmp ->
                        findViewById<ImageView>(R.id.img_intPhoto).setImageBitmap(bmp)
                    }
                }
            }

            // ✅ 갤러리 선택(외부)
            REQ_PICK_EXT -> {
                val uri = data?.data ?: return
                copyUriToTempFile(uri)?.let { file ->
                    extPhotoFile = file
                    hasExtPhotoRemote = false // 로컬 새 파일 선택이므로 리모트 플래그 해제
                    BitmapFactory.decodeFile(file.absolutePath)?.also { bmp ->
                        findViewById<ImageView>(R.id.img_extPhoto).setImageBitmap(bmp)
                    }
                } ?: Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show()
            }

            // ✅ 갤러리 선택(내부)
            REQ_PICK_INT -> {
                val uri = data?.data ?: return
                copyUriToTempFile(uri)?.let { file ->
                    intPhotoFile = file
                    hasIntPhotoRemote = false
                    BitmapFactory.decodeFile(file.absolutePath)?.also { bmp ->
                        findViewById<ImageView>(R.id.img_intPhoto).setImageBitmap(bmp)
                    }
                } ?: Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show()
            }

            REQ_EDIT_EXT -> {
                val uriStr = data?.getStringExtra(EditActivity.EXTRA_EDITED_IMAGE_URI) ?: return
                val file = File(Uri.parse(uriStr).path!!)
                extEditPhotoFile = file
                BitmapFactory.decodeFile(file.absolutePath)?.also { bmp ->
                    findViewById<ImageView>(R.id.img_extPhoto).setImageBitmap(bmp)
                }
            }

            REQ_EDIT_INT -> {
                val uriStr = data?.getStringExtra(EditActivity.EXTRA_EDITED_IMAGE_URI) ?: return
                val file = File(Uri.parse(uriStr).path!!)
                intEditPhotoFile = file
                BitmapFactory.decodeFile(file.absolutePath)?.also { bmp ->
                    findViewById<ImageView>(R.id.img_intPhoto).setImageBitmap(bmp)
                }
            }
        }
        updateSubmitState()
    }


    private fun idxOfChecked(rgId: Int): Int {
        val rg = findViewById<RadioGroup>(rgId)
        if (rg.checkedRadioButtonId == -1) return 0
        return rg.indexOfChild(findViewById(rg.checkedRadioButtonId)) + 1
    }

    // ▼ 추가
    private fun setEnabledDeep(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setEnabledDeep(view.getChildAt(i), enabled)
            }
        }
    }


    private fun File.toPart(name: String): MultipartBody.Part {
        val body = this.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, this.name, body)
    }

    // 제출
    private fun submitSurvey() {
        lifecycleScope.launch {
            val dto = buildDtoForSubmitOrTemp("SENT")   // ★ status 명시
            val impossible = isImpossible()

            val res = ApiClient.service.submitSurvey(
                dto = Gson().toJson(dto).toRequestBody("application/json".toMediaTypeOrNull()),
                extPhoto = if (impossible) null else extPhotoFile?.toPart("extPhoto"),
                extEditPhoto = if (impossible) null else extEditPhotoFile?.toPart("extEditPhoto"),
                intPhoto = if (impossible) null else intPhotoFile?.toPart("intPhoto"),
                intEditPhoto = if (impossible) null else intEditPhotoFile?.toPart("intEditPhoto")
            )

            if (res.isSuccessful) {
                Toast.makeText(this@SurveyActivity, "제출 성공!", Toast.LENGTH_SHORT).show()
                goBackToOrigin()
            } else {
                Toast.makeText(this@SurveyActivity, "실패: ${res.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 임시저장
    private fun saveTemp() {
        lifecycleScope.launch {
            val dto = buildDtoForSubmitOrTemp("TEMP")   // ★ status 명시
            val impossible = isImpossible()

            val res = ApiClient.service.saveTemp(
                dto = Gson().toJson(dto).toRequestBody("application/json".toMediaTypeOrNull()),
                extPhoto = if (impossible) null else extPhotoFile?.toPart("extPhoto"),
                extEditPhoto = if (impossible) null else extEditPhotoFile?.toPart("extEditPhoto"),
                intPhoto = if (impossible) null else intPhotoFile?.toPart("intPhoto"),
                intEditPhoto = if (impossible) null else intEditPhotoFile?.toPart("intEditPhoto")
            )

            if (res.isSuccessful) {
                Toast.makeText(this@SurveyActivity, "임시저장 완료", Toast.LENGTH_SHORT).show()
                goBackToOrigin()
            } else {
                Toast.makeText(this@SurveyActivity, "임시저장 실패: ${res.code()}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun prefillIfPossible() {
        lifecycleScope.launch {
            try {
                val uid =
                    bitc.fullstack502.final_project_team1.core.AuthManager.userId(this@SurveyActivity)
                if (uid <= 0) return@launch

                val detail = when {
                    editingSurveyId != null ->
                        ApiClient.service.getSurveyDetail(uid, editingSurveyId!!)

                    mode == "REINSPECT" ->
                        ApiClient.service.getSurveyLatest(uid, assignedBuildingId)

                    else -> null
                }

                if (detail != null) {
                    // 기존 값 채우기
                    setRadioByIndex(R.id.radioGroup_possible, detail.possible)
                    setRadioByIndex(R.id.radioGroup_adminUse, detail.adminUse)
                    setRadioByIndex(R.id.radioGroup_idleRate, detail.idleRate)
                    setRadioByIndex(R.id.radioGroup_safety, detail.safety)
                    setRadioByIndex(R.id.radioGroup_wall, detail.wall)
                    setRadioByIndex(R.id.radioGroup_roof, detail.roof)
                    setRadioByIndex(R.id.radioGroup_window, detail.windowState)
                    setRadioByIndex(R.id.radioGroup_parking, detail.parking)
                    setRadioByIndex(R.id.radioGroup_entrance, detail.entrance)
                    setRadioByIndex(R.id.radioGroup_ceiling, detail.ceiling)
                    setRadioByIndex(R.id.radioGroup_floor, detail.floor)

                    findViewById<EditText>(R.id.input_extEtc).setText(detail.extEtc ?: "")
                    findViewById<EditText>(R.id.input_intEtc).setText(detail.intEtc ?: "")

                    loadRemotePhotoFlag(detail.extPhoto) { hasExtPhotoRemote = it }
                    loadRemotePhotoFlag(detail.extEditPhoto) { hasExtEditPhotoRemote = it }
                    loadRemotePhotoFlag(detail.intPhoto) { hasIntPhotoRemote = it }
                    loadRemotePhotoFlag(detail.intEditPhoto) { hasIntEditPhotoRemote = it }


                    // ▼ URL 보관
                    extPhotoUrl = detail.extPhoto
                    extEditPhotoUrl = detail.extEditPhoto
                    intPhotoUrl = detail.intPhoto
                    intEditPhotoUrl = detail.intEditPhoto

                    Log.d(
                        "SurveyPrefill",
                        "detail ext=${detail?.extPhoto}, extEdit=${detail?.extEditPhoto}, int=${detail?.intPhoto}, intEdit=${detail?.intEditPhoto}"
                    )

                    // ▼ 서버 보유 플래그
                    hasExtPhotoRemote = !extPhotoUrl.isNullOrBlank()
                    hasExtEditPhotoRemote = !extEditPhotoUrl.isNullOrBlank()
                    hasIntPhotoRemote = !intPhotoUrl.isNullOrBlank()
                    hasIntEditPhotoRemote = !intEditPhotoUrl.isNullOrBlank()

                    // ▼ 화면에 표시: "편집본 우선, 없으면 원본"
//    (normalizeUrlMaybe() + loadInto() 가 같은 클래스에 선언돼 있어야 함)
                    loadInto(R.id.img_extPhoto, extEditPhotoUrl ?: extPhotoUrl)
                    loadInto(R.id.img_intPhoto, intEditPhotoUrl ?: intPhotoUrl)
                }
            } catch (e: Exception) {
                val msg = if (e is retrofit2.HttpException) "HTTP ${e.code()}" else e.message ?: ""
                Toast.makeText(this@SurveyActivity, "이전 결과 불러오기 실패: $msg", Toast.LENGTH_SHORT)
                    .show()
            }

            applyImpossibleModeIfNeeded()
            updateSubmitState()
            maybeAutoSubmitIfRequested()
        }
    }

    // ✅ 자동 전송 시도
    private fun maybeAutoSubmitIfRequested() {
        val wantAuto = intent.getBooleanExtra(EXTRA_AUTO_SUBMIT, false)
        if (!wantAuto) return

        if (allCompleted()) {
            // 모든 항목이 이미 채워져 있으면 즉시 제출
            submitSurvey()
        } else {
            // 미완료면 편집 화면에 머물도록 안내
            Toast.makeText(this, "미입력 항목이 있어 편집 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setRadioByIndex(rgId: Int, idx1based: Int?) {
        if (idx1based == null || idx1based <= 0) return
        val rg = findViewById<RadioGroup>(rgId)
        val child = rg.getChildAt(idx1based - 1) ?: return
        rg.check(child.id)
    }

    private fun loadRemotePhotoFlag(url: String?, onHas: (Boolean) -> Unit) {
        if (!url.isNullOrBlank()) onHas(true)
    }
}
