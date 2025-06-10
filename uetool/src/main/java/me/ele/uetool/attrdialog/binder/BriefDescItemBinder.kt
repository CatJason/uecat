package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.BriefDescViewHolder
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.BriefDescItem

class BriefDescItemBinder : AttrsDialogItemViewBinder<BriefDescItem, BriefDescViewHolder>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): BriefDescViewHolder {
        return BriefDescViewHolder.newInstance(parent, getAttrDialogCallback(adapter))
    }

    override fun onBindViewHolder(
        holder: BriefDescViewHolder,
        item: BriefDescItem
    ) {
        holder.bindView(item)
    }
}