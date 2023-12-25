package com.jakewharton.scalpel

import android.content.Context
import android.content.res.Resources
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import java.util.ArrayDeque
import java.util.BitSet
import java.util.Deque

/**
 * Renders your view hierarchy as an interactive 3D visualization of layers.
 *
 *
 * Interactions supported:
 *
 *  * Single touch: controls the rotation of the model.
 *  * Two finger vertical pinch: Adjust zoom.
 *  * Two finger horizontal pinch: Adjust layer spacing.
 *
 */
class ScalpelFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private class LayeredView {
        var view: View? = null
        var layer = 0
        operator fun set(view: View?, layer: Int) {
            this.view = view
            this.layer = layer
        }

        fun clear() {
            view = null
            layer = -1
        }
    }

    private val viewBoundsRect = Rect()
    private val viewBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val camera = Camera()
    private val matrix = Matrix()
    private val location = IntArray(2)
    private val visibilities = BitSet(CHILD_COUNT_ESTIMATION)
    private val idNames = SparseArray<String?>()
    private val layeredViewQueue: Deque<LayeredView> = ArrayDeque()
    private val layeredViewPool: Pool<LayeredView> = object : Pool<LayeredView>(
        CHILD_COUNT_ESTIMATION
    ) {
        override fun newObject(): LayeredView {
            return LayeredView()
        }
    }
    private val res: Resources
    private val density: Float
    private val slop: Float
    private val textOffset: Float
    private val textSize: Float
    private var enabled = false
    /** Returns true when view layers draw their contents.  */
    var isDrawingViews = true
        private set
    /** Returns true when view layers draw their IDs.  */
    var isDrawingIds = false
        private set
    private var pointerOne = MotionEvent.INVALID_POINTER_ID
    private var lastOneX = 0f
    private var lastOneY = 0f
    private var pointerTwo = MotionEvent.INVALID_POINTER_ID
    private var lastTwoX = 0f
    private var lastTwoY = 0f
    private var multiTouchTracking = TRACKING_UNKNOWN
    private var rotationY = ROTATION_DEFAULT_Y.toFloat()
    private var rotationX = ROTATION_DEFAULT_X.toFloat()
    private var zoom = ZOOM_DEFAULT
    private var spacing = SPACING_DEFAULT.toFloat()
    private var lastInvalidateTime = 0L
    /** Get the view border chrome color.  */
    var chromeColor = 0
        /** Set the view border chrome color.  */
        set(color) {
            if (chromeColor != color) {
                viewBorderPaint.color = color
                field = color
                invalidate()
            }
        }
    /** Get the view border chrome shadow color.  */
    var chromeShadowColor = 0
        /** Set the view border chrome shadow color.  */
        set(color) {
            if (chromeShadowColor != color) {
                viewBorderPaint.setShadowLayer(1f, -1f, 1f, color)
                field = color
                invalidate()
            }
        }

    init {
        res = context.resources
        density = context.resources.displayMetrics.density
        slop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        textSize = TEXT_SIZE_DP * density
        textOffset = TEXT_OFFSET_DP * density
        chromeColor = CHROME_COLOR
        viewBorderPaint.style = Paint.Style.STROKE
        viewBorderPaint.textSize = textSize
        chromeShadowColor = CHROME_SHADOW_COLOR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewBorderPaint.typeface =
                Typeface.create("sans-serif-condensed", Typeface.NORMAL)
        }
    }

    var isLayerInteractionEnabled: Boolean
        /** Returns true when 3D view layer interaction is enabled.  */
        get() = enabled
        /** Set whether or not the 3D view layer interaction is enabled.  */
        set(enabled) {
            if (this.enabled != enabled) {
                this.enabled = enabled
                setWillNotDraw(!enabled)
                invalidate()
            }
        }

    /** Set whether the view layers draw their IDs.  */
    fun setDrawIds(drawIds: Boolean) {
        if (isDrawingIds != drawIds) {
            isDrawingIds = drawIds
            invalidate()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return enabled || super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enabled) {
            return super.onTouchEvent(event)
        }
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = if (action == MotionEvent.ACTION_DOWN) 0 else event.actionIndex
                if (pointerOne == MotionEvent.INVALID_POINTER_ID) {
                    pointerOne = event.getPointerId(index)
                    lastOneX = event.getX(index)
                    lastOneY = event.getY(index)
                } else if (pointerTwo == MotionEvent.INVALID_POINTER_ID) {
                    pointerTwo = event.getPointerId(index)
                    lastTwoX = event.getX(index)
                    lastTwoY = event.getY(index)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (pointerTwo == MotionEvent.INVALID_POINTER_ID) {
                    var i = 0
                    val count = event.pointerCount
                    while (i < count) {
                        if (pointerOne == event.getPointerId(i)) {
                            val eventX = event.getX(i)
                            val eventY = event.getY(i)
                            val dx = eventX - lastOneX
                            val dy = eventY - lastOneY
                            val drx = 90 * (dx / width)
                            val dry = 90 * (-dy / height)
                            rotationY = Math.min(
                                Math.max(rotationY + drx, ROTATION_MIN.toFloat()),
                                ROTATION_MAX.toFloat()
                            )
                            rotationX = Math.min(
                                Math.max(rotationX + dry, ROTATION_MIN.toFloat()),
                                ROTATION_MAX.toFloat()
                            )
                            lastOneX = eventX
                            lastOneY = eventY
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastInvalidateTime > INVALIDATE_FREQUENCY_MS) {
                                invalidate()
                                lastInvalidateTime = currentTime
                            }
                        }
                        i++
                    }
                } else {
                    val pointerOneIndex = event.findPointerIndex(pointerOne)
                    val pointerTwoIndex = event.findPointerIndex(pointerTwo)
                    val xOne = event.getX(pointerOneIndex)
                    val yOne = event.getY(pointerOneIndex)
                    val xTwo = event.getX(pointerTwoIndex)
                    val yTwo = event.getY(pointerTwoIndex)
                    val dxOne = xOne - lastOneX
                    val dyOne = yOne - lastOneY
                    val dxTwo = xTwo - lastTwoX
                    val dyTwo = yTwo - lastTwoY
                    if (multiTouchTracking == TRACKING_UNKNOWN) {
                        val adx = Math.abs(dxOne) + Math.abs(dxTwo)
                        val ady = Math.abs(dyOne) + Math.abs(dyTwo)
                        if (adx > slop * 2 || ady > slop * 2) {
                            multiTouchTracking = if (adx > ady) {
                                TRACKING_HORIZONTALLY
                            } else {
                                TRACKING_VERTICALLY
                            }
                        }
                    }
                    if (multiTouchTracking == TRACKING_VERTICALLY) {
                        zoom += if (yOne >= yTwo) {
                            dyOne / height - dyTwo / height
                        } else {
                            dyTwo / height - dyOne / height
                        }
                        zoom = Math.min(Math.max(zoom, ZOOM_MIN), ZOOM_MAX)
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastInvalidateTime > INVALIDATE_FREQUENCY_MS) {
                            invalidate()
                            lastInvalidateTime = currentTime
                        }
                    } else if (multiTouchTracking == TRACKING_HORIZONTALLY) {
                        spacing += if (xOne >= xTwo) {
                            dxOne / width * SPACING_MAX - dxTwo / width * SPACING_MAX
                        } else {
                            dxTwo / width * SPACING_MAX - dxOne / width * SPACING_MAX
                        }
                        spacing = Math.min(
                            Math.max(spacing, SPACING_MIN.toFloat()),
                            SPACING_MAX.toFloat()
                        )
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastInvalidateTime > INVALIDATE_FREQUENCY_MS) {
                            invalidate()
                            lastInvalidateTime = currentTime
                        }
                    }
                    if (multiTouchTracking != TRACKING_UNKNOWN) {
                        lastOneX = xOne
                        lastOneY = yOne
                        lastTwoX = xTwo
                        lastTwoY = yTwo
                    }
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val index = if (action != MotionEvent.ACTION_POINTER_UP) 0 else event.actionIndex
                val pointerId = event.getPointerId(index)
                if (pointerOne == pointerId) {
                    pointerOne = pointerTwo
                    lastOneX = lastTwoX
                    lastOneY = lastTwoY
                    pointerTwo = MotionEvent.INVALID_POINTER_ID
                    multiTouchTracking = TRACKING_UNKNOWN
                } else if (pointerTwo == pointerId) {
                    pointerTwo = MotionEvent.INVALID_POINTER_ID
                    multiTouchTracking = TRACKING_UNKNOWN
                }
            }
        }
        return true
    }

    override fun draw(canvas: Canvas) {
        if (!enabled) {
            super.draw(canvas)
            return
        }
        getLocationInWindow(location)
        val x = location[0].toFloat()
        val y = location[1].toFloat()
        val saveCount = canvas.save()
        val cx = width / 2f
        val cy = height / 2f
        camera.save()
        camera.rotate(rotationX, rotationY, 0f)
        camera.getMatrix(matrix)
        camera.restore()
        matrix.preTranslate(-cx, -cy)
        matrix.postTranslate(cx, cy)
        canvas.concat(matrix)
        canvas.scale(zoom, zoom, cx, cy)
        if (!layeredViewQueue.isEmpty()) {
            throw AssertionError("View queue is not empty.")
        }

        var i = 0
        val count = childCount
        while (i < count) {
            val layeredView = layeredViewPool.obtain()
            layeredView[getChildAt(i)] = 0
            layeredViewQueue.add(layeredView)
            i++
        }
        while (!layeredViewQueue.isEmpty()) {
            val layeredView = layeredViewQueue.removeFirst()
            val view = layeredView.view
            val layer = layeredView.layer

            layeredView.clear()
            layeredViewPool.restore(layeredView)

            if (view is ViewGroup) {
                val viewGroup = view
                visibilities.clear()
                var i = 0
                val count = viewGroup.childCount
                while (i < count) {
                    val child = viewGroup.getChildAt(i)
                    if (child.visibility == VISIBLE) {
                        visibilities.set(i)
                        child.visibility = INVISIBLE
                    }
                    i++
                }
            }
            val viewSaveCount = canvas.save()

            val translateShowX = rotationY / ROTATION_MAX
            val translateShowY = rotationX / ROTATION_MAX
            val tx = layer * spacing * density * translateShowX
            val ty = layer * spacing * density * translateShowY
            canvas.translate(tx, -ty)
            view!!.getLocationInWindow(location)
            canvas.translate(location[0] - x, location[1] - y)
            viewBoundsRect[0, 0, view.width] = view.height
            canvas.drawRect(viewBoundsRect, viewBorderPaint)
            if (isDrawingViews) {
                view.draw(canvas)
            }
            if (isDrawingIds) {
                val id = view.id
                if (id != NO_ID) {
                    canvas.drawText(nameForId(id)!!, textOffset, textSize, viewBorderPaint)
                }
            }
            canvas.restoreToCount(viewSaveCount)

            if (view is ViewGroup) {
                val viewGroup = view
                var i = 0
                val count = viewGroup.childCount
                while (i < count) {
                    if (visibilities[i]) {
                        val child = viewGroup.getChildAt(i)
                        child.visibility = VISIBLE
                        val childLayeredView = layeredViewPool.obtain()
                        childLayeredView[child] = layer + 1
                        layeredViewQueue.add(childLayeredView)
                    }
                    i++
                }
            }
        }
        canvas.restoreToCount(saveCount)
    }

    private fun nameForId(id: Int): String? {
        var name = idNames[id]
        if (name == null) {
            name = try {
                res.getResourceEntryName(id)
            } catch (e: Resources.NotFoundException) {
                String.format("0x%8x", id)
            }
            idNames.put(id, name)
        }
        return name
    }

    private abstract class Pool<T>(initialSize: Int) {
        private val pool: Deque<T>

        init {
            pool = ArrayDeque(initialSize)
            for (i in 0 until initialSize) {
                pool.addLast(newObject())
            }
        }

        fun obtain(): T {
            return if (pool.isEmpty()) newObject() else pool.removeLast()
        }

        fun restore(instance: T) {
            pool.addLast(instance)
        }

        protected abstract fun newObject(): T
    }

    companion object {
        private const val TRACKING_UNKNOWN = 0
        private const val TRACKING_VERTICALLY = 1
        private const val TRACKING_HORIZONTALLY = -1
        private const val ROTATION_MAX = 60
        private const val ROTATION_MIN = -ROTATION_MAX
        private const val ROTATION_DEFAULT_X = -10
        private const val ROTATION_DEFAULT_Y = 15
        private const val ZOOM_DEFAULT = 0.6f
        private const val ZOOM_MIN = 0.33f
        private const val ZOOM_MAX = 2f
        private const val SPACING_DEFAULT = 25
        private const val SPACING_MIN = 10
        private const val SPACING_MAX = 100
        private const val CHROME_COLOR = -0x777778
        private const val CHROME_SHADOW_COLOR = -0x1000000
        private const val TEXT_OFFSET_DP = 2
        private const val TEXT_SIZE_DP = 10
        private const val CHILD_COUNT_ESTIMATION = 25
        const val INVALIDATE_FREQUENCY_MS = 100L
    }
}