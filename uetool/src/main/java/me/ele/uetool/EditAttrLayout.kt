package me.ele.uetool

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import me.ele.uetool.base.DimenUtil.dip2px
import me.ele.uetool.base.DimenUtil.px2dip
import me.ele.uetool.base.Element
import kotlin.math.absoluteValue

class EditAttrLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CollectViewsLayout(context, attrs, defStyleAttr) {

    private val moveUnit = dip2px(1f)
    private val lineBorderDistance = dip2px(5f)

    private val areaPaint = Paint().apply {
        isAntiAlias = true
        color = 0x30000000
    }

    private var targetElement: Element? = null
    private var dialog: AttrsDialog? = null
    private var mode: IMode = ShowMode()
    private var lastX = 0f
    private var lastY = 0f
    private var onDragListener: OnDragListener? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        targetElement?.let { element ->
            canvas.drawRect(element.rect, areaPaint)
            mode.onDraw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_UP -> mode.triggerActionUp(event)
            MotionEvent.ACTION_MOVE -> mode.triggerActionMove(event)
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        targetElement = null
        dismissAttrsDialog()
    }

    fun setOnDragListener(listener: OnDragListener) {
        this.onDragListener = listener
    }

    fun dismissAttrsDialog() {
        dialog?.dismiss()
    }

    private inner class MoveMode : IMode {
        override fun onDraw(canvas: Canvas) {
            targetElement?.let { element ->
                val rect = element.rect
                val originRect = element.originRect
                canvas.drawRect(originRect, dashLinePaint)

                element.parentElement?.let { parentElement ->
                    val parentRect = parentElement.rect
                    val x = rect.left + rect.width() / 2
                    val y = rect.top + rect.height() / 2

                    drawLineWithText(canvas, rect.left, y, parentRect.left, y, dip2px(2f))
                    drawLineWithText(canvas, x, rect.top, x, parentRect.top, dip2px(2f))
                    drawLineWithText(canvas, rect.right, y, parentRect.right, y, dip2px(2f))
                    drawLineWithText(canvas, x, rect.bottom, x, parentRect.bottom, dip2px(2f))
                }

                onDragListener?.showOffset(
                    "Offset:\n" +
                            "x -> ${px2dip((rect.left - originRect.left).toFloat(), true)} " +
                            "y -> ${px2dip((rect.top - originRect.top).toFloat(), true)}"
                )
            }
        }

        override fun triggerActionMove(event: MotionEvent) {
            targetElement?.let { element ->
                var changed = false
                val view = element.view?: return
                val diffX = event.x - lastX
                if (diffX.absoluteValue >= moveUnit) {
                    view.translationX += diffX
                    lastX = event.x
                    changed = true
                }
                val diffY = event.y - lastY
                if (diffY.absoluteValue >= moveUnit) {
                    view.translationY += diffY
                    lastY = event.y
                    changed = true
                }
                if (changed) {
                    element.reset()
                    invalidate()
                }
            }
        }

        override fun triggerActionUp(event: MotionEvent) {
            // No action needed
        }
    }

    private inner class ShowMode : IMode {
        override fun onDraw(canvas: Canvas) {
            targetElement?.rect?.let { rect ->
                drawLineWithText(
                    canvas,
                    rect.left,
                    rect.top - lineBorderDistance,
                    rect.right,
                    rect.top - lineBorderDistance
                )
                drawLineWithText(
                    canvas,
                    rect.right + lineBorderDistance,
                    rect.top,
                    rect.right + lineBorderDistance,
                    rect.bottom
                )
            }
        }

        override fun triggerActionMove(event: MotionEvent) {
            // No action needed
        }

        override fun triggerActionUp(event: MotionEvent) {
            getTargetElement(event.x, event.y)?.let { element ->
                targetElement = element
                invalidate()

                if (dialog == null) {
                    dialog = AttrsDialog(context).apply {
                        setAttrDialogCallback(object : AttrsDialog.AttrDialogCallback {
                            override fun enableMove() {
                                mode = MoveMode()
                                dismissAttrsDialog()
                            }

                            override fun showValidViews(position: Int, isChecked: Boolean) {
                                val positionStart = position + 1
                                if (isChecked) {
                                    notifyValidViewItemInserted(
                                        positionStart,
                                        getTargetElements(lastX, lastY),
                                        targetElement
                                    )
                                } else {
                                    notifyItemRangeRemoved(positionStart)
                                }
                            }

                            override fun selectView(element: Element) {
                                targetElement = element
                                dismissAttrsDialog()
                                show(element)
                            }
                        })

                        setOnDismissListener {
                            targetElement?.let {
                                it.reset()
                                invalidate()
                            }
                        }
                    }
                }
                dialog?.show(targetElement)
            }
        }
    }

    interface IMode {
        fun onDraw(canvas: Canvas)
        fun triggerActionMove(event: MotionEvent)
        fun triggerActionUp(event: MotionEvent)
    }

    interface OnDragListener {
        fun showOffset(offsetContent: String)
    }
}