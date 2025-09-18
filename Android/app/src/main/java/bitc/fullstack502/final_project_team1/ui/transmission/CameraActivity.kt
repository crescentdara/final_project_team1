package bitc.fullstack502.final_project_team1.ui.transmission

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import bitc.fullstack502.final_project_team1.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var capturedImageView: ImageView
    private lateinit var photoButton: ImageButton
    private lateinit var editButton: Button
    private lateinit var photoUri: Uri
    private var photoFile: File? = null

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_EDIT_IMAGE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        capturedImageView = findViewById(R.id.capturedImageView)
        photoButton = findViewById(R.id.photo_button)
        editButton = findViewById(R.id.btn_edit_photo)

        // 사진 촬영 버튼
        photoButton.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                dispatchTakePictureIntent()
            }
        }

        // 사진 편집 버튼
        editButton.setOnClickListener {
            openEditActivity()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "이미지 파일 생성 실패", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.let {
            photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun openEditActivity() {
        if (photoFile != null) {
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("imageUri", photoUri.toString())
            startActivityForResult(intent, REQUEST_EDIT_IMAGE)
        } else {
            Toast.makeText(this, "먼저 사진을 찍으세요.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                // 찍은 사진 바로 표시
                val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                capturedImageView.setImageBitmap(bitmap)
            } else if (requestCode == REQUEST_EDIT_IMAGE && resultCode == Activity.RESULT_OK) {
                // 편집 후 표시
                val editedUriString = data?.getStringExtra("editedImageUri")
                editedUriString?.let {
                    val editedBitmap = BitmapFactory.decodeFile(File(Uri.parse(it).path!!).absolutePath)
                    capturedImageView.setImageBitmap(editedBitmap)
                    // 최신 URI 업데이트
                    photoUri = Uri.parse(it)
                    photoFile = File(photoUri.path!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "이미지 불러오기 실패", Toast.LENGTH_SHORT).show()
        }
    }
}
