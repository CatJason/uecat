package me.ele.uetool

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.ele.uetool.base.item.AddMinusEditItem

class AddMinusEditViewHolder(itemView: View) : EditTextViewHolder<AddMinusEditItem>(itemView) {
    private val vAdd: View = itemView.findViewById(R.id.add)
    private val vMinus: View = itemView.findViewById(R.id.minus)

    init {
        vAdd.setOnClickListener {
            try {
                val textSize = vDetail.text.toString().toIntOrNull() ?: 0
                vDetail.text = (textSize + 1).toString().toEditable()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        vMinus.setOnClickListener {
            try {
                val textSize = vDetail.text.toString().toIntOrNull() ?: 0
                if (textSize > 0) {
                    vDetail.text = (textSize - 1).toString().toEditable()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup): AddMinusEditViewHolder {
            return AddMinusEditViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.uet_cell_add_minus_edit, parent, false)
            )
        }
    }

    override fun bindView(editTextItem: AddMinusEditItem) {
        super.bindView(editTextItem)
    }

    // 扩展函数：String → Editable
    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
}
