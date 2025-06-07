package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.AttrsDialog
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.BitmapItem

class BitmapItemBinder : AttrsDialogItemViewBinder<BitmapItem, AttrsDialog.Adapter.BitmapInfoViewHolder>() {
    override fun onBindViewHolder(
        holder: AttrsDialog.Adapter.BitmapInfoViewHolder,
        item: BitmapItem
    ) {
        holder.bindView(item)
    }

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): AttrsDialog.Adapter.BitmapInfoViewHolder {
        return AttrsDialog.Adapter.BitmapInfoViewHolder.newInstance(parent)
    }
}