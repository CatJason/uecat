package me.ele.uetool

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import me.ele.uetool.base.dip2px
import me.ele.uetool.base.item.EditTextItem
import kotlin.math.abs

open class EditTextViewHolder<T : EditTextItem>(itemView: View) : BaseViewHolder<T>(itemView) {
    protected val vName: TextView = itemView.findViewById(R.id.name)
    protected val vDetail: EditText = itemView.findViewById(R.id.detail)
    private val vColor: View? = itemView.findViewById(R.id.color)

    protected val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            try {
                val currentItem = item ?: return
                val currentView = currentItem.element.view
                val inputText = s?.toString() ?: ""

                when (currentItem.type) {
                    EditTextItem.Type.TYPE_TEXT -> {
                        val textView = currentView as TextView
                        if (textView.text.toString() != inputText) {
                            textView.text = inputText
                        }
                    }
                    EditTextItem.Type.TYPE_TEXT_SIZE -> {
                        val textView = currentView as TextView
                        val textSize = inputText.toFloatOrNull() ?: return
                        if (abs(textView.textSize - textSize) >= 0.1f) {
                            textView.textSize = textSize
                        }
                    }
                    EditTextItem.Type.TYPE_TEXT_COLOR -> {
                        val textView = currentView as TextView
                        try {
                            val color = Color.parseColor(inputText)
                            if (textView.currentTextColor != color) {
                                vColor?.setBackgroundColor(color)
                                textView.setTextColor(color)
                            }
                        } catch (e: IllegalArgumentException) {
                            e.printStackTrace()
                        }
                    }
                    EditTextItem.Type.TYPE_WIDTH -> {
                        val width = inputText.toIntOrNull() ?: return
                        if (abs(currentView.width - width) >= dip2px(1f)) {
                            currentView.layoutParams = currentView.layoutParams.apply {
                                this.width = dip2px(width.toFloat())
                            }
                            currentView.requestLayout()
                        }
                    }
                    EditTextItem.Type.TYPE_HEIGHT -> {
                        val height = inputText.toIntOrNull()?: return
                        if (abs(currentView.height - height) >= dip2px(1f)) {
                            currentView.layoutParams = currentView.layoutParams.apply {
                                this.height = dip2px(height.toFloat())
                            }
                            currentView.requestLayout()
                        }
                    }
                    EditTextItem.Type.TYPE_PADDING_LEFT -> {
                        val paddingLeft = inputText.toIntOrNull() ?: return
                        if (abs(currentView.paddingLeft - paddingLeft) >= dip2px(1f)) {
                            currentView.setPadding(
                                dip2px(paddingLeft.toFloat()),
                                currentView.paddingTop,
                                currentView.paddingRight,
                                currentView.paddingBottom
                            )
                        }
                    }
                    EditTextItem.Type.TYPE_PADDING_RIGHT -> {
                        val paddingRight = inputText.toIntOrNull() ?: return
                        if (abs(currentView.paddingRight - paddingRight) >= dip2px(1f)) {
                            currentView.setPadding(
                                currentView.paddingLeft,
                                currentView.paddingTop,
                                dip2px(paddingRight.toFloat()),
                                currentView.paddingBottom
                            )
                        }
                    }
                    EditTextItem.Type.TYPE_PADDING_TOP -> {
                        val paddingTop = inputText.toIntOrNull() ?: return
                        if (abs(currentView.paddingTop - paddingTop) >= dip2px(1f)) {
                            currentView.setPadding(
                                currentView.paddingLeft,
                                dip2px(paddingTop.toFloat()),
                                currentView.paddingRight,
                                currentView.paddingBottom
                            )
                        }
                    }
                    EditTextItem.Type.TYPE_PADDING_BOTTOM -> {
                        val paddingBottom = inputText.toIntOrNull() ?: return
                        if (abs(currentView.paddingBottom - paddingBottom) >= dip2px(1f)) {
                            currentView.setPadding(
                                currentView.paddingLeft,
                                currentView.paddingTop,
                                currentView.paddingRight,
                                dip2px(paddingBottom.toFloat())
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    init {
        vDetail.addTextChangedListener(textWatcher)
    }

    companion object {
        fun newInstance(parent: ViewGroup): EditTextViewHolder<EditTextItem> {
            return EditTextViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.uet_cell_edit_text, parent, false)
            )
        }
    }

    override fun bindView(editTextItem: T) {
        super.bindView(editTextItem)
        vName.text = editTextItem.name
        vDetail.setText(editTextItem.detail)

        vColor?.let { colorView ->
            try {
                Color.parseColor(editTextItem.detail) // 验证颜色是否有效
                colorView.setBackgroundColor(Color.parseColor(editTextItem.detail))
                colorView.visibility = View.VISIBLE
            } catch (e: IllegalArgumentException) {
                colorView.visibility = View.GONE
            }
        }
    }
}