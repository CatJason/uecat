package me.ele.uetool

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.base.item.Item

abstract class BaseViewHolder<T : Item>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // 对外提供只读访问（protected set 允许子类修改）
    var item: T? = null
        protected set

    open fun bindView(t: T) {
        item = t
    }
}