package bitc.fullstack502.final_project_team1.ui.transmission

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    enum class Mode { FREE, RECT, CIRCLE, LINE, ERASER }

    data class DrawItem(
        val shapeType: Mode,
        val path: Path? = null,
        val rect: RectF? = null,
        val paint: Paint
    )

    private var currentMode = Mode.FREE
    private var drawPath = Path()
    private var startX = 0f
    private var startY = 0f
    private var tempRect: RectF? = null

    private var drawPaint = Paint()
    private var currentPaint = Paint(drawPaint)

    private val items = mutableListOf<DrawItem>()
    private var backgroundBitmap: Bitmap? = null

    init {
        drawPaint.apply {
            color = Color.RED
            isAntiAlias = true
            strokeWidth = 8f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        currentPaint = Paint(drawPaint)
    }

    fun setBackgroundBitmap(bitmap: Bitmap) {
        backgroundBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        invalidate()
    }

    fun setMode(mode: Mode) { currentMode = mode }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        backgroundBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        for (item in items) {
            when (item.shapeType) {
                Mode.FREE -> canvas.drawPath(item.path!!, item.paint)
                Mode.RECT -> canvas.drawRect(item.rect!!, item.paint)
                Mode.CIRCLE -> canvas.drawOval(item.rect!!, item.paint)
                Mode.LINE -> canvas.drawLine(
                    item.rect!!.left, item.rect!!.top,
                    item.rect!!.right, item.rect!!.bottom,
                    item.paint
                )
                Mode.ERASER -> canvas.drawPath(item.path!!, item.paint)
            }
        }
        // 드래그 중일 때 미리보기 도형
        tempRect?.let {
            when (currentMode) {
                Mode.RECT -> canvas.drawRect(it, currentPaint)
                Mode.CIRCLE -> canvas.drawOval(it, currentPaint)
                Mode.LINE -> canvas.drawLine(it.left, it.top, it.right, it.bottom, currentPaint)
                else -> {}
            }
        }
        if (currentMode == Mode.FREE || currentMode == Mode.ERASER) {
            canvas.drawPath(drawPath, currentPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                if (currentMode == Mode.FREE || currentMode == Mode.ERASER) {
                    drawPath.moveTo(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (currentMode) {
                    Mode.FREE, Mode.ERASER -> drawPath.lineTo(x, y)
                    Mode.RECT, Mode.CIRCLE, Mode.LINE -> tempRect = RectF(startX, startY, x, y)
                }
            }
            MotionEvent.ACTION_UP -> {
                when (currentMode) {
                    Mode.FREE, Mode.ERASER -> {
                        items.add(DrawItem(currentMode, Path(drawPath), null, Paint(currentPaint)))
                        drawPath.reset()
                    }
                    Mode.RECT, Mode.CIRCLE, Mode.LINE -> {
                        items.add(DrawItem(currentMode, null, RectF(startX, startY, x, y), Paint(currentPaint)))
                        tempRect = null
                    }
                }
            }
        }
        invalidate()
        return true
    }

    fun setColor(color: Int) { currentPaint.color = color }

    fun setStrokeWidth(width: Float) { currentPaint.strokeWidth = width }

    fun enableEraser(enable: Boolean) {
        if (enable) {
            currentMode = Mode.ERASER
            currentPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            currentPaint.xfermode = null
        }
    }

    fun clear() {
        items.clear()
        drawPath.reset()
        tempRect = null
        invalidate()
    }

    fun getBitmap(): Bitmap {
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        draw(c)
        return b
    }
}
