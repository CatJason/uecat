package me.ele.uetool.sample

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var moreAttribution: String? = null
        get() = field
        set(value) {
            field = value
            // You can add additional logic here when the value changes
            // For example: invalidate() or requestLayout()
        }

    // Alternative concise property syntax (without custom setter logic):
    // var moreAttribution: String? = null
}