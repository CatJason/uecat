package me.ele.uetool

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import me.ele.uetool.base.item.TextItem

class TextViewHolder(itemView: View) : BaseViewHolder<TextItem>(itemView) {
    private val vName: TextView = itemView.findViewById(R.id.name)
    private val vDetail: TextView = itemView.findViewById(R.id.detail)

    companion object {
        fun newInstance(parent: ViewGroup): TextViewHolder {
            return TextViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.uet_cell_text, parent, false)
            )
        }
    }

    override fun bindView(textItem: TextItem) {
        super.bindView(textItem)
        vName.text = textItem.name
        val detail = textItem.detail

        textItem.onClickListener?.let { onClickListener ->
            vDetail.text = Html.fromHtml("<u>$detail</u>")
            vDetail.setOnClickListener(onClickListener)
        } ?: run {
            vDetail.text = detail
            if (textItem.isEnableCopy) {
                vDetail.setOnClickListener {
                    clipText(detail)
                }
            }
        }
    }

    private fun clipText(text: String) {
        // 实现复制文本到剪贴板的逻辑
        val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(itemView.context, "已复制", Toast.LENGTH_SHORT).show()
    }
}