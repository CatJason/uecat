package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.AttrsDialog
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.TextItem

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 23:46
 */
class TextItemBinder : AttrsDialogItemViewBinder<TextItem, AttrsDialog.Adapter.TextViewHolder>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): AttrsDialog.Adapter.TextViewHolder {
        return AttrsDialog.Adapter.TextViewHolder.newInstance(parent)
    }

    override fun onBindViewHolder(
        holder: AttrsDialog.Adapter.TextViewHolder,
        item: TextItem
    ) {
        holder.bindView(item)
    }
}