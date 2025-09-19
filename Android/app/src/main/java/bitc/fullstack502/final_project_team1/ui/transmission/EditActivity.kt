package bitc.fullstack502.final_project_team1.ui.transmission

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import java.io.File

class EditActivity : AppCompatActivity() {

    private lateinit var bgView: ImageView
    private lateinit var drawingView: DrawingView

    private var baseBitmap: Bitmap? = null  // 원본(배경) 비트맵

    companion object {
        const val EXTRA_IMAGE_URI = "imageUri"
        const val EXTRA_EDITED_IMAGE_URI = "editedImageUri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit) // 첫 XML 파일 이름이 activity_edit.xml 이어야 함

        bgView = findViewById(R.id.editImageView)
        drawingView = findViewById(R.id.drawingView)

        // ── 배경 세팅: SurveyActivity 에서 넘긴 사진 경로
        intent.getStringExtra(EXTRA_IMAGE_URI)?.let { uriStr ->
            val file = File(Uri.parse(uriStr).path!!)
            if (file.exists()) {
                baseBitmap = BitmapFactory.decodeFile(file.absolutePath)
                bgView.setImageBitmap(baseBitmap)
            }
        }

        // ── 팔레트
        findViewById<ImageButton>(R.id.redButton).setOnClickListener { drawingView.setColor(Color.RED) }
        findViewById<ImageButton>(R.id.blueButton).setOnClickListener { drawingView.setColor(Color.BLUE) }
        findViewById<ImageButton>(R.id.greenButton).setOnClickListener { drawingView.setColor(Color.GREEN) }

        // ── 브러시/도형/지우개
        findViewById<ImageButton>(R.id.brushButton).setOnClickListener {
            drawingView.enableEraser(false)
            drawingView.setMode(DrawingView.Mode.FREE)
        }
        // 도형 버튼은 RECT → CIRCLE → LINE 순환
        findViewById<ImageButton>(R.id.shapeButton).setOnClickListener {
            drawingView.enableEraser(false)
            drawingView.setMode(nextShapeMode())
        }
        findViewById<ImageButton>(R.id.eraserButton).setOnClickListener {
            drawingView.enableEraser(true)
        }

        // ── 초기화
        findViewById<ImageButton>(R.id.clearButton).setOnClickListener { drawingView.clearAll() }

        // ── 저장: 배경 + 드로잉 합성 → 캐시에 저장 → URI 반환
        findViewById<ImageButton>(R.id.saveButton).setOnClickListener {
            val bg = baseBitmap ?: return@setOnClickListener
            val stroke = drawingView.getStrokeBitmap()
            val merged = composeFitCenter(bg, stroke, drawingView.width, drawingView.height)

            val outFile = File(cacheDir, "edited_${System.currentTimeMillis()}.jpg")
            outFile.outputStream().use { merged.compress(Bitmap.CompressFormat.JPEG, 95, it) }

            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(EXTRA_EDITED_IMAGE_URI, Uri.fromFile(outFile).toString())
            )
            finish()
        }
    }

    /** fitCenter 규칙으로 배경을 dst(뷰 크기)에 배치하고, 그 위에 stroke 비트맵을 그대로 합성 */
    private fun composeFitCenter(
        base: Bitmap,
        stroke: Bitmap,
        dstW: Int,
        dstH: Int
    ): Bitmap {
        val result = Bitmap.createBitmap(dstW, dstH, Bitmap.Config.ARGB_8888)
        val c = Canvas(result)

        // fitCenter 대상 사각형 계산
        val srcW = base.width.toFloat()
        val srcH = base.height.toFloat()
        val dstRatio = dstW.toFloat() / dstH.toFloat()
        val srcRatio = srcW / srcH

        val drawW: Float
        val drawH: Float
        if (srcRatio > dstRatio) {
            // 가로가 더 긴 경우: 가로 꽉, 세로 여백
            drawW = dstW.toFloat()
            drawH = drawW / srcRatio
        } else {
            // 세로가 더 긴 경우: 세로 꽉, 가로 여백
            drawH = dstH.toFloat()
            drawW = drawH * srcRatio
        }

        val left = (dstW - drawW) / 2f
        val top = (dstH - drawH) / 2f
        val dstRect = RectF(left, top, left + drawW, top + drawH)

        c.drawBitmap(base, null, dstRect, null)
        c.drawBitmap(stroke, 0f, 0f, null)
        return result
    }

    // 사각형 → 원 → 선 → 사각형 순환
    private var shapeIndex = 0
    private fun nextShapeMode(): DrawingView.Mode {
        shapeIndex = (shapeIndex + 1) % 3
        return when (shapeIndex) {
            0 -> DrawingView.Mode.RECT
            1 -> DrawingView.Mode.CIRCLE
            else -> DrawingView.Mode.LINE
        }
    }
}
