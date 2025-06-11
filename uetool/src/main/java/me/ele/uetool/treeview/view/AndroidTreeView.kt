package me.ele.uetool.treeview.view

import android.content.Context
import android.text.TextUtils
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.ScrollView
import me.ele.uetool.R
import me.ele.uetool.treeview.holder.SimpleViewHolder
import me.ele.uetool.treeview.model.TreeNode
import java.util.*

class AndroidTreeView(private val mContext: Context) {
    private val NODES_PATH_SEPARATOR = ";"

    var mRoot: TreeNode? = null
    private var applyForRoot = false
    private var containerStyle = 0
    private var defaultViewHolderClass: Class<out TreeNode.BaseNodeViewHolder<*>> = SimpleViewHolder::class.java
    private var nodeClickListener: TreeNode.TreeNodeClickListener? = null
    private var nodeLongClickListener: TreeNode.TreeNodeLongClickListener? = null
    private var mSelectionModeEnabled = false
    private var mUseDefaultAnimation = false
    private var use2dScroll = false
    private var enableAutoToggle = true

    constructor(context: Context, root: TreeNode) : this(context) {
        mRoot = root
    }

    fun setRoot(root: TreeNode) {
        this.mRoot = root
    }

    fun setDefaultAnimation(defaultAnimation: Boolean) {
        this.mUseDefaultAnimation = defaultAnimation
    }

    fun setDefaultContainerStyle(style: Int) {
        setDefaultContainerStyle(style, false)
    }

    fun setDefaultContainerStyle(style: Int, applyForRoot: Boolean) {
        containerStyle = style
        this.applyForRoot = applyForRoot
    }

    fun setUse2dScroll(use2dScroll: Boolean) {
        this.use2dScroll = use2dScroll
    }

    fun is2dScrollEnabled(): Boolean {
        return use2dScroll
    }

    fun setUseAutoToggle(enableAutoToggle: Boolean) {
        this.enableAutoToggle = enableAutoToggle
    }

    fun isAutoToggleEnabled(): Boolean {
        return enableAutoToggle
    }

    fun setDefaultViewHolder(viewHolder: Class<out TreeNode.BaseNodeViewHolder<*>>) {
        defaultViewHolderClass = viewHolder
    }

    fun setDefaultNodeClickListener(listener: TreeNode.TreeNodeClickListener?) {
        nodeClickListener = listener
    }

    fun setDefaultNodeLongClickListener(listener: TreeNode.TreeNodeLongClickListener?) {
        nodeLongClickListener = listener
    }

    fun expandAll() {
        mRoot?.let { expandNode(it, true) }
    }

    fun collapseAll() {
        mRoot?.getChildren()?.forEach { collapseNode(it, true) }
    }

    fun getView(style: Int): View {
        val view: ViewGroup = if (style > 0) {
            val newContext = ContextThemeWrapper(mContext, style)
            if (use2dScroll) TwoDScrollView(newContext) else ScrollView(newContext)
        } else {
            if (use2dScroll) TwoDScrollView(mContext) else ScrollView(mContext)
        }

        val containerContext = if (containerStyle != 0 && applyForRoot) {
            ContextThemeWrapper(mContext, containerStyle)
        } else {
            mContext
        }

        val viewTreeItems = LinearLayout(containerContext, null, containerStyle).apply {
            id = R.id.tree_items
            orientation = LinearLayout.VERTICAL
        }

        view.addView(viewTreeItems)

        mRoot?.setViewHolder(object : TreeNode.BaseNodeViewHolder<Any>(mContext) {
            override fun getNodeItemsView(): ViewGroup {
                return viewTreeItems
            }

            override fun createNodeView(node: TreeNode?, value: Any): View? = null
        })

        mRoot?.let { expandNode(it, false) }
        return view
    }

    fun getView(): View {
        return getView(-1)
    }

    fun expandLevel(level: Int) {
        mRoot?.getChildren()?.forEach { expandLevel(it, level) }
    }

    private fun expandLevel(node: TreeNode, level: Int) {
        if (node.getLevel() <= level) {
            expandNode(node, false)
        }
        node.getChildren().forEach { expandLevel(it, level) }
    }

    fun expandNode(node: TreeNode) {
        expandNode(node, false)
    }

    fun collapseNode(node: TreeNode) {
        collapseNode(node, false)
    }

