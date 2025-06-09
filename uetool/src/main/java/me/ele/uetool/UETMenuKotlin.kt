package me.ele.uetool

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View
import android.view.ViewGroup

// 配置类用于自定义3D视图参数
data class ScalpelConfig(
    val drawIds: Boolean = true,
    val interactionEnabled: Boolean = true,
    val zAxisScale: Float = 1.5f,
    val tagIdentifier: String = "UET_SCALPEL_TAG"
)

fun getSubMenu(resources: Resources, context: Context): UETSubMenu.SubMenu {
    // 默认配置
    val config = ScalpelConfig()

    return UETSubMenu.SubMenu(
        resources.getString(R.string.uet_scalpel),
        R.drawable.icon_2d_to_3d
    ) {
        try {
            Log.d("ScalpelMenu", "开始处理3D视图切换")

            val activity = getCurrentActivity() ?: run {
                Log.w("ScalpelMenu", "无有效Activity")
                return@SubMenu
            }

            val decorView = activity.window?.decorView as? ViewGroup ?: run {
                Log.w("ScalpelMenu", "无法获取DecorView")
                return@SubMenu
            }

            val content = decorView.findViewById<ViewGroup>(android.R.id.content) ?: run {
                Log.w("ScalpelMenu", "未找到内容区域")
                return@SubMenu
            }

            // 检查是否已存在Scalpel容器
            content.findViewWithTag<ScalpelFrameLayout>(config.tagIdentifier)?.let { scalpel ->
                Log.d("ScalpelMenu", "检测到现有3D视图，执行恢复操作")
                restoreOriginalView(scalpel, content)
                return@SubMenu
            }

            // 创建新的3D视图
            setupScalpelView(content, context, config)

        } catch (e: Exception) {
            Log.e("ScalpelMenu", "3D视图切换失败", e)
        }
    }
}

/**
 * 恢复原始视图布局
 */
private fun restoreOriginalView(scalpel: ScalpelFrameLayout, content: ViewGroup) {
    // 获取原始视图
    val originalView = scalpel.getChildAt(0) ?: run {
        Log.w("ScalpelMenu", "3D容器中没有子视图")
        return
    }

    // 保存布局参数
    val layoutParams = scalpel.layoutParams

    // 移除3D容器
    scalpel.removeAllViews()
    content.removeView(scalpel)

    // 恢复原始视图
    content.addView(originalView, 0, layoutParams)

    Log.i("ScalpelMenu", "已恢复原始视图布局")
}

/**
 * 设置3D视图
 */
private fun setupScalpelView(content: ViewGroup, context: Context, config: ScalpelConfig) {
    // 获取根视图
    val rootView = content.getChildAt(0) ?: run {
        Log.w("ScalpelMenu", "内容区域无子视图")
        return
    }

    // 保存原始布局参数
    val originalParams = rootView.layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    // 移除原始视图
    content.removeView(rootView)

    // 创建并配置3D容器
    ScalpelFrameLayout(context).apply {
        tag = config.tagIdentifier
        isLayerInteractionEnabled = config.interactionEnabled
        setDrawIds(config.drawIds)

        // 设置Z轴缩放
        try {
            val field = javaClass.getDeclaredField("mZAxisScale")
            field.isAccessible = true
            field.setFloat(this, config.zAxisScale)
        } catch (e: Exception) {
            Log.w("ScalpelMenu", "设置Z轴缩放失败", e)
        }

        // 添加原始视图
        addView(rootView, originalParams)

        // 添加到内容区域
        content.addView(this, originalParams)

        Log.i("ScalpelMenu", "3D视图已启用 (drawIds=${config.drawIds}, interaction=${config.interactionEnabled})")
    }
}

/**
 * 扩展函数：简化查找视图操作
 */
private inline fun <reified T : View> ViewGroup.findViewWithTag(tagValue: Any): T? {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is T && child.tag == tagValue) {
            return child
        }
    }
    return null
}