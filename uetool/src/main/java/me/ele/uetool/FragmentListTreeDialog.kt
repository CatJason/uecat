package me.ele.uetool

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import me.ele.uetool.treeview.model.TreeNode
import me.ele.uetool.treeview.view.AndroidTreeView

class FragmentListTreeDialog(context: Context) : Dialog(context), Provider {

    private lateinit var containerView: ViewGroup
    private lateinit var regionView: RegionView

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.uet_dialog_fragment_list_tree)

        containerView = findViewById(R.id.container)
        regionView = findViewById(R.id.region)
        val checkBox = findViewById<CheckBox>(R.id.checkbox)

        createTree(checkBox.isChecked)

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            createTree(isChecked)
        }
    }

    private fun createTree(showPackageName: Boolean) {
        val root = TreeNode.root()

        val activity = UETool.getTargetActivity()
        if (activity is FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            createTreeNode(root, fragmentManager, showPackageName)
        }

        containerView.removeAllViews()

        val tView = AndroidTreeView(context, root).apply {
            setDefaultAnimation(true)
            setUse2dScroll(true)
            setDefaultContainerStyle(R.style.uet_TreeNodeStyleCustom)
        }
        containerView.addView(tView.view)

        tView.expandAll()
    }

    private fun createTreeNode(rootNode: TreeNode, fragmentManager: FragmentManager, showPackageName: Boolean): TreeNode {
        for (fragment in fragmentManager.fragments) {
            val node = TreeNode(TreeItem(fragment, showPackageName)).setViewHolder(
                TreeItemVH(context, this)
            )
            val childManager = fragment.childFragmentManager
            rootNode.addChild(createTreeNode(node, childManager, showPackageName))
        }
        return rootNode
    }

    override fun onStart() {
        super.onStart()
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onClickTreeItem(rectF: RectF) {
        regionView.drawRegion(rectF)
    }

    class TreeItemVH(context: Context, private val provider: Provider) : TreeNode.BaseNodeViewHolder<TreeItem>(context) {

        private lateinit var nameView: TextView
        private lateinit var arrowView: ImageView

        override fun createNodeView(node: TreeNode, value: TreeItem): View {
            val view = LayoutInflater.from(context).inflate(R.layout.uet_cell_tree, null, false)

            nameView = view.findViewById(R.id.name)
            arrowView = view.findViewById(R.id.arrow)

            nameView.text = Html.fromHtml(value.name)

            value.rectF?.let {
                nameView.setOnClickListener { _ ->
                    provider.onClickTreeItem(it)
                }
            }

            return view
        }

        override fun toggle(active: Boolean) {
            super.toggle(active)
            arrowView.animate()
                .setDuration(200)
                .rotation(if (active) 90f else 0f)
                .start()
        }
    }

    class TreeItem(fragment: Fragment, showPackageName: Boolean) {
        val name: String
        val rectF: RectF?

        init {
            name = initName(fragment, showPackageName)
            rectF = initRect(fragment)
        }

        private fun initName(fragment: Fragment, showPackageName: Boolean): String {
            val sb = StringBuilder().apply {
                append(if (showPackageName) fragment.javaClass.name else fragment.javaClass.simpleName)
                append("[visible=${fragment.isVisible}, hashCode=${fragment.hashCode()}]")
            }
            return if (fragment.isVisible) "<u>$sb</u>" else sb.toString()
        }

        private fun initRect(fragment: Fragment): RectF? {
            return if (fragment.isVisible) {
                val view = fragment.view ?: return null
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                RectF(
                    location[0].toFloat(),
                    location[1].toFloat(),
                    (location[0] + view.width).toFloat(),
                    (location[1] + view.height).toFloat()
                )
            } else {
                null
            }
        }
    }
}