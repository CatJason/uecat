package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.AttrsDialog
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.BriefDescItem

class BriefDescItemBinder : AttrsDialogItemViewBinder<BriefDescItem, AttrsDialog.Adapter.BriefDescViewHolder>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): AttrsDialog.Adapter.BriefDescViewHolder {
        return AttrsDialog.Adapter.BriefDescViewHolder.newInstance(parent, getAttrDialogCallback(adapter))
    }

    override fun onBindViewHolder(
        holder: AttrsDialog.Adapter.BriefDescViewHolder,
        item: BriefDescItem
    ) {
        holder.bindView(item)
    }
}