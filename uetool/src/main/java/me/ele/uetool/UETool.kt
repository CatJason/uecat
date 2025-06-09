package me.ele.uetool

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import me.ele.uetool.attrdialog.AttrsDialogMultiTypePool
import me.ele.uetool.attrdialog.binder.AddMinusEditTextItemBinder
import me.ele.uetool.attrdialog.binder.BitmapItemBinder
import me.ele.uetool.attrdialog.binder.BriefDescItemBinder
import me.ele.uetool.attrdialog.binder.EditTextItemBinder
import me.ele.uetool.attrdialog.binder.SwitchItemBinder
import me.ele.uetool.attrdialog.binder.TextItemBinder
import me.ele.uetool.attrdialog.binder.TitleItemBinder
import me.ele.uetool.base.Application
import me.ele.uetool.base.ItemViewBinder
import me.ele.uetool.base.item.AddMinusEditItem
import me.ele.uetool.base.item.BitmapItem
import me.ele.uetool.base.item.BriefDescItem
import me.ele.uetool.base.item.EditTextItem
import me.ele.uetool.base.item.Item
import me.ele.uetool.base.item.SwitchItem
import me.ele.uetool.base.item.TextItem
import me.ele.uetool.base.item.TitleItem

/**
 * UETool 核心工具类
 * 提供视图调试菜单的显示/隐藏、属性提供器管理等功能
 */
object UETool {
    // region 线程安全集合
    // 过滤器类名集合（线程安全）
    private val filterClassesSet = CopyOnWriteArraySet<String>()

    // 属性提供器类名列表（线程安全），默认包含核心实现
    private val attrsProviderSet = CopyOnWriteArrayList<String>().apply {
        add(UETCore::class.java.name)          // 默认核心属性提供器
        add("me.ele.uetool.fresco.UETFresco") // Fresco 图片库支持
    }
    // endregion

    // region 弱引用对象
    // 当前目标Activity的弱引用（避免内存泄漏）
    @Volatile
    private var targetActivityRef: WeakReference<Activity>? = null

    // UETool菜单的弱引用
    @Volatile
    private var uetMenuRef: WeakReference<UETMenu>? = null
    // endregion

    // region 类型池初始化
    /**
     * 属性对话框多类型池（懒加载）
     * 注册了各种Item类型对应的ViewBinder
     */
    val attrsDialogMultiTypePool by lazy {
        AttrsDialogMultiTypePool().apply {
            // 注册各种Item类型与对应的ViewBinder
            register(AddMinusEditItem::class.java, AddMinusEditTextItemBinder()) // 加减编辑框
            register(BitmapItem::class.java, BitmapItemBinder())                 // 图片项
            register(BriefDescItem::class.java, BriefDescItemBinder())            // 简要描述
            register(EditTextItem::class.java, EditTextItemBinder())            // 编辑框
            register(SwitchItem::class.java, SwitchItemBinder())                 // 开关
            register(TextItem::class.java, TextItemBinder())                     // 文本
            register(TitleItem::class.java, TitleItemBinder())                   // 标题
        }
    }
    // endregion

    // region 公共API
    /**
     * 添加需要过滤的类（通过Class对象）
     */
    fun putFilterClass(clazz: Class<*>) = putFilterClassName(clazz.name)

    /**
     * 添加需要过滤的类（通过类名字符串）
     */
    fun putFilterClass(className: String) = putFilterClassName(className)

    /**
     * 注册属性对话框的Item类型与对应ViewBinder
     */
    fun <T : Item> registerAttrDialogItemViewBinder(clazz: Class<T>, binder: ItemViewBinder<T, *>) {
        attrsDialogMultiTypePool.register(clazz, binder)
    }

    /**
     * 添加属性提供器（通过Class对象）
     */
    fun putAttrsProviderClass(clazz: Class<*>) = putAttrsProviderClass(clazz.name)

    /**
     * 添加属性提供器（通过类名字符串）
     */
    fun putAttrsProviderClass(className: String) = putAttrsProviderClassName(className)

    /**
     * 显示UETool菜单（默认位置）
     */
    fun showUETMenu(): Boolean = showMenu()

    /**
     * 显示UETool菜单（指定Y轴位置）
     */
    fun showUETMenu(y: Int): Boolean = showMenu(y)

    /**
     * 隐藏UETool菜单
     * @return 菜单隐藏时的Y轴位置（用于下次恢复显示）
     */
    fun dismissUETMenu(): Int {
        uetMenuRef?.get()?.let { menu ->
            val y = menu.dismiss()  // 获取当前Y位置
            uetMenuRef = null        // 清除引用
            return y
        }
        return -1  // 返回无效位置
    }

    /**
     * 获取当前所有过滤类名
     */
    fun getFilterClasses(): Set<String> = filterClassesSet.toSet()

    /**
     * 获取当前目标Activity
     */
    fun getTargetActivity(): Activity? = targetActivityRef?.get()

    /**
     * 获取所有属性提供器类名
     */
    fun getAttrsProvider(): List<String> = attrsProviderSet.toList()

    /**
     * 设置当前目标Activity
     */
    fun setTargetActivity(targetActivity: Activity?) {
        targetActivityRef = targetActivity?.let { WeakReference(it) }
    }

    /**
     * 释放所有资源
     */
    fun release() {
        dismissUETMenu()
        targetActivityRef = null
    }
    // endregion

    // region 私有方法
    /**
     * 内部方法：添加过滤类名
     */
    private fun putFilterClassName(className: String) {
        filterClassesSet.add(className)
    }

    /**
     * 内部方法：添加属性提供器（添加到列表首位）
     */
    private fun putAttrsProviderClassName(className: String) {
        attrsProviderSet.add(0, className)  // 添加到首位
    }

    /**
     * 内部方法：显示菜单核心逻辑
     */
    private fun showMenu(y: Int = 10): Boolean {
        val context = Application.getApplicationContext()

        // 检查悬浮窗权限（API 23+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            context?.let {
                requestPermission(it)  // 请求权限
                Toast.makeText(it, "授权后请重新启用UETool", Toast.LENGTH_LONG).show()
            }
            return false
        }

        // 线程安全的菜单显示逻辑
        return synchronized(this) {
            val menu = uetMenuRef?.get() ?: context?.let {
                UETMenu(it, y).also {
                    uetMenuRef = WeakReference(it)  // 创建新菜单
                }
            } ?: return false  // 上下文无效时返回false

            if (!menu.isShown) {
                menu.show()  // 显示菜单
                true
            } else {
                false  // 菜单已显示
            }
        }
    }

    /**
     * 请求悬浮窗权限（API 23+）
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermission(context: Context) {
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"))
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            .let { context.startActivity(it) }
    }
    // endregion
}