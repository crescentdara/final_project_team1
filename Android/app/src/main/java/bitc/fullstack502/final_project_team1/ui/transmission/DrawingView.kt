package bitc.fullstack502.final_project_team1.ui.transmission

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.Stack

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    enum class Mode { FREE, RECT, CIRCLE, LINE }

    private var tempPath: Path? = null
    private val paths = Stack<Pair<Path, Paint>>()
    private val undone = Stack<Pair<Path, Paint>>()

    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    // 투명한 드로잉 버퍼 (뷰 크기와 동일)
    private lateinit var strokeBitmap: Bitmap
    private lateinit var strokeCanvas: Canvas
    private val bmpPaint = Paint(Paint.DITHER_FLAG)

    private var mode: Mode = Mode.FREE
    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var lastY = 0f

    fun setMode(m: Mode) { mode = m }
    fun setColor(color: Int) { if (drawPaint.xfermode == null) drawPaint.color = color }
    fun enableEraser(enable: Boolean) {
        if (enable) { drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR); drawPaint.strokeWidth = 50f }
        else { drawPaint.xfermode = null; drawPaint.strokeWidth = 8f }
    }

    fun clearAll() {
        if (::strokeBitmap.isInitialized) {
            strokeBitmap.eraseColor(Color.TRANSPARENT)
            paths.clear()
            undone.clear()
            tempPath = null
            invalidate()
        }
    }

    fun undo() { if (paths.isNotEmpty()) { undone.push(paths.pop()); invalidate() } }
    fun redo() { if (undone.isNotEmpty()) { paths.push(undone.pop()); invalidate() } }

    /** 현재 드로잉 레이어(투명 포함)를 비트맵으로 반환 */
    fun getStrokeBitmap(): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(result)
        // 누적된 path들
        for ((p, paint) in paths) c.drawPath(p, paint)
        // 진행 중 path
        tempPath?.let { c.drawPath(it, drawPaint) }
        return result
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return
        strokeBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        strokeCanvas = Canvas(strokeBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        // 이미지는 Activity 쪽 editImageView에 그려짐. 여기선 드로잉만.
        for ((p, paint) in paths) canvas.drawPath(p, paint)
        tempPath?.let { canvas.drawPath(it, drawPaint) }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x; val y = e.y
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x; startY = y
                lastX = x; lastY = y
                tempPath = Path().apply { moveTo(x, y) }
                undone.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                when (mode) {
                    Mode.FREE -> {
                        tempPath?.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f)
                        lastX = x; lastY = y
                    }
                    Mode.RECT -> tempPath = Path().apply { addRect(startX, startY, x, y, Path.Direction.CW) }
                    Mode.CIRCLE -> {
                        val r = Math.hypot((x - startX).toDouble(), (y - startY).toDouble()).toFloat()
                        tempPath = Path().apply { addCircle(startX, startY, r, Path.Direction.CW) }
                    }
                    Mode.LINE -> tempPath = Path().apply { moveTo(startX, startY); lineTo(x, y) }
                }
            }
            MotionEvent.ACTION_UP -> { tempPath?.let { paths.push(it to Paint(drawPaint)) }; tempPath = null }
        }
        invalidate()
        return true
    }
}
