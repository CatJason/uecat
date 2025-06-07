package me.ele.uetool.attrdialog

import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.AttrsDialog
import me.ele.uetool.base.ItemViewBinder
import me.ele.uetool.base.item.Item

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 23:37
 */
abstract class AttrsDialogItemViewBinder<T : Item, VH : AttrsDialog.Adapter.BaseViewHolder<T>> : ItemViewBinder<T, VH> {

    protected fun getAttrDialogCallback(adapter: RecyclerView.Adapter<*>): AttrsDialog.AttrDialogCallback? =
        (adapter as? AttrsDialog.Adapter)?.attrDialogCallback
}