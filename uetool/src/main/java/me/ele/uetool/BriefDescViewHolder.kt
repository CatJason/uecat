package me.ele.uetool

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.ele.uetool.base.item.BriefDescItem
import me.ele.uetool.cat.extractAfterLastDot
import me.ele.uetool.cat.getViewLayer

class BriefDescViewHolder(itemView: View, private val callback: AttrDialogCallback?) : AttrsDialog.Adapter.BaseViewHolder<BriefDescItem>(itemView) {
    private val vDesc: TextView = itemView as TextView

    init {
        vDesc.setOnClickListener {
            callback?.selectView(item.element)
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup, callback: AttrDialogCallback?): BriefDescViewHolder {
            return BriefDescViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.uet_cell_brief_view_desc, parent, false),
                callback
            )
        }

        private fun isSystemClass(view: View?): Boolean {
            if (view == null) return false
            val className = view.javaClass.name
            return className.startsWith("android.") || className.startsWith("com.android")
        }
    }

    override fun bindView(briefDescItem: BriefDescItem) {
        super.bindView(briefDescItem)
        val view = briefDescItem.element.view
        val resName = getResourceName(view.id)

        // Create styled text
        val spannableText = buildDisplayTextWithStyles(view, resName)

        // Apply to TextView
        vDesc.text = spannableText
        vDesc.isSelected = briefDescItem.isSelected
        vDesc.textSize = 10f
    }

    private fun buildDisplayTextWithStyles(view: View, resName: String?): SpannableString {
        val sb = StringBuilder()

        // Add indentation
        val numberOfSpaces = getViewLayer(view) * 2
        sb.append("  ".repeat(numberOfSpaces.coerceAtLeast(0)))

        // Add class info
        sb.append(if (numberOfSpaces > 0) "└ " else "")
            .append(if (isSystemClass(view)) "[系统] " else "[自定义] ")
            .append(extractAfterLastDot(view.javaClass.name))

        // Add resource ID if available
        if (!resName.isNullOrEmpty()) {
            sb.append(" (@+id/").append(resName).append(")")
        } else {
            sb.append(" (未设置 id)")
        }

        return applySpannableStyles(sb.toString(), resName)
    }

    private fun applySpannableStyles(text: String, resName: String?): SpannableString {
        val spannableString = SpannableString(text)

        if (!resName.isNullOrEmpty()) {
            val start = text.indexOf("(@+id/")
            val end = start + resName.length + 6 // Include "(@+id/" and ")"
            spannableString.setSpan(
                ForegroundColorSpan(Color.GREEN),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            val start = text.indexOf("(未设置 id)")
            val end = start + "(未设置 id)".length
            spannableString.setSpan(
                StrikethroughSpan(),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(Color.YELLOW),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannableString
    }
}