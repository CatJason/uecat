package me.ele.uetool

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import me.ele.uetool.base.DimenUtil.dip2px
import me.ele.uetool.base.DimenUtil.getScreenHeight
import me.ele.uetool.base.DimenUtil.getScreenWidth

class GriddingLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        val LINE_INTERVAL = dip2px(5f)
    }

    private val screenWidth = getScreenWidth()
    private val screenHeight = getScreenHeight()

    private val paint = Paint().apply {
        isAntiAlias = true
        color = 0x30000000
        strokeWidth = 1f
    }

    private var bindActivity: Activity? = UETool.getTargetActivity()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var startX = 0
        while (startX < screenWidth) {
            canvas.drawLine(startX.toFloat(), 0f, startX.toFloat(), screenHeight.toFloat(), paint)
            startX += LINE_INTERVAL
        }

        var startY = 0
        while (startY < screenHeight) {
            canvas.drawLine(0f, startY.toFloat(), screenWidth.toFloat(), startY.toFloat(), paint)
            startY += LINE_INTERVAL
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        bindActivity?.dispatchTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bindActivity = null
    }
}