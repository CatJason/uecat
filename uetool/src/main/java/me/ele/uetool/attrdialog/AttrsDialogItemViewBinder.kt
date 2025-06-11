package me.ele.uetool.attrdialog

import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.AttrDialogCallback
import me.ele.uetool.BaseViewHolder
import me.ele.uetool.UEAdapter
import me.ele.uetool.base.ItemViewBinder
import me.ele.uetool.base.item.Item

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 23:37
 */
abstract class AttrsDialogItemViewBinder<T : Item, VH : BaseViewHolder<T>> : ItemViewBinder<T, VH> {

    protected fun getAttrDialogCallback(adapter: RecyclerView.Adapter<*>): AttrDialogCallback? =
        (adapter as? UEAdapter)?.attrDialogCallback
}