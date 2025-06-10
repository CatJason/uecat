package me.ele.uetool

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import me.ele.uetool.base.dip2px
import me.ele.uetool.base.item.BitmapItem
import kotlin.math.min

class BitmapInfoViewHolder(itemView: View) : AttrsDialog.Adapter.BaseViewHolder<BitmapItem>(itemView) {
    private val imageHeight = dip2px(58f)
    private val vName: TextView = itemView.findViewById(R.id.name)
    private val vImage: ImageView = itemView.findViewById(R.id.image)
    private val vInfo: TextView = itemView.findViewById(R.id.info)

    companion object {
        fun newInstance(parent: ViewGroup): BitmapInfoViewHolder {
            return BitmapInfoViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.uet_cell_bitmap_info, parent, false)
            )
        }
    }

    override fun bindView(bitmapItem: BitmapItem) {
        super.bindView(bitmapItem)

        vName.text = bitmapItem.name
        val bitmap = bitmapItem.bitmap?: return

        val height = min(bitmap.height, imageHeight)
        val width = (height.toFloat() / bitmap.height * bitmap.width).toInt()

        val layoutParams = vImage.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        vImage.layoutParams = layoutParams
        vImage.setImageBitmap(bitmap)
        vInfo.text = "${bitmap.width}px*${bitmap.height}px"
    }
}