package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.AttrsDialog
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.AddMinusEditItem

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 23:46
 */
class AddMinusEditTextItemBinder : AttrsDialogItemViewBinder<AddMinusEditItem, AttrsDialog.Adapter.AddMinusEditViewHolder>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ) = AttrsDialog.Adapter.AddMinusEditViewHolder.newInstance(parent)

    override fun onBindViewHolder(
        holder: AttrsDialog.Adapter.AddMinusEditViewHolder,
        item: AddMinusEditItem
    ) {
        holder.bindView(item)
    }
}