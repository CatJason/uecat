package me.ele.uetool

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.ele.uetool.base.Element
import me.ele.uetool.base.dip2px
import me.ele.uetool.base.getScreenHeight
import me.ele.uetool.base.getScreenWidth
import me.ele.uetool.base.item.BriefDescItem
import me.ele.uetool.base.item.Item
import me.ele.uetool.base.item.SwitchItem
import java.util.ArrayList

class AttrsDialog(context: Context) : Dialog(context, R.style.uet_Theme_Holo_Dialog_background_Translucent) {
    private lateinit var vList: RecyclerView
    private val adapter = UEAdapter()
    private val layoutManager = LinearLayoutManager(context)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.uet_dialog_attrs)
        vList = findViewById(R.id.list)
        vList.adapter = adapter
        vList.layoutManager = layoutManager
    }

    fun show(element: Element) {
        show()
        val dialogWindow = window ?: return
        val lp = dialogWindow.attributes
        dialogWindow.setGravity(Gravity.LEFT or Gravity.TOP)
        lp.x = element.rect.left
        lp.y = element.rect.bottom
        lp.width = getScreenWidth() - dip2px(30f)
        lp.height = getScreenHeight() / 2
        dialogWindow.attributes = lp
        adapter.notifyDataSetChanged(element)
        layoutManager.scrollToPosition(0)

        for (i in 0 until adapter.itemCount) {
            val item = adapter.getItem<Item>(i)
            if (item is SwitchItem && item.type == SwitchItem.Type.TYPE_SHOW_VALID_VIEWS) {
                item.isChecked = true
                adapter.callback?.showValidViews(i, true)
                break
            }
        }

        val myCat = findViewById<View>(R.id.cat)
        myCat.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f
            private var isDragged: Boolean = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = dialogWindow.attributes.x
                        initialY = dialogWindow.attributes.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragged = false
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragged) {
                            v.performClick()
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = (event.rawX - initialTouchX).toInt()
                        val deltaY = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                            isDragged = true
                            val params = dialogWindow.attributes
                            params.x = initialX + deltaX
                            params.y = initialY + deltaY
                            dialogWindow.attributes = params
                        }
                        return true
                    }
                }
                return false
            }
        })

        val sign = findViewById<View>(R.id.sign)
        myCat.setOnClickListener {
            if (vList.visibility == View.GONE) {
                vList.visibility = View.VISIBLE
                sign.alpha = 1f
                it.alpha = 1f
            } else {
                vList.visibility = View.GONE
                sign.alpha = 0.2f
                it.alpha = 0.2f
            }
        }
    }

    fun notifyValidViewItemInserted(positionStart: Int, validElements: List<Element>, targetElement: Element) {
        val validItems = ArrayList<Item>()
        var flag = false
        for (i in validElements.size - 1 downTo 0) {
            val element = validElements[i]
            if (element.view.javaClass.name == "com.android.internal.policy.DecorView") {
                flag = true
            }
            if (flag) {
                validItems.add(BriefDescItem(element, targetElement == element))
            }
        }
        adapter.notifyValidViewItemInserted(positionStart, validItems)
    }

    fun notifyItemRangeRemoved(positionStart: Int) {
        adapter.notifyValidViewItemRemoved(positionStart)
    }

    fun setAttrDialogCallback(callback: AttrDialogCallback) {
        adapter.setAttrDialogCallback(callback)
    }
}