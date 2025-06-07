package me.ele.uetool

import android.content.Context
import android.content.res.Resources
import android.view.ViewGroup

fun getSubMenu(resources: Resources, context: Context): UETSubMenu.SubMenu {
    return UETSubMenu.SubMenu(
        resources.getString(R.string.uet_scalpel),
        R.drawable.uet_scalpel
    ) {
        val decorView = getCurrentActivity()?.window?.decorView as? ViewGroup?: return@SubMenu
        val content = decorView.findViewById<ViewGroup>(android.R.id.content)
        val contentChild = content.getChildAt(0)
        if (contentChild != null) {
            content.removeAllViews()
            if (contentChild is ScalpelFrameLayout) {
                content.addView(
                    contentChild.apply {
                        getChildAt(0)
                        removeAllViews()
                    }
                )
            } else {
                content.addView(
                    ScalpelFrameLayout(context).apply {
                        isLayerInteractionEnabled = true
                        setDrawIds(true)
                        addView(contentChild)
                    }
                )
            }
        }
    }
}