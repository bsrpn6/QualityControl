package info.onesandzeros.qualitycontrol.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class BoundingBoxOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var boundingBoxes: List<Rect>? = null
    private var cameraPreviewWidth: Int = 0
    private var cameraPreviewHeight: Int = 0

    fun setBoundingBoxes(boundingBox: List<Rect>) {
        this.boundingBoxes = boundingBox
        invalidate()
    }

    fun setCameraPreviewSize(width: Int, height: Int) {
        cameraPreviewWidth = width
        cameraPreviewHeight = height
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        boundingBoxes?.forEach { boundingBox ->
            val rect = RectF(
                boundingBox.left * scaleX,
                boundingBox.top * scaleY,
                boundingBox.right * scaleX,
                boundingBox.bottom * scaleY
            )
            canvas.drawRect(rect, paint)
        }
    }

}
