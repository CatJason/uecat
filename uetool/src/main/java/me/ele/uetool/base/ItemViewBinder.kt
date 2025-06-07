package me.ele.uetool.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author: weishenhong [contact me.](mailto:weishenhong@bytedance.com)
 * @date: 2019-07-08 22:51
 */
interface ItemViewBinder<T, VH : RecyclerView.ViewHolder?> {
    fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: RecyclerView.Adapter<*>
    ): VH

    fun onBindViewHolder(holder: VH, item: T)
}


