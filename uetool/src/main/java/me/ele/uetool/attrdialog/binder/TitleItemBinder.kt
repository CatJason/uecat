package me.ele.uetool.attrdialog.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.AttrsDialog
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder
import me.ele.uetool.base.item.TitleItem

/**
 * @author weishenhong <a href="mailto:weishenhong@bytedance.com">contact me.</a>
 * @date 2019-07-08 23:46
 */
class TitleItemBinder : AttrsDialogItemViewBinder<TitleItem, AttrsDialog.Adapter.TitleViewHolder>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): AttrsDialog.Adapter.TitleViewHolder =
        AttrsDialog.Adapter.TitleViewHolder.newInstance(parent)

    override fun onBindViewHolder(
        holder: AttrsDialog.Adapter.TitleViewHolder,
        item: TitleItem
    ) {
        holder.bindView(item)
    }
}