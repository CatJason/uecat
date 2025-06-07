package me.ele.uetool.base.item

import androidx.annotation.IntDef
import me.ele.uetool.base.Element

open class EditTextItem(
    name: String,
    element: Element,
    @JvmField @field:Type @param:Type val type: Int,
    @JvmField val detail: String
) :
    ElementItem(name, element) {
    @IntDef(
        Type.TYPE_TEXT,
        Type.TYPE_TEXT_SIZE,
        Type.TYPE_TEXT_COLOR,
        Type.TYPE_WIDTH,
        Type.TYPE_HEIGHT,
        Type.TYPE_PADDING_LEFT,
        Type.TYPE_PADDING_RIGHT,
        Type.TYPE_PADDING_TOP,
        Type.TYPE_PADDING_BOTTOM
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type {
        companion object {
            const val TYPE_TEXT: Int = 1
            const val TYPE_TEXT_SIZE: Int = 2
            const val TYPE_TEXT_COLOR: Int = 3
            const val TYPE_WIDTH: Int = 4
            const val TYPE_HEIGHT: Int = 5
            const val TYPE_PADDING_LEFT: Int = 6
            const val TYPE_PADDING_RIGHT: Int = 7
            const val TYPE_PADDING_TOP: Int = 8
            const val TYPE_PADDING_BOTTOM: Int = 9
        }
    }
}
