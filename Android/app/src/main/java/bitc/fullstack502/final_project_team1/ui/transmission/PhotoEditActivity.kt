package bitc.fullstack502.final_project_team1.ui.transmission

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R

class PhotoEditActivity : AppCompatActivity() {

    companion object { var photoLabel: String = "사진 편집" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit) // 둘째 XML 파일명

        val photoTypeText: TextView = findViewById(R.id.photo_type_text)
        val photoPreview: ImageView = findViewById(R.id.photo_preview)
        val btnSaveEdit: Button = findViewById(R.id.btn_save_edit)

        photoTypeText.text = photoLabel
        // 필요 시 photoPreview.setImageBitmap(...) 호출해서 외부에서 넘긴 비트맵 보여주기

        btnSaveEdit.setOnClickListener { finish() }
    }
}
