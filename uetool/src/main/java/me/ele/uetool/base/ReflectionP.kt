package me.ele.uetool.base

import android.os.Build
import android.util.Log

/**
 * 使用 FreeReflection 绕过 Android P 限制的工具类
 * @see <a href="https://github.com/tiann/FreeReflection">FreeReflection</a>
 */
object ReflectionP {
    private const val TAG = "Reflection"

    /**
     * 执行需要绕过 Android P 限制的操作
     * @param block 要执行的操作块
     * @return 操作的结果
     */
    @JvmStatic
    fun <T> breakAndroidP(block: () -> T): T {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            tryUnsealPlatform()
        }
        return block()
    }

    /**
     * 尝试解除平台限制
     */
    private fun tryUnsealPlatform() {
        val context = Application.getApplicationContext()
        if (context == null) {
            Log.w(TAG, "Application context is null, cannot unseal platform")
            return
        }

        runCatching {
            Reflection.unseal(context)
        }.onFailure { e ->
            Log.e(TAG, "Failed to unseal platform", e)
        }
    }
}