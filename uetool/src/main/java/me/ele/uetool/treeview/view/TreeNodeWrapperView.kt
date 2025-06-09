package me.ele.uetool.treeview.view

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import me.ele.uetool.R

class TreeNodeWrapperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val containerStyle: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var nodeItemsContainer: LinearLayout
    private lateinit var nodeContainer: ViewGroup

    init {
        initViews()
    }

    // Secondary constructor for Java compatibility
    constructor(context: Context, containerStyle: Int) : this(context, null, 0, containerStyle)

    private fun initViews() {
        orientation = VERTICAL

        nodeContainer = RelativeLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            id = R.id.node_header
        }

        val newContext = ContextThemeWrapper(context, containerStyle)
        nodeItemsContainer = LinearLayout(newContext, null, containerStyle).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            id = R.id.node_items
            orientation = VERTICAL
            visibility = View.GONE
        }

        addView(nodeContainer)
        addView(nodeItemsContainer)
    }

    fun insertNodeView(nodeView: View) {
        nodeContainer.addView(nodeView)
    }

    fun getNodeContainer(): ViewGroup = nodeContainer
}