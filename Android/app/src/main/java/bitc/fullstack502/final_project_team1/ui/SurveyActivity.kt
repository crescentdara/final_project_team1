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

class SurveyActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"            // "CREATE" | "EDIT" | "REINSPECT"
        const val EXTRA_SURVEY_ID = "surveyId"   // Long? (EDIT에서 사용)
        const val EXTRA_BUILDING_ID = "buildingId" // Long (모든 모드 기본)
    }

    private var mode: String = "CREATE"
    private var editingSurveyId: Long? = null

    // ---- request codes ----
    private val REQ_CAPTURE_EXT = 101
    private val REQ_CAPTURE_INT = 102
    private val REQ_EDIT_EXT    = 201
    private val REQ_EDIT_INT    = 202

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = intent.getStringExtra(EXTRA_MODE) ?: "CREATE"
        assignedBuildingId = intent.getLongExtra(EXTRA_BUILDING_ID, -1L)
            .takeIf { it > 0 } ?: intent.getLongExtra("buildingId", -1L)

        editingSurveyId = if (intent.hasExtra(EXTRA_SURVEY_ID))
            intent.getLongExtra(EXTRA_SURVEY_ID, -1L).takeIf { it > 0 } else null

        setContentView(R.layout.activity_survey)

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
        tempButton   = findViewById(R.id.save_temp_button)
        submitButton.isEnabled = false
        submitButton.alpha = 0.5f
        tempButton.isEnabled = true
        tempButton.alpha = 1f
        submitButton.setOnClickListener { submitSurvey() }
        tempButton.setOnClickListener   { saveTemp() }

        // 라디오 변경 시 제출 버튼 상태 갱신
        allRadioGroups().forEach { rg ->
            rg.setOnCheckedChangeListener { _, _ -> updateSubmitState() }
        }

        // 조사불가 여부 변화 시 즉시 토글
        findViewById<RadioGroup>(R.id.radioGroup_possible).setOnCheckedChangeListener { _, _ ->
            applyImpossibleModeIfNeeded()
        }

        // ===== 사진 버튼 =====
        findViewById<ImageButton>(R.id.btn_extPhoto).setOnClickListener { startCamera(REQ_CAPTURE_EXT) }
        findViewById<ImageButton>(R.id.btn_extEditPhoto).setOnClickListener {
            val src = extPhotoFile ?: return@setOnClickListener Toast.makeText(this, "외부 사진을 먼저 촬영하세요.", Toast.LENGTH_SHORT).show()
            startEdit(REQ_EDIT_EXT, src)
        }
        findViewById<ImageButton>(R.id.btn_intPhoto).setOnClickListener { startCamera(REQ_CAPTURE_INT) }
        findViewById<ImageButton>(R.id.btn_intEditPhoto).setOnClickListener {
            val src = intPhotoFile ?: return@setOnClickListener Toast.makeText(this, "내부 사진을 먼저 촬영하세요.", Toast.LENGTH_SHORT).show()
            startEdit(REQ_EDIT_INT, src)
        }

        updateSubmitState()
        prefillIfPossible()
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
    private val photoButtonIds = listOf(R.id.btn_extPhoto, R.id.btn_extEditPhoto, R.id.btn_intPhoto, R.id.btn_intEditPhoto)
    private val photoImageIds  = listOf(R.id.img_extPhoto, R.id.img_intPhoto)

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
    private fun buildDtoForSubmitOrTemp(): SurveyResultRequest {
        val impossible = isImpossible()
        fun vOrZero(id: Int): Int =
            if (impossible) 0 else idxOfChecked(id).takeIf { it > 0 } ?: 0

        return SurveyResultRequest(
            possible    = idxOfChecked(R.id.radioGroup_possible), // 불가(2) 그대로
            adminUse    = vOrZero(R.id.radioGroup_adminUse),
            idleRate    = vOrZero(R.id.radioGroup_idleRate),
            safety      = vOrZero(R.id.radioGroup_safety),
            wall        = vOrZero(R.id.radioGroup_wall),
            roof        = vOrZero(R.id.radioGroup_roof),
            windowState = vOrZero(R.id.radioGroup_window),
            parking     = vOrZero(R.id.radioGroup_parking),
            entrance    = vOrZero(R.id.radioGroup_entrance),
            ceiling     = vOrZero(R.id.radioGroup_ceiling),
            floor       = vOrZero(R.id.radioGroup_floor),
            extEtc      = if (impossible) "" else findViewById<EditText>(R.id.input_extEtc).text.toString(),
            intEtc      = if (impossible) "" else findViewById<EditText>(R.id.input_intEtc).text.toString(),
            buildingId  = assignedBuildingId.takeIf { it > 0 } ?: 1L,
            userId      = bitc.fullstack502.final_project_team1.core.AuthManager.userId(this)
        )
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
        val file = createImageFile()
        pendingOutputFile = file
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
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
            val dto = buildDtoForSubmitOrTemp()
            val impossible = isImpossible()

            val res = ApiClient.service.submitSurvey(
                dto = Gson().toJson(dto).toRequestBody("application/json".toMediaTypeOrNull()),
                extPhoto     = if (impossible) null else extPhotoFile?.toPart("extPhoto"),
                extEditPhoto = if (impossible) null else extEditPhotoFile?.toPart("extEditPhoto"),
                intPhoto     = if (impossible) null else intPhotoFile?.toPart("intPhoto"),
                intEditPhoto = if (impossible) null else intEditPhotoFile?.toPart("intEditPhoto")
            )

            if (res.isSuccessful) {
                Toast.makeText(this@SurveyActivity, "제출 성공!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@SurveyActivity, "실패: ${res.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 임시저장
    private fun saveTemp() {
        lifecycleScope.launch {
            val dto = buildDtoForSubmitOrTemp()
            val impossible = isImpossible()

            val res = ApiClient.service.saveTemp(
                dto = Gson().toJson(dto).toRequestBody("application/json".toMediaTypeOrNull()),
                extPhoto     = if (impossible) null else extPhotoFile?.toPart("extPhoto"),
                extEditPhoto = if (impossible) null else extEditPhotoFile?.toPart("extEditPhoto"),
                intPhoto     = if (impossible) null else intPhotoFile?.toPart("intPhoto"),
                intEditPhoto = if (impossible) null else intEditPhotoFile?.toPart("intEditPhoto")
            )

            if (res.isSuccessful) {
                Toast.makeText(this@SurveyActivity, "임시저장 완료", Toast.LENGTH_SHORT).show()
                // 조사목록으로 이동
                startActivity(Intent(this@SurveyActivity,
                    bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@SurveyActivity, "임시저장 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prefillIfPossible() {
        lifecycleScope.launch {
            try {
                val uid = bitc.fullstack502.final_project_team1.core.AuthManager.userId(this@SurveyActivity)
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
                    setRadioByIndex(R.id.radioGroup_possible,    detail.possible)
                    setRadioByIndex(R.id.radioGroup_adminUse,    detail.adminUse)
                    setRadioByIndex(R.id.radioGroup_idleRate,    detail.idleRate)
                    setRadioByIndex(R.id.radioGroup_safety,      detail.safety)
                    setRadioByIndex(R.id.radioGroup_wall,        detail.wall)
                    setRadioByIndex(R.id.radioGroup_roof,        detail.roof)
                    setRadioByIndex(R.id.radioGroup_window,      detail.windowState)
                    setRadioByIndex(R.id.radioGroup_parking,     detail.parking)
                    setRadioByIndex(R.id.radioGroup_entrance,    detail.entrance)
                    setRadioByIndex(R.id.radioGroup_ceiling,     detail.ceiling)
                    setRadioByIndex(R.id.radioGroup_floor,       detail.floor)

                    findViewById<EditText>(R.id.input_extEtc).setText(detail.extEtc ?: "")
                    findViewById<EditText>(R.id.input_intEtc).setText(detail.intEtc ?: "")

                    loadRemotePhotoFlag(detail.extPhoto)     { hasExtPhotoRemote = it }
                    loadRemotePhotoFlag(detail.extEditPhoto) { hasExtEditPhotoRemote = it }
                    loadRemotePhotoFlag(detail.intPhoto)     { hasIntPhotoRemote = it }
                    loadRemotePhotoFlag(detail.intEditPhoto) { hasIntEditPhotoRemote = it }
                }
            } catch (e: Exception) {
                val msg = if (e is retrofit2.HttpException) "HTTP ${e.code()}" else e.message ?: ""
                Toast.makeText(this@SurveyActivity, "이전 결과 불러오기 실패: $msg", Toast.LENGTH_SHORT).show()
            }

            applyImpossibleModeIfNeeded()
            updateSubmitState()
        }
    }


    private fun setRadioByIndex(rgId: Int, idx1based: Int?) {
        if (idx1based == null || idx1based <= 0) return
        val rg = findViewById<RadioGroup>(rgId)
        val child = rg.getChildAt(idx1based - 1) ?: return
        rg.check(child.id)
    }

    private fun loadRemotePhotoFlag(url: String?, onHas: (Boolean)->Unit) {
        if (!url.isNullOrBlank()) onHas(true)
    }
}
