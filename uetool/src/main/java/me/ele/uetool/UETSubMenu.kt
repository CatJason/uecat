package me.ele.uetool

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import me.ele.uetool.base.dip2px

class UETSubMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val padding = dip2px(5f)
    private var vImage: ImageView
    private var vTitle: TextView

    init {
        inflate(context, R.layout.uet_sub_menu_layout, this)
        gravity = Gravity.CENTER
        orientation = VERTICAL
        setPadding(padding, 0, padding, 0)
        translationY = dip2px(2f).toFloat()
        vImage = findViewById(R.id.image)
        vTitle = findViewById(R.id.title)
    }

    fun update(subMenu: SubMenu) {
        vImage.setImageResource(subMenu.imageRes)
        val params = LayoutParams(
            (20 * resources.displayMetrics.density).toInt(), // 宽度 20dp → px
            (20 * resources.displayMetrics.density).toInt()   // 高度 20dp → px
        )
        vImage.layoutParams = params
        vTitle.text = subMenu.title
        setOnClickListener(subMenu.onClickListener)
    }

    class SubMenu(
        val title: String,
        val imageRes: Int,
        val onClickListener: OnClickListener
    )
}