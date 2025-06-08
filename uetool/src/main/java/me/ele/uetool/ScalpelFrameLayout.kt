package me.ele.uetool

import android.annotation.SuppressLint
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
 * 将视图层次结构渲染为可交互的3D图层可视化效果
 *
 * 支持的交互操作:
 *
 *  * 单指触摸: 控制模型的旋转
 *  * 双指垂直捏合: 调整缩放
 *  * 双指水平捏合: 调整图层间距
 */
class ScalpelFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    // 内部类，用于存储视图及其层级信息
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

    // 绘图相关变量
    private val viewBoundsRect = Rect()  // 视图边界矩形
    private val viewBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)  // 视图边框画笔
    private val camera = Camera()  // 用于3D变换的相机
    private val matrix = Matrix()  // 变换矩阵
    private val location = IntArray(2)  // 存储位置坐标

    // 视图可见性管理
    private val visibilities = BitSet(CHILD_COUNT_ESTIMATION)  // 子视图可见性位集
    private val idNames = SparseArray<String?>()  // 存储视图ID对应的名称

    // 视图层级队列和对象池
    private val layeredViewQueue: Deque<LayeredView> = ArrayDeque()  // 待处理的视图队列
    private val layeredViewPool: Pool<LayeredView> = object : Pool<LayeredView>(
        CHILD_COUNT_ESTIMATION
    ) {
        override fun newObject(): LayeredView {
            return LayeredView()  // 创建新的LayeredView对象
        }
    }

    // 资源相关变量
    private val res: Resources  // 资源对象
    private val density: Float  // 屏幕密度
    private val slop: Float  // 触摸阈值
    private val textOffset: Float  // 文本偏移量
    private val textSize: Float  // 文本大小

    // 状态控制变量
    private var enabled = false  // 是否启用3D交互

    /** 当视图图层绘制其内容时返回true */
    var isDrawingViews = true
        private set

    /** 当视图图层绘制其ID时返回true */
    var isDrawingIds = false
        private set

    // 触摸交互相关变量
    private var pointerOne = MotionEvent.INVALID_POINTER_ID  // 第一个触摸点ID
    private var lastOneX = 0f  // 第一个触摸点最后X坐标
    private var lastOneY = 0f  // 第一个触摸点最后Y坐标
    private var pointerTwo = MotionEvent.INVALID_POINTER_ID  // 第二个触摸点ID
    private var lastTwoX = 0f  // 第二个触摸点最后X坐标
    private var lastTwoY = 0f  // 第二个触摸点最后Y坐标
    private var multiTouchTracking = TRACKING_UNKNOWN  // 多点触控跟踪状态

    // 3D变换参数
    private var rotationY = ROTATION_DEFAULT_Y.toFloat()  // Y轴旋转角度
    private var rotationX = ROTATION_DEFAULT_X.toFloat()  // X轴旋转角度
    private var zoom = ZOOM_DEFAULT  // 缩放比例
    private var spacing = SPACING_DEFAULT.toFloat()  // 图层间距

    private var lastInvalidateTime = 0L  // 最后无效化时间

    /** 获取视图边框颜色 */
    var chromeColor = 0
        /** 设置视图边框颜色 */
        set(color) {
            if (chromeColor != color) {
                viewBorderPaint.color = color
                field = color
                invalidate()  // 触发重绘
            }
        }

    /** 获取视图边框阴影颜色 */
    var chromeShadowColor = 0
        /** 设置视图边框阴影颜色 */
        set(color) {
            if (chromeShadowColor != color) {
                viewBorderPaint.setShadowLayer(1f, -1f, 1f, color)
                field = color
                invalidate()  // 触发重绘
            }
        }

    init {
        // 初始化各种参数
        res = context.resources
        density = context.resources.displayMetrics.density
        slop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        textSize = TEXT_SIZE_DP * density
        textOffset = TEXT_OFFSET_DP * density

        // 设置默认颜色和画笔属性
        chromeColor = CHROME_COLOR
        viewBorderPaint.style = Paint.Style.STROKE
        viewBorderPaint.textSize = textSize
        chromeShadowColor = CHROME_SHADOW_COLOR

        // 设置字体
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewBorderPaint.typeface =
                Typeface.create("sans-serif-condensed", Typeface.NORMAL)
        }
    }

    // 3D交互启用状态
    var isLayerInteractionEnabled: Boolean
        /** 当3D视图图层交互启用时返回true */
        get() = enabled
        /** 设置是否启用3D视图图层交互 */
        set(enabled) {
            if (this.enabled != enabled) {
                this.enabled = enabled
                setWillNotDraw(!enabled)
                invalidate()  // 触发重绘
            }
        }

    /** 设置视图图层是否绘制其ID */
    fun setDrawIds(drawIds: Boolean) {
        if (isDrawingIds != drawIds) {
            isDrawingIds = drawIds
            invalidate()  // 触发重绘
        }
    }

    // 触摸事件拦截
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return enabled || super.onInterceptTouchEvent(ev)
    }

    // 触摸事件处理
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enabled) {
            return super.onTouchEvent(event)
        }

        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // 处理按下事件
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
                // 处理移动事件
                if (pointerTwo == MotionEvent.INVALID_POINTER_ID) {
                    // 单指移动 - 旋转控制
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
                            rotationY = (rotationY + drx).coerceAtLeast(ROTATION_MIN.toFloat())
                                .coerceAtMost(ROTATION_MAX.toFloat())
                            rotationX = (rotationX + dry).coerceAtLeast(ROTATION_MIN.toFloat())
                                .coerceAtMost(ROTATION_MAX.toFloat())
                            lastOneX = eventX
                            lastOneY = eventY
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastInvalidateTime > INVALIDATE_FREQUENCY_MS) {
                                clearView()
                                lastInvalidateTime = currentTime
                            }
                        }
                        i++
                    }
                } else {
                    // 双指移动 - 缩放或间距控制
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

                    // 判断是垂直还是水平移动
                    if (multiTouchTracking == TRACKING_UNKNOWN) {
                        val adx = Math.abs(dxOne) + Math.abs(dxTwo)
                        val ady = Math.abs(dyOne) + Math.abs(dyTwo)
                        if (adx > slop * 2 || ady > slop * 2) {
                            multiTouchTracking = if (adx > ady) {
                                TRACKING_HORIZONTALLY  // 水平移动 - 调整间距
                            } else {
                                TRACKING_VERTICALLY  // 垂直移动 - 调整缩放
                            }
                        }
                    }

                    // 根据移动方向调整参数
                    if (multiTouchTracking == TRACKING_VERTICALLY) {
                        // 垂直移动 - 调整缩放
                        zoom += if (yOne >= yTwo) {
                            dyOne / height - dyTwo / height
                        } else {
                            dyTwo / height - dyOne / height
                        }
                        zoom = Math.min(Math.max(zoom, ZOOM_MIN), ZOOM_MAX)
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastInvalidateTime > INVALIDATE_FREQUENCY_MS) {
                            clearView()
                            lastInvalidateTime = currentTime
                        }
                    } else if (multiTouchTracking == TRACKING_HORIZONTALLY) {
                        // 水平移动 - 调整间距
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
                            clearView()
                            lastInvalidateTime = currentTime
                        }
                    }

                    // 更新最后位置
                    if (multiTouchTracking != TRACKING_UNKNOWN) {
                        lastOneX = xOne
                        lastOneY = yOne
                        lastTwoX = xTwo
                        lastTwoY = yTwo
                    }
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                // 处理抬起事件
                val index = if (action != MotionEvent.ACTION_POINTER_UP) 0 else event.actionIndex
                val pointerId = event.getPointerId(index)
                if (pointerOne == pointerId) {
                    // 第一个触摸点抬起，将第二个触摸点转为第一个
                    pointerOne = pointerTwo
                    lastOneX = lastTwoX
                    lastOneY = lastTwoY
                    pointerTwo = MotionEvent.INVALID_POINTER_ID
                    multiTouchTracking = TRACKING_UNKNOWN
                } else if (pointerTwo == pointerId) {
                    // 第二个触摸点抬起
                    pointerTwo = MotionEvent.INVALID_POINTER_ID
                    multiTouchTracking = TRACKING_UNKNOWN
                }
            }
        }
        return true
    }

    // 清除所有层级视图
    fun clearView() {
        // 清空视图队列
        layeredViewQueue.clear()
        // 回收所有LayeredView对象到对象池
        while (!layeredViewPool.pool.isEmpty()) {
            val layeredView = layeredViewPool.pool.removeFirst()
            layeredView.clear()
        }

        // 强制重绘
        invalidate()
    }

    // 绘制方法
    override fun draw(canvas: Canvas) {
        if (!enabled) {
            super.draw(canvas)
            return
        }

        // 获取窗口位置
        getLocationInWindow(location)
        val x = location[0].toFloat()
        val y = location[1].toFloat()

        // 保存画布状态
        val saveCount = canvas.save()
        val cx = width / 2f
        val cy = height / 2f

        // 设置相机变换
        camera.save()
        camera.rotate(rotationX, rotationY, 0f)
        camera.getMatrix(matrix)
        camera.restore()
        matrix.preTranslate(-cx, -cy)
        matrix.postTranslate(cx, cy)
        canvas.concat(matrix)

        // 应用缩放
        canvas.scale(zoom, zoom, cx, cy)

        // 检查视图队列是否为空
        if (!layeredViewQueue.isEmpty()) {
            throw AssertionError("View queue is not empty.")
        }

        // 初始化视图队列
        var i = 0
        val count = childCount
        while (i < count) {
            val layeredView = layeredViewPool.obtain()
            layeredView[getChildAt(i)] = 0
            layeredViewQueue.add(layeredView)
            i++
        }

        // 处理所有视图
        while (!layeredViewQueue.isEmpty()) {
            val layeredView = layeredViewQueue.removeFirst()
            val view = layeredView.view
            val layer = layeredView.layer

            // 回收LayeredView对象
            layeredView.clear()
            layeredViewPool.restore(layeredView)

            // 如果是ViewGroup，处理其子视图
            if (view is ViewGroup) {
                val viewGroup = view
                visibilities.clear()
                var i = 0
                val count = viewGroup.childCount
                while (i < count) {
                    val child = viewGroup.getChildAt(i)
                    if (child.visibility == VISIBLE) {
                        visibilities.set(i)
                        child.visibility = INVISIBLE  // 临时隐藏子视图
                    }
                    i++
                }
            }

            // 保存画布状态
            val viewSaveCount = canvas.save()

            // 计算3D变换偏移
            val translateShowX = rotationY / ROTATION_MAX
            val translateShowY = rotationX / ROTATION_MAX
            val tx = layer * spacing * density * translateShowX
            val ty = layer * spacing * density * translateShowY
            canvas.translate(tx, -ty)

            // 定位视图
            view!!.getLocationInWindow(location)
            canvas.translate(location[0] - x, location[1] - y)

            // 绘制视图边框
            viewBoundsRect[0, 0, view.width] = view.height
            canvas.drawRect(viewBoundsRect, viewBorderPaint)

            // 绘制视图内容
            if (isDrawingViews) {
                view.draw(canvas)
            }

            // 绘制视图ID
            if (isDrawingIds) {
                val id = view.id
                if (id != NO_ID) {
                    canvas.drawText(nameForId(id)!!, textOffset, textSize, viewBorderPaint)
                }
            }

            // 恢复画布状态
            canvas.restoreToCount(viewSaveCount)

            // 如果是ViewGroup，恢复子视图可见性并添加到队列
            if (view is ViewGroup) {
                val viewGroup = view
                var i = 0
                val count = viewGroup.childCount
                while (i < count) {
                    if (visibilities[i]) {
                        val child = viewGroup.getChildAt(i)
                        child.visibility = VISIBLE  // 恢复子视图可见性
                        val childLayeredView = layeredViewPool.obtain()
                        childLayeredView[child] = layer + 1  // 子视图层级+1
                        layeredViewQueue.add(childLayeredView)
                    }
                    i++
                }
            }
        }

        // 恢复画布状态
        canvas.restoreToCount(saveCount)
    }

    // 根据ID获取资源名称
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

    // 对象池抽象类
    private abstract class Pool<T>(initialSize: Int) {
        val pool: Deque<T>  // 对象池队列

        init {
            pool = ArrayDeque(initialSize)
            // 初始化对象池
            for (i in 0 until initialSize) {
                pool.addLast(newObject())
            }
        }

        // 获取对象
        fun obtain(): T {
            return if (pool.isEmpty()) newObject() else pool.removeLast()
        }

        // 归还对象
        fun restore(instance: T) {
            pool.addLast(instance)
        }

        // 创建新对象
        protected abstract fun newObject(): T
    }

    companion object {
        // 多点触控跟踪状态常量
        private const val TRACKING_UNKNOWN = 0
        private const val TRACKING_VERTICALLY = 1
        private const val TRACKING_HORIZONTALLY = -1

        // 旋转参数常量
        private const val ROTATION_MAX = 60
        private const val ROTATION_MIN = -ROTATION_MAX
        private const val ROTATION_DEFAULT_X = -10
        private const val ROTATION_DEFAULT_Y = 15

        // 缩放参数常量
        private const val ZOOM_DEFAULT = 0.6f
        private const val ZOOM_MIN = 0.33f
        private const val ZOOM_MAX = 2f

        // 间距参数常量
        private const val SPACING_DEFAULT = 25
        private const val SPACING_MIN = 10
        private const val SPACING_MAX = 100

        // 颜色常量
        private const val CHROME_COLOR = -0x777778
        private const val CHROME_SHADOW_COLOR = -0x1000000

        // 文本参数常量
        private const val TEXT_OFFSET_DP = 2
        private const val TEXT_SIZE_DP = 10

        // 其他常量
        private const val CHILD_COUNT_ESTIMATION = 25  // 子视图数量估计值
        const val INVALIDATE_FREQUENCY_MS = 100L  // 重绘频率
    }
}