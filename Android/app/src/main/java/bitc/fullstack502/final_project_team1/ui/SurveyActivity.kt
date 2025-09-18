package bitc.fullstack502.final_project_team1.ui.transmission

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import bitc.fullstack502.final_project_team1.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SurveyActivity : AppCompatActivity() {

    private lateinit var stages: List<LinearLayout>
    private var currentStage = 0

    private lateinit var nextButton: Button
    private lateinit var tabButtons: Map<Button, Int>

    // 사진 관련
    private lateinit var capturedImageView: ImageView
    private lateinit var photoButton: ImageButton
    private var photoFile: File? = null
    private var photoTaken = false
    private val REQUEST_IMAGE_CAPTURE = 100

    // 편집 관련
    private lateinit var drawingView: DrawingView
    private lateinit var btnSaveEdit: Button
    private lateinit var btnUndo: Button
    private lateinit var btnRedo: Button
    private lateinit var btnEraser: ToggleButton
    private lateinit var btnModeFree: Button
    private lateinit var btnModeRect: Button
    private lateinit var btnModeCircle: Button
    private lateinit var btnModeLine: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)

        // 단계별 레이아웃
        stages = listOf(
            findViewById(R.id.stage_investigation),
            findViewById(R.id.stage_purpose),
            findViewById(R.id.stage_idle_ratio),
            findViewById(R.id.stage_safety),
            findViewById(R.id.stage_internal_status),
            findViewById(R.id.stage_external_status),
            findViewById(R.id.stage_photo),
            findViewById(R.id.stage_photo_edit)
        )

        val backButton = findViewById<Button>(R.id.back_button)
        nextButton = findViewById(R.id.next_button)

        tabButtons = mapOf(
            findViewById<Button>(R.id.btn_investigation) to 0,
            findViewById<Button>(R.id.btn_purpose) to 1,
            findViewById<Button>(R.id.btn_idle_ratio) to 2,
            findViewById<Button>(R.id.btn_safety) to 3,
            findViewById<Button>(R.id.btn_internal_status) to 4,
            findViewById<Button>(R.id.btn_external_status) to 5,
            findViewById<Button>(R.id.photo_button_tab) to 6,
            findViewById<Button>(R.id.photo_edit_button) to 7
        )

        tabButtons.forEach { (button, index) ->
            button.setOnClickListener { showStage(index) }
        }

        backButton.setOnClickListener {
            if (currentStage > 0) showStage(currentStage - 1)
        }

        nextButton.setOnClickListener {
            if (currentStage < stages.size - 1) showStage(currentStage + 1)
            else Toast.makeText(this, "모든 단계 완료!", Toast.LENGTH_SHORT).show()
        }

        // 사진 단계
        capturedImageView = findViewById(R.id.capturedImageView)
        photoButton = findViewById(R.id.photo_button)
        photoButton.setOnClickListener { dispatchTakePictureIntent() }

        // 편집 단계
        drawingView = findViewById(R.id.drawingView)
        btnSaveEdit = findViewById(R.id.btn_save_edit)
        btnUndo = findViewById(R.id.btn_undo)
        btnRedo = findViewById(R.id.btn_redo)
        btnEraser = findViewById(R.id.btn_eraser)
        btnModeFree = findViewById(R.id.btn_mode_free)
        btnModeRect = findViewById(R.id.btn_mode_rect)
        btnModeCircle = findViewById(R.id.btn_mode_circle)
        btnModeLine = findViewById(R.id.btn_mode_line)

        btnSaveEdit.setOnClickListener { saveEditedImage() }
        btnUndo.setOnClickListener { drawingView.undo() }
        btnRedo.setOnClickListener { drawingView.redo() }
        btnEraser.setOnCheckedChangeListener { _, checked -> drawingView.enableEraser(checked) }
        btnModeFree.setOnClickListener { drawingView.setMode(DrawingView.Mode.FREE) }
        btnModeRect.setOnClickListener { drawingView.setMode(DrawingView.Mode.RECT) }
        btnModeCircle.setOnClickListener { drawingView.setMode(DrawingView.Mode.CIRCLE) }
        btnModeLine.setOnClickListener { drawingView.setMode(DrawingView.Mode.LINE) }

        showStage(0)
    }

    private fun showStage(index: Int) {
        stages.forEach { it.visibility = View.GONE }
        currentStage = index
        stages[currentStage].visibility = View.VISIBLE

        if (currentStage == 7 && photoFile != null) {
            val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
            drawingView.setBackgroundBitmap(bitmap)
        }

        updateTabs()
        updateNextButton()
        setRadioListeners()
    }

    private fun updateTabs() {
        tabButtons.forEach { (button, index) ->
            button.setBackgroundColor(if (index == currentStage) 0xFF6200EE.toInt() else 0xFFD3D3D3.toInt())
            button.setTextColor(if (index == currentStage) 0xFFFFFFFF.toInt() else 0xFF000000.toInt())
        }
    }

    private fun updateNextButton() {
        val hasChecked = if (currentStage == 6) photoTaken
        else stages[currentStage].findViewsByType(RadioGroup::class.java).all { it.checkedRadioButtonId != -1 }

        nextButton.isEnabled = hasChecked
        nextButton.alpha = if (hasChecked) 1.0f else 0.5f
    }

    private fun setRadioListeners() {
        stages[currentStage].findViewsByType(RadioGroup::class.java).forEach { rg ->
            rg.setOnCheckedChangeListener { _, _ -> updateNextButton() }
        }
    }

    private fun <T> View.findViewsByType(clazz: Class<T>): List<T> {
        val result = mutableListOf<T>()
        val stack = ArrayDeque<View>()
        stack.add(this)
        while (stack.isNotEmpty()) {
            val view = stack.removeFirst()
            if (clazz.isInstance(view)) result.add(clazz.cast(view)!!)
            if (view is ViewGroup) for (i in 0 until view.childCount) stack.add(view.getChildAt(i))
        }
        return result
    }

    // 사진 촬영
    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = try { createImageFile() } catch (e: Exception) { e.printStackTrace(); return }

        photoFile?.let {
            val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
            capturedImageView.setImageBitmap(bitmap)
            photoTaken = true
            updateNextButton()
        }
    }

    private fun saveEditedImage() {
        drawingView.mergePathToBitmap() // Path를 Bitmap에 반영
        val editedBitmap = drawingView.getBitmap()
        try {
            FileOutputStream(photoFile!!).use { fos ->
                editedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }
            capturedImageView.setImageBitmap(editedBitmap)
            Toast.makeText(this, "편집된 이미지 저장 완료", Toast.LENGTH_SHORT).show()
            showStage(6)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "편집 저장 실패", Toast.LENGTH_SHORT).show()
        }
    }
}