    fun getSaveState(): String {
        val builder = StringBuilder()
        mRoot?.let { getSaveState(it, builder) }
        if (builder.isNotEmpty()) {
            builder.setLength(builder.length - 1)
        }
        return builder.toString()
    }

    fun restoreState(saveState: String?) {
        if (!TextUtils.isEmpty(saveState)) {
            collapseAll()
            val openNodesArray = saveState!!.split(NODES_PATH_SEPARATOR.toRegex()).toTypedArray()
            val openNodes = HashSet(listOf(*openNodesArray))
            mRoot?.let { restoreNodeState(it, openNodes) }
        }
    }

    private fun restoreNodeState(node: TreeNode, openNodes: Set<String>) {
        node.getChildren().forEach { n ->
            if (openNodes.contains(n.getPath())) {
                expandNode(n)
                restoreNodeState(n, openNodes)
            }
        }
    }

    private fun getSaveState(root: TreeNode, sBuilder: StringBuilder) {
        root.getChildren().forEach { node ->
            if (node.isExpanded) {
                sBuilder.append(node.getPath())
                sBuilder.append(NODES_PATH_SEPARATOR)
                getSaveState(node, sBuilder)
            }
        }
    }

    fun toggleNode(node: TreeNode) {
        if (node.isExpanded) {
            collapseNode(node, false)
        } else {
            expandNode(node, false)
        }
    }

    private fun collapseNode(node: TreeNode, includeSubnodes: Boolean) {
        node.isExpanded = false
        val nodeViewHolder = getViewHolderForNode(node)

        if (mUseDefaultAnimation) {
            collapse(nodeViewHolder.getNodeItemsView())
        } else {
            nodeViewHolder.getNodeItemsView().visibility = View.GONE
        }
        nodeViewHolder.toggle(false)
        if (includeSubnodes) {
            node.getChildren().forEach { n -> collapseNode(n, includeSubnodes) }
        }
    }

    private fun expandNode(node: TreeNode, includeSubnodes: Boolean) {
        node.isExpanded = true
        val parentViewHolder = getViewHolderForNode(node)
        parentViewHolder.getNodeItemsView().removeAllViews()

        parentViewHolder.toggle(true)

        node.getChildren().forEach { n ->
            addNode(parentViewHolder.getNodeItemsView(), n)

            if (n.isExpanded || includeSubnodes) {
                expandNode(n, includeSubnodes)
            }
        }

        if (mUseDefaultAnimation) {
            expand(parentViewHolder.getNodeItemsView())
        } else {
            parentViewHolder.getNodeItemsView().visibility = View.VISIBLE
        }
    }

    private fun addNode(container: ViewGroup, n: TreeNode) {
        val viewHolder = getViewHolderForNode(n)
        val nodeView = viewHolder.getView()
        container.addView(nodeView)
        if (mSelectionModeEnabled) {
            viewHolder.toggleSelectionMode(mSelectionModeEnabled)
        }

        nodeView?.setOnClickListener {
            n.getClickListener()?.onClick(n, n.getValue()) ?: nodeClickListener?.onClick(n, n.getValue())
            if (enableAutoToggle) {
                toggleNode(n)
            }
        }

        nodeView?.setOnLongClickListener {
            val handled = n.getLongClickListener()?.onLongClick(n, n.getValue()) ?:
            nodeLongClickListener?.onLongClick(n, n.getValue()) ?: false
            if (enableAutoToggle) {
                toggleNode(n)
            }
            handled
        }
    }

    // Selection methods
    fun setSelectionModeEnabled(selectionModeEnabled: Boolean) {
        if (!selectionModeEnabled) {
            deselectAll()
        }
        mSelectionModeEnabled = selectionModeEnabled

        mRoot?.getChildren()?.forEach { toggleSelectionMode(it, selectionModeEnabled) }
    }

    fun <E> getSelectedValues(clazz: Class<E>): List<E> {
        val result = ArrayList<E>()
        val selected = selected
        selected.forEach { n ->
            val value = n.getValue()
            if (value != null && value.javaClass == clazz) {
                @Suppress("UNCHECKED_CAST")
                result.add(value as E)
            }
        }
        return result
    }

    fun isSelectionModeEnabled(): Boolean {
        return mSelectionModeEnabled
    }

