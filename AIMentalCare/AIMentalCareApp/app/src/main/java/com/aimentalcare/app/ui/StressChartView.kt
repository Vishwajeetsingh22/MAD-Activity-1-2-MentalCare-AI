package com.aimentalcare.app.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.aimentalcare.app.R

/**
 * Premium Canvas-based Stress Trend Chart View
 * Features:
 * - Dynamic density-scaled typography (crisp on all screen sizes)
 * - Dark mode theme-adaptive colors
 * - Area gradient fill beneath the curve
 * - Smooth Bezier or line connections
 * - Handles Empty, Single-record, and Multi-day history
 * - Accessible text labels and score indicators
 */
class StressChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var scores: List<Int> = emptyList()
    private var dateLabels: List<String> = emptyList()

    private val density = context.resources.displayMetrics.density
    private val scaledDensity = context.resources.displayMetrics.scaledDensity

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 3.5f * density
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.2f * density
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(6f * density, 6f * density), 0f)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f * scaledDensity
        textAlign = Paint.Align.CENTER
    }

    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f * scaledDensity
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val emptyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f * scaledDensity
        textAlign = Paint.Align.CENTER
    }

    fun setScores(newScores: List<Int>, labels: List<String>? = null) {
        this.scores = newScores
        this.dateLabels = labels ?: emptyList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // Resolve theme colors dynamically for dark-mode support
        val primaryColor = ContextCompat.getColor(context, R.color.primary_green)
        val secondaryTextColor = ContextCompat.getColor(context, R.color.text_subtle)
        val mainTextColor = ContextCompat.getColor(context, R.color.main_text)
        val dividerColor = ContextCompat.getColor(context, R.color.divider_color)
        val cardBgColor = ContextCompat.getColor(context, R.color.card_background)

        linePaint.color = primaryColor
        dotPaint.color = primaryColor
        dotHaloPaint.color = cardBgColor
        gridPaint.color = dividerColor
        textPaint.color = secondaryTextColor
        labelTextPaint.color = mainTextColor
        emptyTextPaint.color = secondaryTextColor

        // 1. EMPTY STATE
        if (scores.isEmpty()) {
            val centerY = h / 2f
            canvas.drawText("Complete a few wellness check-ins", w / 2f, centerY - 10f * density, emptyTextPaint)
            canvas.drawText("to see your personal trends.", w / 2f, centerY + 14f * density, emptyTextPaint)
            return
        }

        val paddingLeft = 36f * density
        val paddingRight = 24f * density
        val paddingTop = 28f * density
        val paddingBottom = 28f * density

        val graphWidth = w - paddingLeft - paddingRight
        val graphHeight = h - paddingTop - paddingBottom
        val baselineY = h - paddingBottom

        // 2. HORIZONTAL GRID GUIDELINES (25%, 50%, 75%, 100%)
        val gridLevels = listOf(25, 50, 75, 100)
        for (lvl in gridLevels) {
            val y = paddingTop + graphHeight * (1f - (lvl / 100f))
            canvas.drawLine(paddingLeft, y, w - paddingRight, y, gridPaint)
        }

        val count = scores.size.coerceAtMost(7)

        // 3. SINGLE RECORD STATE
        if (count == 1) {
            val cx = paddingLeft + graphWidth / 2f
            val score = scores[0].coerceIn(0, 100)
            val cy = paddingTop + graphHeight * (1f - (score / 100f))

            // Soft baseline
            canvas.drawLine(paddingLeft, cy, w - paddingRight, cy, linePaint)

            // Halo and Center Dot
            canvas.drawCircle(cx, cy, 9f * density, dotHaloPaint)
            canvas.drawCircle(cx, cy, 6f * density, dotPaint)

            // Value text
            val scoreText = "$score%"
            canvas.drawText(scoreText, cx, cy - 14f * density, labelTextPaint)

            // Day label
            val dayText = if (dateLabels.isNotEmpty()) dateLabels[0] else "Today"
            canvas.drawText(dayText, cx, baselineY + 16f * density, textPaint)
            canvas.drawText("1 check-in logged • Daily check-ins reveal your trend curve", w / 2f, h - 4f * density, textPaint)
            return
        }

        // 4. MULTI-DAY RECORDS (2 to 7 records)
        val stepX = graphWidth / (count - 1)
        val points = mutableListOf<PointF>()

        for (i in 0 until count) {
            val cx = paddingLeft + i * stepX
            val score = scores[i].coerceIn(0, 100)
            val cy = paddingTop + graphHeight * (1f - (score / 100f))
            points.add(PointF(cx, cy))
        }

        // Create line path
        val path = Path()
        val fillPath = Path()

        path.moveTo(points[0].x, points[0].y)
        fillPath.moveTo(points[0].x, baselineY)
        fillPath.lineTo(points[0].x, points[0].y)

        for (i in 1 until count) {
            val prev = points[i - 1]
            val curr = points[i]
            // Cubic bezier for smooth organic curve
            val cpx1 = (prev.x + curr.x) / 2f
            val cpy1 = prev.y
            val cpx2 = (prev.x + curr.x) / 2f
            val cpy2 = curr.y

            path.cubicTo(cpx1, cpy1, cpx2, cpy2, curr.x, curr.y)
            fillPath.cubicTo(cpx1, cpy1, cpx2, cpy2, curr.x, curr.y)
        }

        fillPath.lineTo(points.last().x, baselineY)
        fillPath.close()

        // Gradient shader under the trend curve
        val gradientAlphaTop = (0.28f * 255).toInt()
        val colorWithAlphaTop = Color.argb(gradientAlphaTop, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))
        val colorWithAlphaBottom = Color.argb(0, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))

        fillPaint.shader = LinearGradient(
            0f, paddingTop,
            0f, baselineY,
            colorWithAlphaTop,
            colorWithAlphaBottom,
            Shader.TileMode.CLAMP
        )

        // Draw area fill
        canvas.drawPath(fillPath, fillPaint)

        // Draw curve stroke
        canvas.drawPath(path, linePaint)

        // Draw points, scores, and day labels
        for (i in points.indices) {
            val p = points[i]

            // Halo for contrast
            canvas.drawCircle(p.x, p.y, 8f * density, dotHaloPaint)
            // Core dot
            canvas.drawCircle(p.x, p.y, 5f * density, dotPaint)

            // Score text above point
            val scoreText = "${scores[i]}%"
            canvas.drawText(scoreText, p.x, p.y - 12f * density, labelTextPaint)

            // Day label below baseline
            val dayText = when {
                i < dateLabels.size && dateLabels[i].isNotEmpty() -> dateLabels[i]
                else -> "D${i + 1}"
            }
            canvas.drawText(dayText, p.x, baselineY + 16f * density, textPaint)
        }
    }
}
