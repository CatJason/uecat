package me.ele.uetool.base.item

import android.graphics.Bitmap

class BitmapItem(@JvmField val name: String, @JvmField val bitmap: Bitmap?) : Item() {
    override val isValid: Boolean
        get() {
            if (bitmap == null) {
                return false
            }
            return true
        }
}