    private fun toggleSelectionMode(parent: TreeNode, mSelectionModeEnabled: Boolean) {
        toogleSelectionForNode(parent, mSelectionModeEnabled)
        if (parent.isExpanded) {
            parent.getChildren().forEach { node -> toggleSelectionMode(node, mSelectionModeEnabled) }
        }
    }

    val selected: List<TreeNode>
        get() = if (mSelectionModeEnabled) {
            mRoot?.let { getSelected(it) } ?: emptyList()
        } else {
            emptyList()
        }

    private fun getSelected(parent: TreeNode): List<TreeNode> {
        val result = ArrayList<TreeNode>()
        parent.getChildren().forEach { n ->
            if (n.isSelectable && n.isSelected) {
                result.add(n)
            }
            result.addAll(getSelected(n))
        }
        return result
    }

    fun selectAll(skipCollapsed: Boolean) {
        makeAllSelection(true, skipCollapsed)
    }

    fun deselectAll() {
        makeAllSelection(false, false)
    }

    private fun makeAllSelection(selected: Boolean, skipCollapsed: Boolean) {
        if (mSelectionModeEnabled) {
            mRoot?.getChildren()?.forEach { selectNode(it, selected, skipCollapsed) }
        }
    }

    fun selectNode(node: TreeNode, selected: Boolean) {
        if (mSelectionModeEnabled) {
            node.isSelected = selected
            toogleSelectionForNode(node, true)
        }
    }

    private fun selectNode(parent: TreeNode, selected: Boolean, skipCollapsed: Boolean) {
        parent.isSelected = selected
        toogleSelectionForNode(parent, true)
        val toContinue = if (skipCollapsed) parent.isExpanded else true
        if (toContinue) {
            parent.getChildren().forEach { node -> selectNode(node, selected, skipCollapsed) }
        }
    }

    private fun toogleSelectionForNode(node: TreeNode, makeSelectable: Boolean) {
        val holder = getViewHolderForNode(node)
        if (holder.isInitialized()) {
            getViewHolderForNode(node).toggleSelectionMode(makeSelectable)
        }
    }

    private fun getViewHolderForNode(node: TreeNode): TreeNode.BaseNodeViewHolder<*> {
        var viewHolder = node.getViewHolder()
        if (viewHolder == null) {
            try {
                val objectInstance = defaultViewHolderClass.getConstructor(Context::class.java).newInstance(mContext)
                viewHolder = objectInstance
                node.setViewHolder(viewHolder)
            } catch (e: Exception) {
                throw RuntimeException("Could not instantiate class $defaultViewHolderClass")
            }
        }
        viewHolder!!
        if (viewHolder.containerStyle <= 0) {
            viewHolder.containerStyle = containerStyle
        }
        if (viewHolder.getTreeView() == null) {
            viewHolder.setTreeViev(this)
        }
        return viewHolder
    }

    companion object {
        private fun expand(v: View) {
            v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            val targetHeight = v.measuredHeight

            v.layoutParams.height = 0
            v.visibility = View.VISIBLE
            val a = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    v.layoutParams.height = if (interpolatedTime == 1f)
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    else
                        (targetHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }

            // 1dp/ms
            a.duration = (targetHeight / v.context.resources.displayMetrics.density).toLong()
            v.startAnimation(a)
        }

        private fun collapse(v: View) {
            val initialHeight = v.measuredHeight

            val a = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (interpolatedTime == 1f) {
                        v.visibility = View.GONE
                    } else {
                        v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                        v.requestLayout()
                    }
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }

            // 1dp/ms
            a.duration = (initialHeight / v.context.resources.displayMetrics.density).toLong()
            v.startAnimation(a)
        }
    }

    // Add / Remove
    fun addNode(parent: TreeNode, nodeToAdd: TreeNode) {
        parent.addChild(nodeToAdd)
        if (parent.isExpanded) {
            val parentViewHolder = getViewHolderForNode(parent)
            addNode(parentViewHolder.getNodeItemsView(), nodeToAdd)
        }
    }

    fun removeNode(node: TreeNode) {
        node.getParent()?.let { parent ->
            val index = parent.deleteChild(node)
            if (parent.isExpanded && index >= 0) {
                val parentViewHolder = getViewHolderForNode(parent)
                parentViewHolder.getNodeItemsView().removeViewAt(index)
            }
        }
    }
}