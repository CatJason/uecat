package me.ele.uetool

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.NinePatch
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.NinePatchDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.text.SpannedString
import android.text.style.ImageSpan
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import me.ele.uetool.base.Application
import me.ele.uetool.base.ReflectionP.breakAndroidP
import java.util.Locale

fun enableFullscreen(window: Window) {
    if (Build.VERSION.SDK_INT >= 21) {
        addSystemUiFlag(window, 1280)
    }
}

private fun addSystemUiFlag(window: Window, flag: Int) {
    val view = window.decorView
    if (view != null) {
        view.systemUiVisibility = view.systemUiVisibility or flag
    }
}

fun setStatusBarColor(window: Window, color: Int) {
    if (Build.VERSION.SDK_INT >= 21) {
        window.statusBarColor = color
    }
}

fun getViewClickListener(view: View?): String? {
    return breakAndroidP {
        try {
            val mListenerInfoField = View::class.java.getDeclaredField("mListenerInfo")
            mListenerInfoField.isAccessible = true
            val mClickListenerField = Class.forName("android.view.View\$ListenerInfo")
                .getDeclaredField("mOnClickListener")
            mClickListenerField.isAccessible = true
            val listener = mClickListenerField[mListenerInfoField[view]] as View.OnClickListener
            return@breakAndroidP listener?.javaClass?.name
        } catch (e: Exception) {
            return@breakAndroidP null
        }
    }
}

