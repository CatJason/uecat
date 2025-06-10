package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.AddMinusEditViewHolder
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.AddMinusEditItem

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 23:46
 */
class AddMinusEditTextItemBinder : AttrsDialogItemViewBinder<AddMinusEditItem, AddMinusEditViewHolder>() {

    override fun onBindViewHolder(
        holder: AddMinusEditViewHolder,
        item: AddMinusEditItem
    ) {
        holder.bindView(item)
    }

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): AddMinusEditViewHolder {
        return AddMinusEditViewHolder.newInstance(parent)
    }
}