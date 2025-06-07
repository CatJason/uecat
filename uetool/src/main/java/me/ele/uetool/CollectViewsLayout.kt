package me.ele.uetool

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import me.ele.uetool.base.Element
import me.ele.uetool.base.ReflectionP
import java.util.*
import me.ele.uetool.base.ReflectionP.Func
import me.ele.uetool.base.DimenUtil.dip2px
import me.ele.uetool.base.DimenUtil.getScreenHeight
import me.ele.uetool.base.DimenUtil.getScreenWidth
import me.ele.uetool.base.DimenUtil.px2dip
import me.ele.uetool.base.DimenUtil.sp2px

open class CollectViewsLayout : View {
    private val halfEndPointWidth = dip2px(2.5f)
    private val textBgFillingSpace = dip2px(2f)
    private val textLineDistance = dip2px(5f)
    protected val screenWidth = getScreenWidth()
    protected val screenHeight = getScreenHeight()

    protected var elements: MutableList<Element> = ArrayList()
    protected var childElement: Element? = null
    protected var parentElement: Element? = null
    protected val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = sp2px(10f).toFloat()
        color = Color.RED
        strokeWidth = dip2px(1f).toFloat()
    }

    private val textBgPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        strokeJoin = Paint.Join.ROUND
    }

    protected val dashLinePaint = Paint().apply {
        isAntiAlias = true
        color = 0x90FF0000.toInt()
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(dip2px(4f).toFloat(), dip2px(8f).toFloat()), 0f)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            val targetActivity = UETool.getTargetActivity()?: return
            val windowManager = targetActivity.windowManager

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                val mGlobalField = Class.forName("android.view.WindowManagerImpl").getDeclaredField("mGlobal")
                mGlobalField.isAccessible = true

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    val mViewsField = Class.forName("android.view.WindowManagerGlobal").getDeclaredField("mViews")
                    mViewsField.isAccessible = true
                    val views: List<View> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mViewsField.get(mGlobalField.get(windowManager)) as List<View>
                    } else {
                        (mViewsField.get(mGlobalField.get(windowManager)) as Array<View>).toList()
                    }

                    for (i in views.indices.reversed()) {
                        val targetView = getTargetDecorView(targetActivity, views[i])
                        if (targetView != null) {
                            createElements(targetView)
                            break
                        }
                    }
                } else {
                    ReflectionP.breakAndroidP(Func<Void> {
                        try {
                            val mRootsField = Class.forName("android.view.WindowManagerGlobal").getDeclaredField("mRoots")
                            mRootsField.isAccessible = true
                            val viewRootImpls = mRootsField.get(mGlobalField.get(windowManager)) as List<*>
                            for (i in viewRootImpls.indices.reversed()) {
                                val clazz = Class.forName("android.view.ViewRootImpl")
                                val obj = viewRootImpls[i]
                                var layoutParams: WindowManager.LayoutParams? = null
                                try {
                                    val mWindowAttributesField = clazz.getDeclaredField("mWindowAttributes")
                                    mWindowAttributesField.isAccessible = true
                                    layoutParams = mWindowAttributesField.get(obj) as WindowManager.LayoutParams
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                val mViewField = clazz.getDeclaredField("mView")
                                mViewField.isAccessible = true
                                val decorView = mViewField.get(obj) as View
                                if ((layoutParams != null && layoutParams.title.toString().contains(targetActivity.javaClass.name))
                                    || getTargetDecorView(targetActivity, decorView) != null) {
                                    createElements(decorView)
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        null
                    })
                }
            } else {
                // http://androidxref.com/4.1.1/xref/frameworks/base/core/java/android/view/WindowManagerImpl.java
                val mWindowManagerField = Class.forName("android.view.WindowManagerImpl\$CompatModeWrapper").getDeclaredField("mWindowManager")
                mWindowManagerField.isAccessible = true
                val mViewsField = Class.forName("android.view.WindowManagerImpl").getDeclaredField("mViews")
                mViewsField.isAccessible = true
                val views = (mViewsField.get(mWindowManagerField.get(windowManager)) as Array<View>).toList()
                for (i in views.indices.reversed()) {
                    val targetView = getTargetDecorView(targetActivity, views[i])
                    if (targetView != null) {
                        createElements(targetView)
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        elements.clear()
        childElement = null
        parentElement = null
    }

    private fun createElements(view: View) {
        val elements = ArrayList<Element>()
        traverse(view, elements)

        // Sort by area in descending order
        elements.sortWith(Comparator { o1, o2 -> o2.area - o1.area })

        this.elements.addAll(elements)
    }

    private fun traverse(view: View, elements: MutableList<Element>) {
        if (UETool.getFilterClasses().contains(view.javaClass.name)) return
        if (view.alpha == 0f || view.visibility != View.VISIBLE) return
        if (resources.getString(R.string.uet_disable) == view.tag) return
        elements.add(Element(view))
        if (view is ViewGroup) {
            val parent = view
            for (i in 0 until parent.childCount) {
                traverse(parent.getChildAt(i), elements)
            }
        }
    }

    private fun getTargetDecorView(targetActivity: Activity, decorView: View): View? {
        if (decorView.width == 0 || decorView.height == 0) {
            return null
        }
        var context: Context? = null
        if (decorView is ViewGroup && decorView.childCount > 0) {
            context = decorView.getChildAt(0).context
        }

        while (context != null) {
            if (context == targetActivity) {
                return decorView
            } else if (context is ContextWrapper) {
                context = (context as ContextWrapper).baseContext
            } else {
                return null
            }
        }
        return null
    }

    protected fun getTargetElement(x: Float, y: Float): Element? {
        var target: Element? = null
        for (i in elements.indices.reversed()) {
            val element = elements[i]
            if (element.rect.contains(x.toInt(), y.toInt())) {
                if (isParentNotVisible(element.parentElement)) {
                    continue
                }
                if (element != childElement) {
                    childElement = element
                    parentElement = element
                } else if (parentElement != null) {
                    parentElement = parentElement!!.parentElement
                }
                target = parentElement ?: element
                break
            }
        }
        if (target == null) {
            Toast.makeText(context, resources.getString(R.string.uet_target_element_not_found, x, y), Toast.LENGTH_SHORT).show()
        }
        return target
    }

    private fun isParentNotVisible(parent: Element?): Boolean {
        if (parent == null) {
            return false
        }
        return if (parent.rect.left >= getScreenWidth()
            || parent.rect.top >= getScreenHeight()
        ) {
            true
        } else {
            isParentNotVisible(parent.parentElement)
        }
    }

    protected fun getTargetElements(x: Float, y: Float): List<Element> {
        val validList = ArrayList<Element>()
        for (i in elements.indices.reversed()) {
            val element = elements[i]
            if (element.rect.contains(x.toInt(), y.toInt())) {
                validList.add(element)
            }
        }
        return validList
    }

    protected fun drawText(canvas: Canvas, text: String, x: Float, y: Float) {
        var left = x - textBgFillingSpace
        var top = y - getTextHeight(text)
        var right = x + getTextWidth(text) + textBgFillingSpace
        var bottom = y + textBgFillingSpace
        // ensure text in screen bound
        if (left < 0) {
            right -= left
            left = 0f
        }
        if (top < 0) {
            bottom -= top
            top = 0f
        }
        if (bottom > screenHeight) {
            val diff = top - bottom
            bottom = screenHeight.toFloat()
            top = bottom + diff
        }
        if (right > screenWidth) {
            val diff = left - right
            right = screenWidth.toFloat()
            left = right + diff
        }
        canvas.drawRect(left, top, right, bottom, textBgPaint)
        canvas.drawText(text, left + textBgFillingSpace, bottom - textBgFillingSpace, textPaint)
    }

    private fun drawLineWithEndPoint(canvas: Canvas, startX: Int, startY: Int, endX: Int, endY: Int) {
        canvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), textPaint)
        if (startX == endX) {
            canvas.drawLine((startX - halfEndPointWidth).toFloat(), startY.toFloat(), (endX + halfEndPointWidth).toFloat(), startY.toFloat(), textPaint)
            canvas.drawLine((startX - halfEndPointWidth).toFloat(), endY.toFloat(), (endX + halfEndPointWidth).toFloat(), endY.toFloat(), textPaint)
        } else if (startY == endY) {
            canvas.drawLine(startX.toFloat(), (startY - halfEndPointWidth).toFloat(), startX.toFloat(), (endY + halfEndPointWidth).toFloat(), textPaint)
            canvas.drawLine(endX.toFloat(), (startY - halfEndPointWidth).toFloat(), endX.toFloat(), (endY + halfEndPointWidth).toFloat(), textPaint)
        }
    }

    protected open fun drawLineWithText(canvas: Canvas, startX: Int, startY: Int, endX: Int, endY: Int) {
        drawLineWithText(canvas, startX, startY, endX, endY, 0)
    }

    protected fun drawLineWithText(canvas: Canvas, startX: Int, startY: Int, endX: Int, endY: Int, endPointSpace: Int) {
        if (startX == endX && startY == endY) {
            return
        }

        var sX = startX
        var sY = startY
        var eX = endX
        var eY = endY

        if (sX > eX) {
            val tempX = sX
            sX = eX
            eX = tempX
        }
        if (sY > eY) {
            val tempY = sY
            sY = eY
            eY = tempY
        }

        if (sX == eX) {
            drawLineWithEndPoint(canvas, sX, sY + endPointSpace, eX, eY - endPointSpace)
            val text = px2dip((eY - sY).toFloat(), true)
            drawText(canvas, text, (sX + textLineDistance).toFloat(), sY + (eY - sY) / 2 + getTextHeight(text) / 2)
        } else if (sY == eY) {
            drawLineWithEndPoint(canvas, sX + endPointSpace, sY, eX - endPointSpace, eY)
            val text = px2dip((eX - sX).toFloat(), true)
            drawText(canvas, text, sX + (eX - sX) / 2 - getTextWidth(text) / 2, (sY - textLineDistance).toFloat())
        }
    }

    protected fun getTextHeight(text: String): Float {
        val rect = Rect()
        textPaint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    protected fun getTextWidth(text: String): Float {
        return textPaint.measureText(text)
    }
}