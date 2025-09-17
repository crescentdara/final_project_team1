package bitc.fullstack502.final_project_team1.ui.surveyList

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.location.Location
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.network.dto.SurveySite
import kotlin.math.abs

class AROverlayView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var sites: List<SurveySite> = emptyList()
    var userLocation: Location? = null
    var azimuthDeg: Float = 0f
    var pitchDeg: Float = 0f
    var horizontalFov: Float = 60f
    var maxRenderDistanceM = 2000f

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        setShadowLayer(6f, 0f, 0f, Color.BLACK)
    }
    private val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 0, 0, 0)
    }

    // ✅ 마커 아이콘 준비
    private val markerBitmap: Bitmap = run {
        val d = ContextCompat.getDrawable(ctx, R.drawable.ic_marker_red)!!
        val bmp = Bitmap.createBitmap(
            d.intrinsicWidth,
            d.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(bmp)
        d.setBounds(0, 0, c.width, c.height)
        d.draw(c)
        bmp
    }
    private val markerW = markerBitmap.width.toFloat()
    private val markerH = markerBitmap.height.toFloat()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val me = userLocation ?: return
        val cx = width / 2f
        val cy = height / 2f

        for (s in sites) {
            val target = Location("").apply {
                latitude = s.lat
                longitude = s.lng
            }
            val dist = me.distanceTo(target)
            if (dist > maxRenderDistanceM) continue

            val bearingTo = me.bearingTo(target)
            val delta = normalizeDeg(bearingTo - azimuthDeg)
            if (abs(delta) > horizontalFov / 2f) continue

            // 📍 마커 좌표
            val x = cx + (delta / (horizontalFov / 2f)) * (width / 2f)
            val y = cy + (-pitchDeg / 45f) * (height * 0.15f) -
                    (dist / maxRenderDistanceM) * (height * 0.25f)

            // ✅ 마커 이미지 그리기 (핀 아래쪽이 기준점)
            canvas.drawBitmap(markerBitmap, x - markerW / 2f, y - markerH, null)

            // ✅ 말풍선 스타일 텍스트
            val label = "${s.name} · ${if (dist < 1000) "${dist.toInt()}m" else String.format("%.1fkm", dist / 1000)}"
            val w = textPaint.measureText(label)
            val rect = RectF(
                x - w / 2f - 16f,
                y - markerH - 80f,
                x + w / 2f + 16f,
                y - markerH - 28f
            )
            canvas.drawRoundRect(rect, 20f, 20f, chipPaint)
            canvas.drawText(label, x - w / 2f, y - markerH - 40f, textPaint)
        }
    }

    fun updatePose(azimuth: Float, pitch: Float) {
        azimuthDeg = (azimuth + 360f) % 360f
        pitchDeg = pitch
        invalidate()
    }

    fun updateUserLocation(loc: Location) {
        userLocation = loc
        invalidate()
    }

    private fun normalizeDeg(d: Float): Float {
        var v = (d + 540f) % 360f - 180f
        if (v < -180f) v += 360f
        return v
    }
}
