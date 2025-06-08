package me.ele.uetool

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.ViewGroup

fun getSubMenu(resources: Resources, context: Context): UETSubMenu.SubMenu {
    return UETSubMenu.SubMenu(
        resources.getString(R.string.uet_scalpel),
        R.drawable.icon_2d_to_3d
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

        content.addView(
            ScalpelGLSurfaceView(context)
        )

        Log.i("ScalpelMenu", "3D视图层级菜单设置完成")
    }
}