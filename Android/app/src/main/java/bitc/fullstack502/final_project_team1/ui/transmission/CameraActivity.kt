package bitc.fullstack502.final_project_team1.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import bitc.fullstack502.final_project_team1.R


class CameraActivity : AppCompatActivity() {

    private lateinit var capturedImageView: ImageView
    private lateinit var photoButton: Button

    private var isFirstPhoto = true // true = 외부, false = 내부
    private var externalPhoto: Bitmap? = null
    private var internalPhoto: Bitmap? = null

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera) // 레이아웃 파일명

        capturedImageView = findViewById(R.id.capturedImageView)
        photoButton = findViewById(R.id.photo_button)

        photoButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    // 카메라 인텐트 실행
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 촬영 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            capturedImageView.setImageBitmap(imageBitmap)

            if (isFirstPhoto) {
                // 외부 사진 편집
                startEditImage(imageBitmap, isExternal = true)
            } else {
                // 내부 사진 편집
                startEditImage(imageBitmap, isExternal = false)
            }
        }
    }

    // 편집 함수 예제
    private fun startEditImage(bitmap: Bitmap, isExternal: Boolean) {
        // 여기서 Crop / Filter / 편집 라이브러리 호출 가능
        // 예제에서는 그대로 저장

        if (isExternal) {
            externalPhoto = bitmap
            Toast.makeText(this, "외부 사진 편집 완료", Toast.LENGTH_SHORT).show()
            isFirstPhoto = false // 다음 사진은 내부
        } else {
            internalPhoto = bitmap
            Toast.makeText(this, "내부 사진 편집 완료", Toast.LENGTH_SHORT).show()
        }

        // 편집 후 ImageView 업데이트 (옵션)
        capturedImageView.setImageBitmap(bitmap)
    }
}
