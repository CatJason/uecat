package me.ele.uetool.sample

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class CustomLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.custom_layout, this)
    }
}