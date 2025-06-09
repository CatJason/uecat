package me.ele.uetool.sample

import me.ele.uetool.base.Element
import me.ele.uetool.base.IAttrs
import me.ele.uetool.base.item.Item
import me.ele.uetool.base.item.TextItem

class CustomAttribution : IAttrs {
    override fun getAttrs(element: Element): List<Item> {
        val items = mutableListOf<Item>()
        if (element.view is CustomView) {
            val view = element.view as CustomView
            items.add(TextItem("More", view.moreAttribution))
        }
        element.view.getTag(R.id.uetool_xml)?.let { tag ->
            items.add(TextItem("XML", tag.toString()))
        }
        return items
    }
}