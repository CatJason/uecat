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

object UETool {
    // Thread-safe collections
    private val filterClassesSet = CopyOnWriteArraySet<String>()
    private val attrsProviderSet = CopyOnWriteArrayList<String>().apply {
        add(UETCore::class.java.name)
        add("me.ele.uetool.fresco.UETFresco")
    }

    // Weak references to avoid memory leaks
    @Volatile
    private var targetActivityRef: WeakReference<Activity>? = null

    @Volatile
    private var uetMenuRef: WeakReference<UETMenu>? = null

    // Lazy initialization for thread safety
    val attrsDialogMultiTypePool by lazy {
        AttrsDialogMultiTypePool().apply {
            // Initialize the pool directly here instead of calling a separate function
            register(AddMinusEditItem::class.java, AddMinusEditTextItemBinder())
            register(BitmapItem::class.java, BitmapItemBinder())
            register(BriefDescItem::class.java, BriefDescItemBinder())
            register(EditTextItem::class.java, EditTextItemBinder())
            register(SwitchItem::class.java, SwitchItemBinder())
            register(TextItem::class.java, TextItemBinder())
            register(TitleItem::class.java, TitleItemBinder())
        }
    }

    // region Public API
    fun putFilterClass(clazz: Class<*>) = putFilterClassName(clazz.name)
    fun putFilterClass(className: String) = putFilterClassName(className)

    fun <T : Item> registerAttrDialogItemViewBinder(clazz: Class<T>, binder: ItemViewBinder<T, *>) {
        attrsDialogMultiTypePool.register(clazz, binder)
    }

    fun putAttrsProviderClass(clazz: Class<*>) = putAttrsProviderClass(clazz.name)
    fun putAttrsProviderClass(className: String) = putAttrsProviderClassName(className)

    fun showUETMenu(): Boolean = showMenu()
    fun showUETMenu(y: Int): Boolean = showMenu(y)

    fun dismissUETMenu(): Int {
        uetMenuRef?.get()?.let { menu ->
            val y = menu.dismiss()
            uetMenuRef = null
            return y
        }
        return -1
    }

    fun getFilterClasses(): Set<String> = filterClassesSet.toSet()
    fun getTargetActivity(): Activity? = targetActivityRef?.get()
    fun getAttrsProvider(): List<String> = attrsProviderSet.toList()

    fun setTargetActivity(targetActivity: Activity?) {
        targetActivityRef = targetActivity?.let { WeakReference(it) }
    }

    fun release() {
        dismissUETMenu()
        targetActivityRef = null
    }
    // endregion

    // region Private Helpers
    private fun putFilterClassName(className: String) {
        filterClassesSet.add(className)
    }

    private fun putAttrsProviderClassName(className: String) {
        attrsProviderSet.add(0, className)
    }

    private fun showMenu(y: Int = 10): Boolean {
        val context = Application.getApplicationContext()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            requestPermission(context)
            Toast.makeText(context, "After grant this permission, re-enable UETool", Toast.LENGTH_LONG).show()
            return false
        }

        return synchronized(this) {
            val menu = uetMenuRef?.get() ?: UETMenu(context, y).also {
                uetMenuRef = WeakReference(it)
            }

            if (!menu.isShown) {
                menu.show()
                true
            } else {
                false
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermission(context: Context) {
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            .let { context.startActivity(it) }
    }
    // endregion
}