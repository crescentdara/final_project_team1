package bitc.fullstack502.final_project_team1.ui.transmission

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import java.io.File
import android.graphics.Bitmap

class EditActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        drawingView = findViewById(R.id.drawingView)
        saveButton = findViewById(R.id.saveButton)

        // SurveyActivity에서 전달한 이미지 URI
        val imageUriString = intent.getStringExtra("imageUri")
        imageUriString?.let {
            val file = File(Uri.parse(it).path!!)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                drawingView.setBackgroundBitmap(bitmap)
            }
        }

        saveButton.setOnClickListener {
            val editedBitmap = drawingView.getBitmap()
            // 임시 파일 저장
            val editedFile = File(cacheDir, "edited_${System.currentTimeMillis()}.jpg")
            editedFile.outputStream().use { out ->
                editedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            val resultIntent = intent
            resultIntent.putExtra("editedImageUri", Uri.fromFile(editedFile).toString())
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
