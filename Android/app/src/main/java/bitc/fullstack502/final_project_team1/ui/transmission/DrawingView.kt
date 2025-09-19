package bitc.fullstack502.final_project_team1.ui.transmission

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    enum class Mode { FREE, RECT, CIRCLE, LINE }

    private var drawPath: Path? = null
    private var drawPaint: Paint = Paint()
    private var canvasPaint: Paint = Paint(Paint.DITHER_FLAG)
    private lateinit var drawCanvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private var mode: Mode = Mode.FREE

    private val paths = Stack<Pair<Path, Paint>>()
    private val undonePaths = Stack<Pair<Path, Paint>>()

    private var startX = 0f
    private var startY = 0f

    init {
        drawPaint.color = Color.BLACK
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = 8f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
    }

    fun setMode(m: Mode) { mode = m }
    fun setColor(color: Int) { drawPaint.color = color }

    fun enableEraser(enable: Boolean) {
        if (enable) {
            drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            drawPaint.strokeWidth = 50f
        } else {
            drawPaint.xfermode = null
            drawPaint.strokeWidth = 8f
        }
    }

    fun undo() { if (paths.isNotEmpty()) { undonePaths.push(paths.pop()); invalidate() } }
    fun redo() { if (undonePaths.isNotEmpty()) { paths.push(undonePaths.pop()); invalidate() } }

    fun setBackgroundBitmap(bitmap: Bitmap) {
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        drawCanvas = Canvas(canvasBitmap)
        invalidate()
    }

    fun getBitmap(): Bitmap = canvasBitmap

    fun mergePathToBitmap() {
        drawPath?.let { drawCanvas.drawPath(it, drawPaint) }
        for ((p, paint) in paths) drawCanvas.drawPath(p, paint)
        paths.clear()
        drawPath = null
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!::canvasBitmap.isInitialized) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(canvasBitmap)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        for ((path, paint) in paths) canvas.drawPath(path, paint)
        drawPath?.let { canvas.drawPath(it, drawPaint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                drawPath = Path().apply { moveTo(x, y) }
                undonePaths.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath = Path()
                when (mode) {
                    Mode.FREE -> drawPath?.lineTo(x, y)
                    Mode.RECT -> drawPath?.addRect(startX, startY, x, y, Path.Direction.CW)
                    Mode.CIRCLE -> {
                        val radius = Math.hypot((x - startX).toDouble(), (y - startY).toDouble()).toFloat()
                        drawPath?.addCircle(startX, startY, radius, Path.Direction.CW)
                    }
                    Mode.LINE -> { drawPath?.moveTo(startX, startY); drawPath?.lineTo(x, y) }
                }
            }
            MotionEvent.ACTION_UP -> {
                drawPath?.let { paths.push(it to Paint(drawPaint)) }
                drawPath = null
            }
        }
        invalidate()
        return true
    }
}
