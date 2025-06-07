package me.ele.uetool.sample;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import com.facebook.drawee.backends.pipeline.Fresco;

import me.ele.uetool.UETool;

public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);

        UETool.INSTANCE.putFilterClass(FilterOutView.class);
        UETool.INSTANCE.putAttrsProviderClass(CustomAttribution.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

                private int visibleActivityCount;
                private int uetoolDismissY = -1;

                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                }

                @Override
                public void onActivityStarted(Activity activity) {
                    visibleActivityCount++;
                    if (visibleActivityCount == 1 && uetoolDismissY >= 0) {
                        UETool.INSTANCE.showUETMenu(uetoolDismissY);
                    }
                }

                @Override
                public void onActivityResumed(Activity activity) {

                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {
                    visibleActivityCount--;
                    if (visibleActivityCount == 0) {
                        uetoolDismissY = UETool.INSTANCE.dismissUETMenu();
                    }
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {

                }
            });
        }
    }
}
