package me.ele.uetool.base.item

import android.text.TextUtils
import android.view.View

class TextItem @JvmOverloads constructor(
    name: String, @JvmField val detail: String?, //  是否可复制文案
    val isEnableCopy: Boolean = false, @JvmField val onClickListener: View.OnClickListener? = null
) :
    TitleItem(name) {
    constructor(name: String, detail: String?, onClickListener: View.OnClickListener?) : this(
        name,
        detail,
        false,
        onClickListener
    )

    override val isValid: Boolean
        get() {
            if (TextUtils.isEmpty(detail)) {
                return false
            }
            return true
        }
}