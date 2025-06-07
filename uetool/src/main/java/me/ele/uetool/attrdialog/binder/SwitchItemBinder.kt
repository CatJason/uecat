package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.AttrsDialog
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.SwitchItem

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 23:46
 */
class SwitchItemBinder : AttrsDialogItemViewBinder<SwitchItem, AttrsDialog.Adapter.SwitchViewHolder>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): AttrsDialog.Adapter.SwitchViewHolder {
        return AttrsDialog.Adapter.SwitchViewHolder.newInstance(parent, getAttrDialogCallback(adapter))
    }

    override fun onBindViewHolder(
        holder: AttrsDialog.Adapter.SwitchViewHolder,
        item: SwitchItem
    ) {
        holder.bindView(item)
    }
}