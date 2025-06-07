package me.ele.uetool;

import static me.ele.uetool.UtilsKt.getDrawableBitmap;
import android.app.Activity;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class Util {

    public static Object getBackground(View view) {
        Drawable drawable = view.getBackground();
        if (drawable instanceof ColorDrawable) {
            return UtilsKt.intToHexColor(((ColorDrawable) drawable).getColor());
        } else if (drawable instanceof GradientDrawable) {
            try {
                Field mFillPaintField = GradientDrawable.class.getDeclaredField("mFillPaint");
                mFillPaintField.setAccessible(true);
                Paint mFillPaint = (Paint) mFillPaintField.get(drawable);
                Shader shader = mFillPaint.getShader();
                if (shader instanceof LinearGradient) {
                    Field mColorsField = LinearGradient.class.getDeclaredField("mColors");
                    mColorsField.setAccessible(true);
                    int[] mColors = (int[]) mColorsField.get(shader);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0, N = mColors.length; i < N; i++) {
                        sb.append(UtilsKt.intToHexColor(mColors[i]));
                        if (i < N - 1) {
                            sb.append(" -> ");
                        }
                    }
                    return sb.toString();
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            return getDrawableBitmap(drawable);
        }
        return null;
    }

    public static Activity getCurrentActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            Field mActivitiesField = activityThreadClass.getDeclaredField("mActivities");
            mActivitiesField.setAccessible(true);
            Map activities = (Map) mActivitiesField.get(currentActivityThread);
            for (Object record : activities.values()) {
                Class recordClass = record.getClass();
                Field pausedField = recordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!(boolean) pausedField.get(record)) {
                    Field activityField = recordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}