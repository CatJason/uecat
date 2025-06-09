package me.ele.uetool.sample

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import com.facebook.drawee.backends.pipeline.Fresco
import me.ele.uetool.UETool

class AppContext : Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)

        // Changed from UETool.INSTANCE.method() to UETool.method()
        UETool.putFilterClass(FilterOutView::class.java)
        UETool.putAttrsProviderClass(CustomAttribution::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                private var visibleActivityCount = 0
                private var uetoolDismissY = -1

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    // No implementation needed
                }

                override fun onActivityStarted(activity: Activity) {
                    visibleActivityCount++
                    if (visibleActivityCount == 1 && uetoolDismissY >= 0) {
                        UETool.showUETMenu(uetoolDismissY)
                    }
                }

                override fun onActivityResumed(activity: Activity) {
                    // No implementation needed
                }

                override fun onActivityPaused(activity: Activity) {
                    // No implementation needed
                }

                override fun onActivityStopped(activity: Activity) {
                    visibleActivityCount--
                    if (visibleActivityCount == 0) {
                        uetoolDismissY = UETool.dismissUETMenu()
                    }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    // No implementation needed
                }

                override fun onActivityDestroyed(activity: Activity) {
                    // No implementation needed
                }
            })
        }
    }
}