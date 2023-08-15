package info.onesandzeros.qualitycontrol.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.FillHeadItem

class TinyLineGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mav: Double = 0.0
    var lsl: Double = 0.0
    var usl: Double = 0.0
    private var weights: List<Double> = emptyList()

    // List of fill head values, for this example, let's say values range between 0 and 100
    var fillHeadValues: List<FillHeadItem> = emptyList()
        set(value) {
            field = value
            weights = value.map { it.weight ?: 0.0 }
            invalidate() // Redraw the view when values change
        }

    // Define paints for different colors
    private val redPaint = Paint().apply {
        color = resources.getColor(R.color.warning_red)
        style = Paint.Style.FILL
    }

    private val yellowPaint = Paint().apply {
        color = resources.getColor(R.color.warning_yellow)
        style = Paint.Style.FILL
    }

    private val defaultPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (weights.isEmpty()) return

        val actualMaxValue = weights.maxOrNull() ?: 1.0
        val actualMinValue = weights.minOrNull() ?: 0.0

        val widthPerValue = width / (weights.size - 1).toFloat()

        for (i in weights.indices) {
            val x = i * widthPerValue
            val y =
                height - ((weights[i] - actualMinValue) / (actualMaxValue - actualMinValue) * height).toFloat()

            val paint = when {
                weights[i] < mav -> redPaint
                weights[i] < lsl || weights[i] > usl -> yellowPaint
                else -> defaultPaint
            }

            canvas.drawCircle(x, y, 10f, paint) // Draw the dot

            // Draw line to next dot if not the last dot
            if (i != weights.size - 1) {
                val nextX = (i + 1) * widthPerValue
                val nextY =
                    height - ((weights[i + 1] - actualMinValue) / (actualMaxValue - actualMinValue) * height).toFloat()
                canvas.drawLine(x, y, nextX, nextY, defaultPaint)
            }
        }
    }

}
