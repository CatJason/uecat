package me.ele.uetool

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import me.ele.uetool.base.DimenUtil.dip2px
import me.ele.uetool.base.Element

class RelativePositionLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CollectViewsLayout(context, attrs, defStyleAttr) {

    private val elementsNum = 2
    private val areaPaint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = dip2px(1f).toFloat()
    }

    private var relativeElements = arrayOfNulls<Element>(elementsNum)
    private var searchCount = 0

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                getTargetElement(event.x, event.y)?.let { element ->
                    relativeElements[searchCount % elementsNum] = element
                    searchCount++
                    invalidate()
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val doubleNotNull = relativeElements.all { it != null }

        relativeElements.forEach { element ->
            element?.let {
                val rect = it.rect
                canvas.apply {
                    drawLine(0f, rect.top.toFloat(), screenWidth.toFloat(), rect.top.toFloat(), dashLinePaint)
                    drawLine(0f, rect.bottom.toFloat(), screenWidth.toFloat(), rect.bottom.toFloat(), dashLinePaint)
                    drawLine(rect.left.toFloat(), 0f, rect.left.toFloat(), screenHeight.toFloat(), dashLinePaint)
                    drawLine(rect.right.toFloat(), 0f, rect.right.toFloat(), screenHeight.toFloat(), dashLinePaint)
                    drawRect(rect, areaPaint)
                }
            }
        }

        if (doubleNotNull) {
            val firstRect = relativeElements[searchCount % elementsNum]?.rect ?: return
            val secondRect = relativeElements[(searchCount - 1) % elementsNum]?.rect ?: return

            when {
                secondRect.top > firstRect.bottom -> {
                    val x = secondRect.left + secondRect.width() / 2
                    drawLineWithText(canvas, x, firstRect.bottom, x, secondRect.top)
                }
                firstRect.top > secondRect.bottom -> {
                    val x = secondRect.left + secondRect.width() / 2
                    drawLineWithText(canvas, x, secondRect.bottom, x, firstRect.top)
                }
                secondRect.left > firstRect.right -> {
                    val y = secondRect.top + secondRect.height() / 2
                    drawLineWithText(canvas, secondRect.left, y, firstRect.right, y)
                }
                firstRect.left > secondRect.right -> {
                    val y = secondRect.top + secondRect.height() / 2
                    drawLineWithText(canvas, secondRect.right, y, firstRect.left, y)
                }
            }

            drawNestedAreaLine(canvas, firstRect, secondRect)
            drawNestedAreaLine(canvas, secondRect, firstRect)
        }
    }

    private fun drawNestedAreaLine(canvas: Canvas, firstRect: Rect, secondRect: Rect) {
        if (secondRect.left >= firstRect.left &&
            secondRect.right <= firstRect.right &&
            secondRect.top >= firstRect.top &&
            secondRect.bottom <= firstRect.bottom) {

            drawLineWithText(
                canvas,
                secondRect.left,
                secondRect.top + secondRect.height() / 2,
                firstRect.left,
                secondRect.top + secondRect.height() / 2
            )

            drawLineWithText(
                canvas,
                secondRect.right,
                secondRect.top + secondRect.height() / 2,
                firstRect.right,
                secondRect.top + secondRect.height() / 2
            )

            drawLineWithText(
                canvas,
                secondRect.left + secondRect.width() / 2,
                secondRect.top,
                secondRect.left + secondRect.width() / 2,
                firstRect.top
            )

            drawLineWithText(
                canvas,
                secondRect.left + secondRect.width() / 2,
                secondRect.bottom,
                secondRect.left + secondRect.width() / 2,
                firstRect.bottom
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        relativeElements = arrayOfNulls(elementsNum)
    }

    override fun drawLineWithText(canvas: Canvas, startX: Int, startY: Int, endX: Int, endY: Int) {
        drawLineWithText(canvas, startX, startY, endX, endY, dip2px(2f))
    }
}