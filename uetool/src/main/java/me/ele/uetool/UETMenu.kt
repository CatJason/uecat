package me.ele.uetool

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import me.ele.uetool.TransparentActivity.Companion.TYPE_UNKNOWN

class UETMenu @JvmOverloads constructor(
    context: Context,
    private var inputY: Int = 0,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    private val vMenu: View
    private val vSubMenuContainer: ViewGroup
    private var animator: ValueAnimator? = null
    private val defaultInterpolator: Interpolator = AccelerateDecelerateInterpolator()
    private val subMenus: MutableList<UETSubMenu.SubMenu> = ArrayList()
    private val windowManager: WindowManager
    private val params = WindowManager.LayoutParams()
    private val touchSlop: Int

    /**
     * 容器刚出来的时候的宽度，用于播放动画
     */
    private var vSubMenuContainerWidth = 0

    init {
        inflate(context, R.layout.uet_menu_layout, this)
        gravity = Gravity.CENTER_VERTICAL
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        vMenu = findViewById(R.id.menu)
        vSubMenuContainer = findViewById(R.id.sub_menu_container)
        val resources = context.resources
        subMenus.add(
            UETSubMenu.SubMenu(
                resources.getString(R.string.uet_catch_view),
                R.drawable.uet_edit_attr
            ) { open(TransparentActivity.TYPE_EDIT_ATTR) })
        subMenus.add(UETSubMenu.SubMenu(
            resources.getString(R.string.uet_relative_location),
            R.drawable.uet_relative_position
        ) { open(TransparentActivity.TYPE_RELATIVE_POSITION) })
        subMenus.add(UETSubMenu.SubMenu(
            resources.getString(R.string.uet_grid), R.drawable.uet_show_gridding
        ) { open(TransparentActivity.TYPE_SHOW_GRIDDING) })
        subMenus.add(getSubMenu(resources, getContext()))
        for (subMenu in subMenus) {
            val uetSubMenu = UETSubMenu(getContext())
            uetSubMenu.update(subMenu)
            vSubMenuContainer.addView(uetSubMenu)
        }
        vMenu.setOnClickListener { startAnim() }
        vMenu.setOnTouchListener(object : OnTouchListener {
            private var downX = 0f
            private var downY = 0f
            private var lastY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX
                        downY = event.rawY
                        lastY = downY
                    }

                    MotionEvent.ACTION_MOVE -> {
                        params.y += (event.rawY - lastY).toInt()
                        params.y = Math.max(0, params.y)
                        windowManager.updateViewLayout(this@UETMenu, params)
                        lastY = event.rawY
                    }

                    MotionEvent.ACTION_UP -> if (Math.abs(event.rawX - downX) < touchSlop && Math.abs(
                            event.rawY - downY
                        ) < touchSlop
                    ) {
                        try {
                            var field = View::class.java.getDeclaredField("mListenerInfo")
                            field.isAccessible = true
                            var vMenu = field[vMenu]
                            field = vMenu.javaClass.getDeclaredField("mOnClickListener")
                            field.isAccessible = true
                            vMenu = field[vMenu]
                            (vMenu as? OnClickListener)?.onClick(this@UETMenu.vMenu)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                return true
            }
        })

        // 获取容器宽度，同时初始化设置
        vSubMenuContainer.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                vSubMenuContainerWidth = vSubMenuContainer.measuredWidth
                vSubMenuContainer.translationX = -vSubMenuContainerWidth.toFloat() // 隐藏
                vSubMenuContainer.visibility = GONE // 设置为不可见，移除父容器占位
                vSubMenuContainer.viewTreeObserver.removeOnPreDrawListener(this)
                // 这次不需要绘制，避免闪烁
                return false
            }
        })
    }

    private fun startAnim() {
        ensureAnim()
        val isOpen = vSubMenuContainer.translationX <= -vSubMenuContainerWidth
        animator?.interpolator = if (isOpen) {
                defaultInterpolator
            } else {
                ReverseInterpolator(defaultInterpolator)
            }
        animator?.removeAllListeners()
        animator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                vSubMenuContainer.visibility = VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!isOpen) {
                    vSubMenuContainer.visibility = GONE
                    val currentTopActivity = getCurrentActivity() ?: return

                    if (currentTopActivity.javaClass == TransparentActivity::class.java) {
                        currentTopActivity.finish()
                    }
                }
            }
        })
        animator?.start()
    }

    private fun ensureAnim() {
        if (animator == null) {
            animator = ValueAnimator.ofInt(-vSubMenuContainerWidth, 0).apply {
                addUpdateListener { animation ->
                    vSubMenuContainer.translationX = (animation.animatedValue as Int).toFloat()
                }
                duration = 400
            }
        }
    }

    private fun getSubMenu(resources: Resources, context: Context): UETSubMenu.SubMenu {
        // 默认配置
        val config = ScalpelConfig()

        fun to3D() {
            try {
                Log.d("ScalpelMenu", "开始处理3D视图切换")

                val activity = getCurrentActivity() ?: run {
                    Log.w("ScalpelMenu", "无有效Activity")
                    return
                }

                val decorView = activity.window?.decorView as? ViewGroup ?: run {
                    Log.w("ScalpelMenu", "无法获取DecorView")
                    return
                }

                val content = decorView.findViewById<ViewGroup>(android.R.id.content) ?: run {
                    Log.w("ScalpelMenu", "未找到内容区域")
                    return
                }

                // 检查是否已存在Scalpel容器
                content.findViewWithTag<ScalpelFrameLayout>(config.tagIdentifier)?.let { scalpel ->
                    Log.d("ScalpelMenu", "检测到现有3D视图，执行恢复操作")
                    restoreOriginalView(scalpel, content)
                    return
                }

                // 创建新的3D视图
                setupScalpelView(content, context, config)

            } catch (e: Exception) {
                Log.e("ScalpelMenu", "3D视图切换失败", e)
            }
        }

        return UETSubMenu.SubMenu(
            resources.getString(R.string.uet_scalpel),
            R.drawable.uet_2d_to_3d
        ) {
            val currentTopActivity = getCurrentActivity() ?: return@SubMenu

            if (currentTopActivity.javaClass == TransparentActivity::class.java) {
                // 注册生命周期监听
                val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityDestroyed(activity: Activity) {
                        if (activity == currentTopActivity) {
                            // 注销监听
                            (context.applicationContext as? Application)?.unregisterActivityLifecycleCallbacks(this)

                            // 确保在主线程执行
                            post {
                                getCurrentActivity()?.let { newActivity ->
                                    if(newActivity !is TransparentActivity) {
                                        to3D()
                                    } else {
                                        postDelayed({
                                            to3D()
                                        } ,300)
                                    }
                                }
                            }
                        }
                    }
                    // 其他生命周期方法留空
                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                    override fun onActivityStarted(activity: Activity) {}
                    override fun onActivityResumed(activity: Activity) {}
                    override fun onActivityPaused(activity: Activity) {}
                    override fun onActivityStopped(activity: Activity) {}
                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                }

                // 注册监听
                (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(lifecycleCallbacks)

                // 关闭当前 Activity
                currentTopActivity.finish()
            } else {
                to3D()
            }
        }
    }

    private fun open(type: Int = TYPE_UNKNOWN) {
        val currentTopActivity = getCurrentActivity() ?: return

        if (currentTopActivity.javaClass == TransparentActivity::class.java) {
            // 注册生命周期监听
            val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
                override fun onActivityDestroyed(activity: Activity) {
                    if (activity == currentTopActivity) {
                        // 注销监听
                        (context.applicationContext as? Application)?.unregisterActivityLifecycleCallbacks(this)

                        // 确保在主线程执行
                        post {
                            getCurrentActivity()?.let { newActivity ->
                                if(newActivity !is TransparentActivity) {
                                    launchNewActivity(newActivity, type)
                                } else {
                                    postDelayed({
                                        launchNewActivity(newActivity, type)
                                    } ,300)
                                }
                            }
                        }
                    }
                }
                // 其他生命周期方法留空
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            }

            // 注册监听
            (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(lifecycleCallbacks)

            // 关闭当前 Activity
            currentTopActivity.finish()
        } else {
            // 直接打开新 Activity
            launchNewActivity(currentTopActivity, type)
        }
    }

    private fun launchNewActivity(activity: Activity, type: Int) {
        if(type == TYPE_UNKNOWN) {
            return
        }
        val intent = Intent(activity, TransparentActivity::class.java)
        intent.putExtra(TransparentActivity.EXTRA_TYPE, type)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
        UETool.setTargetActivity(activity)
    }

    fun show() {
        try {
            windowManager.addView(this, windowLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismiss(): Int {
        try {
            windowManager.removeView(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return params.y
    }

    private val windowLayoutParams: WindowManager.LayoutParams
        get() {
            return params.apply {
                width = FrameLayout.LayoutParams.WRAP_CONTENT
                height = FrameLayout.LayoutParams.WRAP_CONTENT
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                } else {
                    type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                }
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                format = PixelFormat.TRANSLUCENT
                gravity = Gravity.TOP or Gravity.LEFT
                x = 10
                y = inputY
            }
        }

    private class ReverseInterpolator(private val mWrappedInterpolator: TimeInterpolator) :
        TimeInterpolator {
        override fun getInterpolation(input: Float): Float {
            return mWrappedInterpolator.getInterpolation(Math.abs(input - 1f))
        }
    }
}