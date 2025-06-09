package me.ele.uetool

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import me.ele.uetool.base.dip2px

class RegionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var rectF: RectF? = null
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.YELLOW
        strokeWidth = dip2px(2f).toFloat()
        style = Paint.Style.STROKE
    }

    fun drawRegion(rectF: RectF) {
        this.rectF = rectF
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rectF?.let { canvas.drawRect(it, paint) }
    }
}