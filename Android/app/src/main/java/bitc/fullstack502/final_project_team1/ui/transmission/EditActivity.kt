package bitc.fullstack502.final_project_team1.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import java.io.File

class EditActivity : AppCompatActivity() {

    private lateinit var editImageView: ImageView
    private lateinit var drawingView: DrawingView
    private lateinit var saveButton: Button
    private lateinit var colorButton: Button
    private lateinit var brushButton: Button
    private lateinit var eraserButton: Button
    private lateinit var clearButton: Button
    private lateinit var shapeButton: Button
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        editImageView = findViewById(R.id.editImageView)
        drawingView = findViewById(R.id.drawingView)
        saveButton = findViewById(R.id.save_button)
        colorButton = findViewById(R.id.colorButton)
        brushButton = findViewById(R.id.brushButton)
        eraserButton = findViewById(R.id.eraserButton)
        clearButton = findViewById(R.id.clearButton)
        shapeButton = findViewById(R.id.shapeButton) // 도형 선택 버튼 추가

        val uriString = intent.getStringExtra("imageUri")
        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    editImageView.setImageBitmap(bitmap)
                    drawingView.setBackgroundBitmap(bitmap)
                } else {
                    Toast.makeText(this, "이미지 로딩 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "이미지 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 색상 선택
        colorButton.setOnClickListener {
            val colors = arrayOf("빨강", "파랑", "초록", "검정")
            val colorValues = arrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.BLACK)
            AlertDialog.Builder(this)
                .setTitle("색상 선택")
                .setItems(colors) { _, which ->
                    drawingView.setColor(colorValues[which])
                    drawingView.enableEraser(false)
                }
                .show()
        }

        // 브러시 두께 선택
        brushButton.setOnClickListener {
            val sizes = arrayOf("4", "8", "12", "16")
            AlertDialog.Builder(this)
                .setTitle("브러시 두께")
                .setItems(sizes) { _, which ->
                    drawingView.setStrokeWidth(sizes[which].toFloat())
                    drawingView.enableEraser(false)
                }
                .show()
        }

        // 도형 선택
        shapeButton.setOnClickListener {
            val shapes = arrayOf("자유곡선", "사각형", "원", "직선")
            AlertDialog.Builder(this)
                .setTitle("도형 선택")
                .setItems(shapes) { _, which ->
                    when (which) {
                        0 -> drawingView.setMode(DrawingView.Mode.FREE)
                        1 -> drawingView.setMode(DrawingView.Mode.RECT)
                        2 -> drawingView.setMode(DrawingView.Mode.CIRCLE)
                        3 -> drawingView.setMode(DrawingView.Mode.LINE)
                    }
                    drawingView.enableEraser(false)
                }
                .show()
        }

        // 지우개
        eraserButton.setOnClickListener {
            drawingView.setMode(DrawingView.Mode.ERASER)
            drawingView.enableEraser(true)
        }

        // 초기화
        clearButton.setOnClickListener { drawingView.clear() }

        // 저장
        saveButton.setOnClickListener {
            val drawnBitmap = drawingView.getBitmap()

            val inputStream = contentResolver.openInputStream(imageUri)
            val backgroundBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (backgroundBitmap != null) {
                val finalBitmap = Bitmap.createBitmap(backgroundBitmap.width, backgroundBitmap.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(finalBitmap)
                canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)
                canvas.drawBitmap(drawnBitmap, 0f, 0f, null)

                val file = File(cacheDir, "edited_image.png")
                file.outputStream().use {
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                val resultIntent = Intent()
                resultIntent.putExtra("editedImageUri", Uri.fromFile(file).toString())
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "배경 이미지 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
