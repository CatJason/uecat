package me.ele.uetool.base

import android.os.Build
import me.weishu.reflection.Reflection

/**
 * 来自 weishu FreeReflection
 * https://github.com/tiann/FreeReflection
 */
object ReflectionP {
    private const val TAG = "Reflection"

    fun <T> breakAndroidP(func: Func<T>): T {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                Reflection.unseal(Application.getApplicationContext())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val result = func.call()
        return result
    }

    interface Func<T> {
        fun call(): T
    }
}
