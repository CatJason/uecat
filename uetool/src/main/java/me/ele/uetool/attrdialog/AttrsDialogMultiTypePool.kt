package me.ele.uetool.attrdialog

import me.ele.uetool.base.ItemViewBinder
import me.ele.uetool.base.item.Item

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 22:50
 */
class AttrsDialogMultiTypePool {
    private val classes = mutableListOf<Class<*>>()
    private val binders = mutableListOf<ItemViewBinder<*, *>>()

    fun <T : Item> register(
        clazz: Class<T>,
        binder: ItemViewBinder<T, *>
    ) {
        if (clazz in classes) return
        classes.add(clazz)
        binders.add(binder)
    }

    fun getItemViewBinder(index: Int): ItemViewBinder<*, *> {
        require(index in 0 until binders.size) {
            "Unsupported view holder type: $index"
        }
        return binders[index]
    }

    fun getItemType(item: Any): Int {
        return classes.indexOfFirst { it == item.javaClass }.takeIf { it != -1 }
            ?: throw RuntimeException("Unsupported class type: ${item.javaClass.name}")
    }
}