fun getResourceName(id: Int): String {
    val resources = Application.getApplicationContext().resources
    try {
        return if (id == View.NO_ID || id == 0) {
            ""
        } else {
            resources.getResourceEntryName(id)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun getViewTag(view: View): String {
    val tag = view.tag
    return tag?.toString() ?: ""
}

fun getResId(view: View): String {
    try {
        val id = view.id
        return if (id == View.NO_ID) {
            ""
        } else {
            "0x" + Integer.toHexString(id)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun intToHexColor(color: Int): String {
    return "#" + Integer.toHexString(color).uppercase(Locale.getDefault())
}

fun getTextViewBitmap(textView: TextView): List<Pair<String, Bitmap>> {
    val bitmaps: MutableList<Pair<String, Bitmap>> = java.util.ArrayList()
    bitmaps.addAll(getTextViewDrawableBitmap(textView))
    bitmaps.addAll(getTextViewImageSpanBitmap(textView))
    return bitmaps
}

private fun getTextViewDrawableBitmap(textView: TextView): List<Pair<String, Bitmap>> {
    val bitmaps: MutableList<Pair<String, Bitmap>> = java.util.ArrayList()
    try {
        val drawables = textView.compoundDrawables
        bitmaps.add(Pair("DrawableLeft", getDrawableBitmap(drawables[0])))
        bitmaps.add(Pair("DrawableTop", getDrawableBitmap(drawables[1])))
        bitmaps.add(Pair("DrawableRight", getDrawableBitmap(drawables[2])))
        bitmaps.add(Pair("DrawableBottom", getDrawableBitmap(drawables[3])))
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return bitmaps
}

private fun getTextViewImageSpanBitmap(textView: TextView): List<Pair<String, Bitmap>> {
    val bitmaps: MutableList<Pair<String, Bitmap>> = java.util.ArrayList()
    try {
        val text = textView.text
        if (text is SpannedString) {
            val mSpansField =
                Class.forName("android.text.SpannableStringInternal").getDeclaredField("mSpans")
            mSpansField.isAccessible = true
            val spans = mSpansField[text] as Array<Any>
            for (span in spans) {
                if (span is ImageSpan) {
                    bitmaps.add(Pair("SpanBitmap", getDrawableBitmap(span.drawable)))
                }
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return bitmaps
}

//  获取当前 view 所在的最上层 fragment
fun getCurrentFragment(targetView: View): Fragment? {
    val activity: Activity = UETool.getTargetActivity()?: return null
    if (activity is FragmentActivity) {
        val fragments = collectVisibleFragment(activity.supportFragmentManager)
        for (i in fragments.indices.reversed()) {
            val fragment = fragments[i]
            if (findTargetView(fragment.view, targetView)) {
                return fragment
            }
        }
    }

    return null
}

//  收集所有可见 fragment
private fun collectVisibleFragment(fragmentManager: FragmentManager): List<Fragment> {
    val fragments: MutableList<Fragment> = ArrayList()

    for (fragment in fragmentManager.fragments) {
        if (fragment.isVisible) {
            fragments.add(fragment)
            fragments.addAll(collectVisibleFragment(fragment.childFragmentManager))
        }
    }

    return fragments
}

fun getImageViewScaleType(imageView: ImageView): String {
    return imageView.scaleType.name
}

fun getImageViewBitmap(imageView: ImageView): Bitmap? {
    return getDrawableBitmap(imageView.drawable)
}

fun getCurrentActivity(): Activity? {
    try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread")
        val currentActivityThread = currentActivityThreadMethod.invoke(null)
        val mActivitiesField = activityThreadClass.getDeclaredField("mActivities")
        mActivitiesField.isAccessible = true
        val activities = mActivitiesField[currentActivityThread] as Map<*, *>
        for (record in activities.values) {
            val recordClass: Class<*> = record?.javaClass?: continue
            val pausedField = recordClass.getDeclaredField("paused")
            pausedField.isAccessible = true
            if (pausedField[record] == false) {
                val activityField = recordClass.getDeclaredField("activity")
                activityField.isAccessible = true
                return activityField[record] as Activity
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return null
}

private fun getDrawableBitmap(drawable: Drawable): Bitmap? {
    try {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is NinePatchDrawable) {
            var ninePatch: NinePatch? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val mNinePatchStateFiled =
                    NinePatchDrawable::class.java.getDeclaredField("mNinePatchState")
                mNinePatchStateFiled.isAccessible = true
                val mNinePatchState = mNinePatchStateFiled[drawable]
                val mNinePatchFiled = mNinePatchState.javaClass.getDeclaredField("mNinePatch")
                mNinePatchFiled.isAccessible = true
                ninePatch = mNinePatchFiled[mNinePatchState] as NinePatch
                return ninePatch!!.bitmap
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val mNinePatchFiled = NinePatchDrawable::class.java.getDeclaredField("mNinePatch")
                mNinePatchFiled.isAccessible = true
                ninePatch = mNinePatchFiled[drawable] as NinePatch
                return ninePatch!!.bitmap
            }
        } else if (drawable is ClipDrawable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (drawable.drawable as BitmapDrawable).bitmap
            }
        } else if (drawable is StateListDrawable) {
            return (drawable.getCurrent() as BitmapDrawable).bitmap
        } else if (drawable is VectorDrawableCompat) {
            val mVectorStateField =
                VectorDrawableCompat::class.java.getDeclaredField("mVectorState")
            mVectorStateField.isAccessible = true
            val mCachedBitmapField =
                Class.forName("android.support.graphics.drawable.VectorDrawableCompat\$VectorDrawableCompatState")
                    .getDeclaredField("mCachedBitmap")
            mCachedBitmapField.isAccessible = true
            return mCachedBitmapField[mVectorStateField[drawable]] as Bitmap
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return null
}

fun getBackground(view: View): Any? {
    val drawable = view.background
    if (drawable is ColorDrawable) {
        return intToHexColor(drawable.color)
    } else if (drawable is GradientDrawable) {
        try {
            val mFillPaintField = GradientDrawable::class.java.getDeclaredField("mFillPaint")
            mFillPaintField.isAccessible = true
            val mFillPaint = mFillPaintField[drawable] as Paint
            val shader = mFillPaint.shader
            if (shader is LinearGradient) {
                val mColorsField = LinearGradient::class.java.getDeclaredField("mColors")
                mColorsField.isAccessible = true
                val mColors = mColorsField[shader] as IntArray
                val sb = StringBuilder()
                var i = 0
                val N = mColors.size
                while (i < N) {
                    sb.append(intToHexColor(mColors[i]))
                    if (i < N - 1) {
                        sb.append(" -> ")
                    }
                    i++
                }
                return sb.toString()
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    } else {
        return getDrawableBitmap(drawable)
    }
    return null
}

fun clipText(clipText: String?) {
    val context = Application.getApplicationContext()
    val clipData = ClipData.newPlainText("", clipText)
    ((context.getSystemService(Context.CLIPBOARD_SERVICE)) as ClipboardManager).setPrimaryClip(
        clipData
    )
    Toast.makeText(context, "copied", Toast.LENGTH_SHORT).show()
}

//  获取当前 fragment 类名
fun getCurrentFragmentName(targetView: View): String? {
    val fragment = getCurrentFragment(targetView)

    if (fragment != null) {
        return fragment.javaClass.name
    }

    return null
}

//  获取当前 view 的 view holder 类名
fun getViewHolderName(targetView: View?): String? {
    var currentView = targetView
    while (currentView != null) {
        val parent = currentView.parent
        if (parent is RecyclerView) {
            return parent.getChildViewHolder(currentView).javaClass.name
        }
        currentView = if (parent is View) parent else null
    }
    return null
}

//  遍历目标 view 是否在指定 view 内
private fun findTargetView(view: View?, targetView: View): Boolean {
    if (view === targetView) {
        return true
    }
    if (view is ViewGroup) {
        val parent = view
        for (i in 0 until parent.childCount) {
            if (findTargetView(parent.getChildAt(i), targetView)) {
                return true
            }
        }
    }
    return false
}