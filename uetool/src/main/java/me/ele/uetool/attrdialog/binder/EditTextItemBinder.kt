package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.EditTextViewHolder
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.EditTextItem

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 23:46
 */
class EditTextItemBinder : AttrsDialogItemViewBinder<EditTextItem, EditTextViewHolder<EditTextItem>>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): EditTextViewHolder<EditTextItem> {
        return EditTextViewHolder.newInstance(parent)
    }

    override fun onBindViewHolder(
        holder: EditTextViewHolder<EditTextItem>,
        item: EditTextItem
    ) {
        holder.bindView(item)
    }
}