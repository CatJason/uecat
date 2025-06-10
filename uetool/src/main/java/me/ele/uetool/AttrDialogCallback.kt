package me.ele.uetool

import me.ele.uetool.base.Element

interface AttrDialogCallback {
    fun enableMove()
    fun showValidViews(position: Int, isChecked: Boolean)
    fun selectView(element: Element)
}