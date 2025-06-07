package me.ele.uetool.base

import android.graphics.Rect
import android.os.Build
import android.view.View

class Element(@JvmField val view: View) {
    val originRect: Rect = Rect()
    @JvmField
    val rect: Rect = Rect()
    private val location = IntArray(2)
    var parentElement: Element? = null
        get() {
            if (field == null) {
                val parentView: Any = view.parent
                if (parentView is View) {
                    field = Element(parentView)
                }
            }
            return field
        }
        private set

    init {
        reset()
        originRect[rect.left, rect.top, rect.right] = rect.bottom
    }

    fun reset() {
        view!!.getLocationOnScreen(location)
        val width = view.width
        val height = view.height

        val left = location[0]
        val right = left + width
        var top = location[1]
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            top -= DimenUtil.getStatusBarHeight()
        }
        val bottom = top + height

        rect[left, top, right] = bottom
    }

    val area: Int
        //  view 的面积
        get() = view!!.width * view.height

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val element = o as Element

        return view == element.view
    }

    override fun hashCode(): Int {
        return view?.hashCode() ?: 0
    }
}
