package me.ele.uetool.base.item

import me.ele.uetool.base.Element

class BriefDescItem @JvmOverloads constructor(element: Element, @JvmField val isSelected: Boolean = false) :
    ElementItem("", element) {
    override val isValid: Boolean
        get() = true
}
