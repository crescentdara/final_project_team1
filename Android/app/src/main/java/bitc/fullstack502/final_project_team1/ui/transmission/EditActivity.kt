package bitc.fullstack502.final_project_team1.ui.transmission

import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import java.io.File

class EditActivity : AppCompatActivity() {

    private lateinit var editImageView: ImageView
    private lateinit var drawingView: DrawingView
    private lateinit var redButton: ImageButton
    private lateinit var blueButton: ImageButton
    private lateinit var greenButton: ImageButton
    private lateinit var brushButton: ImageButton
    private lateinit var eraserButton: ImageButton
    private lateinit var clearButton: ImageButton
    private lateinit var shapeButton: ImageButton
    private lateinit var saveButton: ImageButton
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // 뷰 초기화
        editImageView = findViewById(R.id.editImageView)
        drawingView = findViewById(R.id.drawingView)
        redButton = findViewById(R.id.redButton)
        blueButton = findViewById(R.id.blueButton)
        greenButton = findViewById(R.id.greenButton)
        brushButton = findViewById(R.id.brushButton)
        eraserButton = findViewById(R.id.eraserButton)
        clearButton = findViewById(R.id.clearButton)
        shapeButton = findViewById(R.id.shapeButton)
        saveButton = findViewById(R.id.saveButton)

        // 이미지 로딩
        intent.getStringExtra("imageUri")?.let {
            imageUri = Uri.parse(it)
            try {
                contentResolver.openInputStream(imageUri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        editImageView.setImageBitmap(bitmap)
                        drawingView.setBackgroundBitmap(bitmap)
                    } else {
                        Toast.makeText(this, "이미지 로딩 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "이미지 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 색상 선택
        redButton.setOnClickListener { drawingView.setColor(Color.RED); drawingView.enableEraser(false) }
        blueButton.setOnClickListener { drawingView.setColor(Color.BLUE); drawingView.enableEraser(false) }
        greenButton.setOnClickListener { drawingView.setColor(Color.GREEN); drawingView.enableEraser(false) }

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

            contentResolver.openInputStream(imageUri)?.use { inputStream ->
                val backgroundBitmap = BitmapFactory.decodeStream(inputStream)
                if (backgroundBitmap != null) {
                    val finalBitmap = Bitmap.createBitmap(
                        backgroundBitmap.width,
                        backgroundBitmap.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(finalBitmap)
                    canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)
                    canvas.drawBitmap(drawnBitmap, 0f, 0f, null)

                    val file = File(cacheDir, "edited_image.png")
                    file.outputStream().use { finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

                    val resultIntent = Intent().apply {
                        putExtra("editedImageUri", Uri.fromFile(file).toString())
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this, "배경 이미지 로딩 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
