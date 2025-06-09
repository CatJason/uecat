package me.ele.uetool.base

import android.content.Context
import android.util.TypedValue

private val CONTEXT: Context? get() = Application.getApplicationContext()

fun px2dip(pxValue: Float): String {
    return px2dip(pxValue, false)
}

fun px2dip(pxValue: Float, withUnit: Boolean): String {
    val scale = CONTEXT?.resources?.displayMetrics?.density ?: return ""
    return "${(pxValue / scale + 0.5F).toInt()}${if (withUnit) "dp" else ""}"
}

fun dip2px(dpValue: Float): Int {
    val scale = CONTEXT?.resources?.displayMetrics?.density ?: return 0
    return (dpValue * scale + 0.5F).toInt()
}

fun sp2px(sp: Float): Int {
    return CONTEXT?.resources?.displayMetrics?.let {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, it).toInt()
    } ?: 0
}

fun px2sp(pxValue: Float): String {
    val fontScale = CONTEXT?.resources?.displayMetrics?.scaledDensity ?: return "0"
    return "${(pxValue / fontScale + 0.5f).toInt()}"
}

fun getScreenWidth(): Int {
    return CONTEXT?.resources?.displayMetrics?.widthPixels ?: 0
}

fun getScreenHeight(): Int {
    return CONTEXT?.resources?.displayMetrics?.heightPixels ?: 0
}

fun getStatusBarHeight(): Int {
    val resources = CONTEXT?.resources ?: return 0
    val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resId > 0) resources.getDimensionPixelSize(resId) else 0
}