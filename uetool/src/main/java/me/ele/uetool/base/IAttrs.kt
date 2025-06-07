package me.ele.uetool.base

import me.ele.uetool.base.item.Item

interface IAttrs {
    fun getAttrs(element: Element?): List<Item?>?
}
