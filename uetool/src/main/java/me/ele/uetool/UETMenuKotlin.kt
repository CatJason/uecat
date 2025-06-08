package me.ele.uetool

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.ViewGroup

fun getSubMenu(resources: Resources, context: Context): UETSubMenu.SubMenu {
    return UETSubMenu.SubMenu(
        resources.getString(R.string.uet_scalpel),
        R.drawable.uet_scalpel
    ) {
        // 获取当前Activity的DecorView
        Log.d("ScalpelMenu", "开始处理3D视图层级菜单")
        val decorView = getCurrentActivity()?.window?.decorView as? ViewGroup?: run {
            Log.w("ScalpelMenu", "无法获取DecorView，可能Activity不存在或已销毁")
            return@SubMenu
        }

        // 获取内容区域ViewGroup
        val content = decorView.findViewById<ViewGroup>(android.R.id.content).also {
            Log.d("ScalpelMenu", "找到内容区域ViewGroup: ${it.javaClass.simpleName}")
        }

        // 获取内容区域的第一个子视图
        val contentChild = content.getChildAt(0).also {
            if (it == null) {
                Log.w("ScalpelMenu", "内容区域没有子视图，无法创建3D效果")
            } else {
                Log.d("ScalpelMenu", "获取到根内容视图: ${it.javaClass.simpleName}")
            }
        } ?: return@SubMenu

        // 移除所有视图准备重建
        content.removeAllViews()
        Log.d("ScalpelMenu", "已清空内容区域视图")

        if (contentChild is ScalpelFrameLayout) {
            // 如果已经是Scalpel视图，则重置
            Log.d("ScalpelMenu", "检测到现有Scalpel布局，进行重置操作")
            content.addView(
                contentChild.apply {
                    getChildAt(0)?.also { child ->
                        Log.d("ScalpelMenu", "从Scalpel布局中移除子视图: ${child.javaClass.simpleName}")
                    }
                    removeAllViews()
                }
            )
        } else {
            // 创建新的Scalpel容器
            Log.d("ScalpelMenu", "创建新的ScalpelFrameLayout容器")
            content.addView(
                ScalpelFrameLayout(context).apply {
                    Log.d("ScalpelMenu", "初始化Scalpel参数: 启用3D交互和ID显示")
                    isLayerInteractionEnabled = true
                    setDrawIds(true)
                    addView(contentChild).also {
                        Log.d("ScalpelMenu", "将原始内容视图添加到Scalpel容器中")
                    }
                }
            )
        }

        Log.i("ScalpelMenu", "3D视图层级菜单设置完成")
    }
}