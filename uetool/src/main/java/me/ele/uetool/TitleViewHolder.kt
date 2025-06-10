package me.ele.uetool

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.ele.uetool.base.item.TitleItem

class TitleViewHolder(itemView: View) : AttrsDialog.Adapter.BaseViewHolder<TitleItem>(itemView) {
    private val vTitle: TextView = itemView.findViewById(R.id.title)

    companion object {
        fun newInstance(parent: ViewGroup): TitleViewHolder {
            return TitleViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.uet_cell_title, parent, false)
            )
        }
    }

    override fun bindView(titleItem: TitleItem) {
        super.bindView(titleItem)
        vTitle.text = titleItem.name
    }
}