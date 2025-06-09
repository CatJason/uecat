package me.ele.uetool.treeview.model

import android.content.Context
import android.view.View
import android.view.ViewGroup
import me.ele.uetool.R
import me.ele.uetool.treeview.view.AndroidTreeView
import me.ele.uetool.treeview.view.TreeNodeWrapperView
import java.util.*

class TreeNode(value: Any?) {
    private var mId = 0
    private var mLastId = 0
    private var mParent: TreeNode? = null
    var isSelected = false
    var isSelectable = true
    private val children: MutableList<TreeNode> = ArrayList()
    private var mViewHolder: BaseNodeViewHolder<*>? = null
    private var mClickListener: TreeNodeClickListener? = null
    private var mLongClickListener: TreeNodeLongClickListener? = null
    private var mValue: Any? = value
    var isExpanded = false

    companion object {
        const val NODES_ID_SEPARATOR = ":"

        fun root(): TreeNode {
            val root = TreeNode(null)
            root.isSelectable = false
            return root
        }
    }

    private fun generateId(): Int {
        return ++mLastId
    }

    fun addChild(childNode: TreeNode): TreeNode {
        childNode.mParent = this
        childNode.mId = generateId()
        children.add(childNode)
        return this
    }

    fun addChildren(vararg nodes: TreeNode): TreeNode {
        for (n in nodes) {
            addChild(n)
        }
        return this
    }

    fun addChildren(nodes: Collection<TreeNode>): TreeNode {
        for (n in nodes) {
            addChild(n)
        }
        return this
    }

    fun deleteChild(child: TreeNode): Int {
        for (i in children.indices) {
            if (child.mId == children[i].mId) {
                children.removeAt(i)
                return i
            }
        }
        return -1
    }

    fun getChildren(): List<TreeNode> {
        return Collections.unmodifiableList(children)
    }

    fun size(): Int {
        return children.size
    }

    fun getParent(): TreeNode? {
        return mParent
    }

    fun getId(): Int {
        return mId
    }

    fun isLeaf(): Boolean {
        return size() == 0
    }

    fun getValue(): Any? {
        return mValue
    }

    fun getPath(): String {
        val path = StringBuilder()
        var node: TreeNode? = this
        while (node?.mParent != null) {
            path.append(node.getId())
            node = node.mParent
            if (node?.mParent != null) {
                path.append(NODES_ID_SEPARATOR)
            }
        }
        return path.toString()
    }

    fun getLevel(): Int {
        var level = 0
        var root: TreeNode? = this
        while (root?.mParent != null) {
            root = root.mParent
            level++
        }
        return level
    }

    fun isLastChild(): Boolean {
        if (!isRoot()) {
            val parentSize = mParent!!.children.size
            if (parentSize > 0) {
                val parentChildren = mParent!!.children
                return parentChildren[parentSize - 1].mId == mId
            }
        }
        return false
    }

    fun setViewHolder(viewHolder: BaseNodeViewHolder<*>?): TreeNode {
        mViewHolder = viewHolder
        if (viewHolder != null) {
            viewHolder.setNode(this)
        }
        return this
    }

    fun setClickListener(listener: TreeNodeClickListener?): TreeNode {
        mClickListener = listener
        return this
    }

    fun getClickListener(): TreeNodeClickListener? {
        return mClickListener
    }

    fun setLongClickListener(listener: TreeNodeLongClickListener?): TreeNode {
        mLongClickListener = listener
        return this
    }

    fun getLongClickListener(): TreeNodeLongClickListener? {
        return mLongClickListener
    }

    fun getViewHolder(): BaseNodeViewHolder<*>? {
        return mViewHolder
    }

    fun isFirstChild(): Boolean {
        if (!isRoot()) {
            val parentChildren = mParent!!.children
            return parentChildren[0].mId == mId
        }
        return false
    }

    fun isRoot(): Boolean {
        return mParent == null
    }

    fun getRoot(): TreeNode? {
        var root: TreeNode? = this
        while (root?.mParent != null) {
            root = root.mParent
        }
        return root
    }

    interface TreeNodeClickListener {
        fun onClick(node: TreeNode, value: Any?)
    }

    interface TreeNodeLongClickListener {
        fun onLongClick(node: TreeNode, value: Any?): Boolean
    }

    abstract class BaseNodeViewHolder<E>(protected val context: Context) {
        protected var tView: AndroidTreeView? = null
        private var mNode: TreeNode? = null
        private var mView: View? = null
        var containerStyle = 0

        fun setNode(node: TreeNode) {
            mNode = node
        }

        fun getView(): View? {
            if (mView != null) {
                return mView
            }
            val nodeView = getNodeView()
            val nodeWrapperView = TreeNodeWrapperView(nodeView.context, containerStyle)
            nodeWrapperView.insertNodeView(nodeView)
            mView = nodeWrapperView
            return mView
        }

        fun setTreeViev(treeViev: AndroidTreeView) {
            tView = treeViev
        }

        fun getTreeView(): AndroidTreeView? {
            return tView
        }

        fun getNodeView(): View {
            return createNodeView(mNode, mNode?.getValue() as E)
        }

        open fun getNodeItemsView(): ViewGroup {
            return getView()!!.findViewById(R.id.node_items)
        }

        fun isInitialized(): Boolean {
            return mView != null
        }

        abstract fun createNodeView(node: TreeNode?, value: E): View

        open fun toggle(active: Boolean) {
            // empty
        }

        open fun toggleSelectionMode(editModeEnabled: Boolean) {
            // empty
        }

        // Java兼容的getter
        fun getValue(): Any? {
            return mNode?.getValue()
        }
    }
}