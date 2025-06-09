package me.ele.uetool

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import me.ele.uetool.base.dip2px

class BoardTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(
    context, attrs, defStyleAttr
) {
    private val defaultInfo = "点击\n退出"
    private val padding = dip2px(3f)

    init {
        initView()
    }

    private fun initView() {
        setBackgroundColor(-0x6fdc6a01)
        setPadding(padding, padding, padding, padding)
        setTextColor(-0x1)
        textSize = 20f
        text = defaultInfo
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            null, null, ContextCompat.getDrawable(
                context, R.drawable.uet_close
            ), null
        )
        compoundDrawablePadding = dip2px(2f)
    }

    fun updateInfo(info: String) {
        text = """
               $info
               $defaultInfo
               """.trimIndent()
    }
}