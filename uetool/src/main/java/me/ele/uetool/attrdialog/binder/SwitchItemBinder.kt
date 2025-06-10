package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.SwitchViewHolder
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.SwitchItem

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 23:46
 */
class SwitchItemBinder : AttrsDialogItemViewBinder<SwitchItem, SwitchViewHolder>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): SwitchViewHolder {
        return SwitchViewHolder.newInstance(parent, getAttrDialogCallback(adapter))
    }

    override fun onBindViewHolder(
        holder: SwitchViewHolder,
        item: SwitchItem
    ) {
        holder.bindView(item)
    }
}