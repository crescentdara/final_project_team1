package bitc.fullstack502.final_project_team1.ui.transmission

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R

class PhotoEditActivity : AppCompatActivity() {

    companion object {
        lateinit var photoBitmap: Bitmap
    }

    private lateinit var photoTypeText: TextView
    private lateinit var photoPreview: ImageView
    private lateinit var btnSaveEdit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)

        photoTypeText = findViewById(R.id.photo_type_text)
        photoPreview = findViewById(R.id.photo_preview)
        btnSaveEdit = findViewById(R.id.btn_save_edit)

        // 전달받은 사진 타입 (EXTERNAL / INTERNAL)
        val photoType = intent.getStringExtra("photo_type") ?: "UNKNOWN"
        photoTypeText.text = when(photoType) {
            "EXTERNAL" -> "외부 사진 편집"
            "INTERNAL" -> "내부 사진 편집"
            else -> "사진 편집"
        }

        // 이미지 표시
        photoPreview.setImageBitmap(photoBitmap)

        // 편집 완료 버튼
        btnSaveEdit.setOnClickListener {
            // TODO: 편집 저장 로직 추가 가능 (현재는 그냥 종료)
            finish()
        }
    }
}
