package me.ele.uetool.base.item

import androidx.annotation.IntDef
import me.ele.uetool.base.Element

class SwitchItem : ElementItem {
    @Type
    var type: Int
        private set
    @JvmField
    var isChecked: Boolean = false

    constructor(name: String, element: Element, @Type type: Int) : super(name, element) {
        this.type = type
    }

    constructor(name: String, element: Element, @Type type: Int, isChecked: Boolean) : super(
        name,
        element
    ) {
        this.type = type
        this.isChecked = isChecked
    }

    @IntDef(
        Type.TYPE_IS_BOLD, Type.TYPE_MOVE, Type.TYPE_SHOW_VALID_VIEWS
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type {
        companion object {
            const val TYPE_IS_BOLD: Int = 1
            const val TYPE_MOVE: Int = 2
            const val TYPE_SHOW_VALID_VIEWS: Int = 3
        }
    }
}
