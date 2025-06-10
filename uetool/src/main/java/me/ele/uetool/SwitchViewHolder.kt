package me.ele.uetool

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import me.ele.uetool.base.item.SwitchItem

class SwitchViewHolder(itemView: View, private val callback: AttrDialogCallback?) : AttrsDialog.Adapter.BaseViewHolder<SwitchItem>(itemView) {
    private val vName: TextView = itemView.findViewById(R.id.name)
    private val vSwitch: Switch = itemView.findViewById(R.id.switch_view)

    init {
        vSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            try {
                when (item?.type) {
                    SwitchItem.Type.TYPE_MOVE -> {
                        if (isChecked) {
                            callback?.enableMove()
                        }
                        return@setOnCheckedChangeListener
                    }
                    SwitchItem.Type.TYPE_SHOW_VALID_VIEWS -> {
                        if (item?.isChecked != isChecked) {
                            item?.isChecked = isChecked
                            callback?.showValidViews(adapterPosition, isChecked)
                        }
                        return@setOnCheckedChangeListener
                    }
                    else -> {
                        if (item?.element?.view is TextView) {
                            val textView = item.element.view as TextView
                            if (item.type == SwitchItem.Type.TYPE_IS_BOLD) {
                                val tf = Typeface.create(textView.typeface, if (isChecked) Typeface.BOLD else Typeface.NORMAL)
                                textView.typeface = tf
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup, callback: AttrDialogCallback?): SwitchViewHolder {
            return SwitchViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.uet_cell_switch, parent, false),
                callback
            )
        }
    }

    override fun bindView(switchItem: SwitchItem) {
        super.bindView(switchItem)
        vName.text = switchItem.name
        vSwitch.isChecked = switchItem.isChecked
    }
}