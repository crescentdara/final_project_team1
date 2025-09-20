package bitc.fullstack502.final_project_team1.ui

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
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
import android.widget.LinearLayout
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

    // ---- request codes ----
    private val REQ_CAPTURE_EXT = 101
    private val REQ_CAPTURE_INT = 102
    private val REQ_EDIT_EXT    = 201
    private val REQ_EDIT_INT    = 202

    // 전달값
    private var assignedBuildingId: Long = -1L
    private lateinit var tvAddress: TextView

    // 스테이지
    private lateinit var stages: List<LinearLayout>
    private var currentStage = 0
    private lateinit var nextButton: Button
    private lateinit var backButton: Button
    private lateinit var submitButton: Button
    private lateinit var tempButton: Button
    private lateinit var tabButtons: Map<Button, Int>

    // 사진 파일(촬영 2장 + 편집본 2장)
    private var extPhotoFile: File? = null
    private var extEditPhotoFile: File? = null
    private var intPhotoFile: File? = null
    private var intEditPhotoFile: File? = null

    // 카메라 촬영 시 output 파일 임시 보관
    private var pendingOutputFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        // 주소 표시
        tvAddress = findViewById(R.id.tv_address)
        assignedBuildingId = intent.getLongExtra("buildingId", -1L)
        val lotAddress = intent.getStringExtra("lotAddress") ?: ""
        tvAddress.text =
            if (lotAddress.isNotBlank())
                if (assignedBuildingId > 0) "조사중: $lotAddress (ID: $assignedBuildingId)"
                else "조사중: $lotAddress"
            else "조사중: -"

        // 스테이지(순서 고정)
        stages = listOf(
            findViewById(R.id.stage_investigation),
            findViewById(R.id.stage_purpose),
            findViewById(R.id.stage_idle_ratio),
            findViewById(R.id.stage_safety),
            findViewById(R.id.stage_external_status),
            findViewById(R.id.stage_internal_status)
        )

        // 하단 버튼
        backButton = findViewById(R.id.back_button)
        nextButton = findViewById(R.id.next_button)
        submitButton = findViewById(R.id.submit_button)
        tempButton = findViewById(R.id.save_temp_button)
        submitButton.visibility = View.GONE
        tempButton.visibility   = View.GONE

        backButton.setOnClickListener { if (currentStage > 0) showStage(currentStage - 1) }
        nextButton.setOnClickListener { if (currentStage < stages.size - 1) showStage(currentStage + 1) }
        submitButton.setOnClickListener { submitSurvey() }
        tempButton.setOnClickListener { saveTemp() }

        // 상단 탭
        tabButtons = mapOf(
            findViewById<Button>(R.id.btn_investigation) to 0,
            findViewById<Button>(R.id.btn_purpose) to 1,
            findViewById<Button>(R.id.btn_idle_ratio) to 2,
            findViewById<Button>(R.id.btn_safety) to 3,
            findViewById<Button>(R.id.btn_external_status) to 4,
            findViewById<Button>(R.id.btn_internal_status) to 5
        )
        tabButtons.forEach { (btn, idx) -> btn.setOnClickListener { showStage(idx) } }

        // ===== 사진 버튼 =====
        // 외부 - 촬영
        findViewById<ImageButton>(R.id.btn_extPhoto).setOnClickListener {
            startCamera(REQ_CAPTURE_EXT)
        }
        // 외부 - 편집(연필)
        findViewById<ImageButton>(R.id.btn_extEditPhoto).setOnClickListener {
            val src = extPhotoFile
            if (src == null) {
                Toast.makeText(this, "외부 사진을 먼저 촬영하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startEdit(REQ_EDIT_EXT, src)
        }

        // 내부 - 촬영
        findViewById<ImageButton>(R.id.btn_intPhoto).setOnClickListener {
            startCamera(REQ_CAPTURE_INT)
        }
        // 내부 - 편집(연필)
        findViewById<ImageButton>(R.id.btn_intEditPhoto).setOnClickListener {
            val src = intPhotoFile
            if (src == null) {
                Toast.makeText(this, "내부 사진을 먼저 촬영하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startEdit(REQ_EDIT_INT, src)
        }

        // 폼 전체 라디오 변화 감지 → 다음/제출 버튼 상태 갱신
        allRadioGroups().forEach { rg ->
            rg.setOnCheckedChangeListener { _, _ ->
                updateNextButton()
                updateSubmitVisibility()
            }
        }

        showStage(0)
        updateSubmitVisibility()
    }

    // ===== 화면 전환/버튼 상태 =====
    private fun showStage(index: Int) {
        stages.forEach { it.visibility = View.GONE }
        currentStage = index
        stages[currentStage].visibility = View.VISIBLE
        updateTabs()
        updateNextButton()
        updateSubmitVisibility()
    }

    // ===== 상단 탭 색상 변경 =====
    private fun updateTabs() {
        tabButtons.forEach { (button, idx) ->
            val selected = idx == currentStage
            button.backgroundTintList = ColorStateList.valueOf(
                if (selected) Color.parseColor("#6898FF") else Color.parseColor("#CCCCCC")
            )
            button.setTextColor(Color.WHITE)
        }
    }

    private fun updateNextButton() {
        val allCheckedInStage = stages[currentStage]
            .findViewsByType(RadioGroup::class.java)
            .all { it.checkedRadioButtonId != -1 }
        nextButton.isEnabled = allCheckedInStage && currentStage < stages.size - 1
        nextButton.alpha = if (nextButton.isEnabled) 1f else 0.5f
    }

    private fun allCompleted(): Boolean {
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
        val photosOk = extPhotoFile != null &&
                intPhotoFile != null &&
                extEditPhotoFile != null &&
                intEditPhotoFile != null
        return radiosOk && photosOk
    }

    private fun updateSubmitVisibility() {
        val visible = if (allCompleted()) View.VISIBLE else View.GONE
        submitButton.visibility = visible
        tempButton.visibility   = visible
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
        updateSubmitVisibility()
    }

    private fun idxOfChecked(rgId: Int): Int {
        val rg = findViewById<RadioGroup>(rgId)
        if (rg.checkedRadioButtonId == -1) return 0
        return rg.indexOfChild(findViewById(rg.checkedRadioButtonId)) + 1
    }

    private fun File.toPart(name: String): MultipartBody.Part {
        val body = this.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, this.name, body)
    }

    // ===== 제출(최종) =====
    private fun submitSurvey() {
        lifecycleScope.launch {
            val dto = SurveyResultRequest(
                possible    = idxOfChecked(R.id.radioGroup_possible),
                adminUse    = idxOfChecked(R.id.radioGroup_adminUse),
                idleRate    = idxOfChecked(R.id.radioGroup_idleRate),
                safety      = idxOfChecked(R.id.radioGroup_safety),
                wall        = idxOfChecked(R.id.radioGroup_wall),
                roof        = idxOfChecked(R.id.radioGroup_roof),
                windowState = idxOfChecked(R.id.radioGroup_window),
                parking     = idxOfChecked(R.id.radioGroup_parking),
                entrance    = idxOfChecked(R.id.radioGroup_entrance),
                ceiling     = idxOfChecked(R.id.radioGroup_ceiling),
                floor       = idxOfChecked(R.id.radioGroup_floor),
                extEtc      = findViewById<EditText>(R.id.input_extEtc).text.toString(),
                intEtc      = findViewById<EditText>(R.id.input_intEtc).text.toString(),
                buildingId  = assignedBuildingId.takeIf { it > 0 } ?: 1L,
                userId      = 1
            )

            val dtoBody = Gson().toJson(dto)
                .toRequestBody("application/json".toMediaTypeOrNull())

            val res = ApiClient.service.submitSurvey(
                dto = dtoBody,
                extPhoto     = extPhotoFile?.toPart("extPhoto"),
                extEditPhoto = extEditPhotoFile?.toPart("extEditPhoto"),
                intPhoto     = intPhotoFile?.toPart("intPhoto"),
                intEditPhoto = intEditPhotoFile?.toPart("intEditPhoto")
            )

            if (res.isSuccessful) {
                Toast.makeText(this@SurveyActivity, "제출 성공!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@SurveyActivity, "실패: ${res.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===== 임시저장 =====
    private fun saveTemp() {
        lifecycleScope.launch {
            val dto = SurveyResultRequest(
                possible    = idxOfChecked(R.id.radioGroup_possible),
                adminUse    = idxOfChecked(R.id.radioGroup_adminUse),
                idleRate    = idxOfChecked(R.id.radioGroup_idleRate),
                safety      = idxOfChecked(R.id.radioGroup_safety),
                wall        = idxOfChecked(R.id.radioGroup_wall),
                roof        = idxOfChecked(R.id.radioGroup_roof),
                windowState = idxOfChecked(R.id.radioGroup_window),
                parking     = idxOfChecked(R.id.radioGroup_parking),
                entrance    = idxOfChecked(R.id.radioGroup_entrance),
                ceiling     = idxOfChecked(R.id.radioGroup_ceiling),
                floor       = idxOfChecked(R.id.radioGroup_floor),
                extEtc      = findViewById<EditText>(R.id.input_extEtc).text.toString(),
                intEtc      = findViewById<EditText>(R.id.input_intEtc).text.toString(),
                buildingId  = assignedBuildingId.takeIf { it > 0 } ?: 1L,
                userId      = 1
            )

            val dtoBody = Gson().toJson(dto).toRequestBody("application/json".toMediaTypeOrNull())

            val res = ApiClient.service.saveTemp(
                dto = dtoBody,
                extPhoto     = extPhotoFile?.toPart("extPhoto"),
                extEditPhoto = extEditPhotoFile?.toPart("extEditPhoto"),
                intPhoto     = intPhotoFile?.toPart("intPhoto"),
                intEditPhoto = intEditPhotoFile?.toPart("intEditPhoto")
            )

            if (res.isSuccessful) {
                Toast.makeText(this@SurveyActivity, "임시저장 완료", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SurveyActivity, "임시저장 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
