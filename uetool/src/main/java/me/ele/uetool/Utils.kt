package me.ele.uetool

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.NinePatch
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.text.SpannedString
import android.text.style.ImageSpan
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
import me.ele.uetool.UETool.getTargetActivity
import me.ele.uetool.base.Application
import me.ele.uetool.base.ReflectionP.breakAndroidP
import java.util.Locale

fun getViewTag(view: View): String {
    val tag = view.tag
    return tag?.toString() ?: ""
}

fun getImageViewScaleType(imageView: ImageView): String {
    return imageView.scaleType.name
}

fun getTextViewBitmap(textView: TextView): List<Pair<String, Bitmap?>> {
    val bitmaps = mutableListOf<Pair<String, Bitmap?>>()
    bitmaps.addAll(getTextViewDrawableBitmap(textView))
    bitmaps.addAll(getTextViewImageSpanBitmap(textView))
    return bitmaps
}

private fun getTextViewImageSpanBitmap(textView: TextView): List<Pair<String, Bitmap?>> {
    return buildList {
        try {
            val text = textView.text
            if (text is SpannedString) {
                val mSpansField = Class.forName("android.text.SpannableStringInternal")
                    .getDeclaredField("mSpans")
                mSpansField.isAccessible = true
                val spans = mSpansField.get(text) as? Array<*>

                spans?.forEach { span ->
                    if (span is ImageSpan) {
                        add("SpanBitmap" to getDrawableBitmap(span.drawable))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun getTextViewDrawableBitmap(textView: TextView): List<Pair<String, Bitmap?>> {
    val bitmaps = mutableListOf<Pair<String, Bitmap?>>()
    try {
        val drawables = textView.compoundDrawables
        bitmaps.add("DrawableLeft" to getDrawableBitmap(drawables[0]))
        bitmaps.add("DrawableTop" to getDrawableBitmap(drawables[1]))
        bitmaps.add("DrawableRight" to getDrawableBitmap(drawables[2]))
        bitmaps.add("DrawableBottom" to getDrawableBitmap(drawables[3]))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bitmaps
}

public fun getDrawableBitmap(drawable: Drawable?): Bitmap? {
    if (drawable == null) return null

    return try {
        when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            is NinePatchDrawable -> {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                        val mNinePatchStateField = NinePatchDrawable::class.java.getDeclaredField("mNinePatchState")
                        mNinePatchStateField.isAccessible = true
                        val mNinePatchState = mNinePatchStateField.get(drawable)
                        val mNinePatchField = mNinePatchState.javaClass.getDeclaredField("mNinePatch")
                        mNinePatchField.isAccessible = true
                        (mNinePatchField.get(mNinePatchState) as? NinePatch)?.bitmap
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                        val mNinePatchField = NinePatchDrawable::class.java.getDeclaredField("mNinePatch")
                        mNinePatchField.isAccessible = true
                        (mNinePatchField.get(drawable) as? NinePatch)?.bitmap
                    }
                    else -> null
                }
            }
            is ClipDrawable -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    (drawable.drawable as? BitmapDrawable)?.bitmap
                } else {
                    null
                }
            }
            is StateListDrawable -> (drawable.current as? BitmapDrawable)?.bitmap
            is VectorDrawableCompat -> {
                val mVectorStateField = VectorDrawableCompat::class.java.getDeclaredField("mVectorState")
                mVectorStateField.isAccessible = true
                val mCachedBitmapField = Class.forName("android.support.graphics.drawable.VectorDrawableCompat\$VectorDrawableCompatState")
                    .getDeclaredField("mCachedBitmap")
                mCachedBitmapField.isAccessible = true
                mCachedBitmapField.get(mVectorStateField.get(drawable)) as? Bitmap
            }
            else -> null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun intToHexColor(color: Int): String {
    return "#" + Integer.toHexString(color).uppercase(Locale.getDefault())
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

//  获取当前 view 所在的最上层 fragment
fun getCurrentFragment(targetView: View?): Fragment? {
    targetView?: return null
    val activity: Activity = getTargetActivity()?: return null

    if (activity is FragmentActivity) {
        val fragments = collectVisibleFragment(activity.supportFragmentManager)
        for (i in fragments.indices.reversed()) {
            val fragment = fragments[i]
            val view = fragment.view?: continue
            if (findTargetView(view, targetView)) {
                return fragment
            }
        }
    }

    return null
}

//  获取当前 fragment 类名
fun getCurrentFragmentName(targetView: View?): String? {
    val fragment = getCurrentFragment(targetView)

    if (fragment != null) {
        return fragment.javaClass.name
    }

    return null
}

//  遍历目标 view 是否在指定 view 内
fun findTargetView(view: View, targetView: View): Boolean {
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
        } catch (e: java.lang.Exception) {
            return@breakAndroidP null
        }
    }
}

fun getResId(view: View): String {
    try {
        val id = view.id
        return if (id == View.NO_ID) {
            ""
        } else {
            "0x" + Integer.toHexString(id)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return ""
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

fun getImageViewBitmap(imageView: ImageView): Bitmap? {
    return getDrawableBitmap(imageView.drawable)
}

fun clipText(clipText: String?) {
    val context = Application.getApplicationContext()
    val clipData = ClipData.newPlainText("", clipText)
    ((context.getSystemService(Context.CLIPBOARD_SERVICE)) as ClipboardManager).setPrimaryClip(
        clipData
    )
    Toast.makeText(context, "copied", Toast.LENGTH_SHORT).show()
}

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
