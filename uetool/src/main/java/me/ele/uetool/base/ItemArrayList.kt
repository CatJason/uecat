package me.ele.uetool.base

import me.ele.uetool.base.item.Item
import java.util.*

class ItemArrayList<T : Item> : ArrayList<T>() {

    override fun add(element: T): Boolean {
        if (!element.isValid) {
            return false
        }
        return super.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val filtered = elements.toMutableList().apply { removeInvalidItems() }
        return super.addAll(filtered)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val filtered = elements.toMutableList().apply { removeInvalidItems() }
        return super.addAll(index, filtered)
    }

    private fun MutableCollection<T>.removeInvalidItems() {
        val iterator = iterator()
        while (iterator.hasNext()) {
            if (!iterator.next().isValid) {
                iterator.remove()
            }
        }
    }
}