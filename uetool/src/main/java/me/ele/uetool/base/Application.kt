package me.ele.uetool.base

import android.content.Context

class Application private constructor() {
    companion object {
        @JvmStatic
        private var CONTEXT: Context? = null

        @JvmStatic
        fun getApplicationContext(): Context? {
            return CONTEXT ?: try {
                val activityThreadClass = Class.forName("android.app.ActivityThread")
                val method = activityThreadClass.getMethod("currentApplication")
                CONTEXT = method.invoke(null) as Context
                CONTEXT
            } catch (e: Exception) {
                null
            }
        }
    }
}