package me.ele.uetool

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import me.ele.uetool.base.DimenUtil
import me.ele.uetool.base.Element
import me.ele.uetool.base.IAttrs
import me.ele.uetool.base.item.AddMinusEditItem
import me.ele.uetool.base.item.BitmapItem
import me.ele.uetool.base.item.EditTextItem
import me.ele.uetool.base.item.Item
import me.ele.uetool.base.item.SwitchItem
import me.ele.uetool.base.item.TextItem
import me.ele.uetool.base.item.TitleItem
import java.util.Locale

class UETCore : IAttrs {
    override fun getAttrs(element: Element): List<Item> {
        val view = element.view
        return mutableListOf<Item>().apply {
            add(TextItem("Fragment", Util.getCurrentFragmentName(view)) { v ->
                val activity = Util.getCurrentActivity()
                if (activity is TransparentActivity) {
                    activity.dismissAttrsDialog()
                }
                FragmentListTreeDialog(v.context).show()
            })
            add(TextItem("ViewHolder", Util.getViewHolderName(view)))
            add(SwitchItem("Move", element, SwitchItem.Type.TYPE_MOVE))
            add(SwitchItem("ValidViews", element, SwitchItem.Type.TYPE_SHOW_VALID_VIEWS))
            AttrsManager.createAttrs(view)?.let { addAll(it.getAttrs(element)) }
            add(TitleItem("COMMON"))
            add(TextItem("Class", view.javaClass.name))
            add(TextItem("Id", Util.getResId(view)))
            add(TextItem("ResName", Util.getResourceName(view.id)))
            add(TextItem("Tag", Util.getViewTag(view)))
            add(TextItem("Clickable", view.isClickable.toString().uppercase(Locale.getDefault())))
            add(TextItem("OnClickListener", Util.getViewClickListener(view)))
            add(TextItem("Focused", view.isFocused.toString().uppercase(Locale.getDefault())))
            add(AddMinusEditItem("Width（dp）", element, EditTextItem.Type.TYPE_WIDTH, DimenUtil.px2dip(view.width.toFloat())))
            add(AddMinusEditItem("Height（dp）", element, EditTextItem.Type.TYPE_HEIGHT, DimenUtil.px2dip(view.height.toFloat())))
            add(TextItem("Alpha", view.alpha.toString()))
            when (val background = Util.getBackground(view)) {
                is String -> add(TextItem("Background", background))
                is Bitmap -> add(BitmapItem("Background", background))
            }
            add(AddMinusEditItem("PaddingLeft（dp）", element, EditTextItem.Type.TYPE_PADDING_LEFT, DimenUtil.px2dip(view.paddingLeft.toFloat())))
            add(AddMinusEditItem("PaddingRight（dp）", element, EditTextItem.Type.TYPE_PADDING_RIGHT, DimenUtil.px2dip(view.paddingRight.toFloat())))
            add(AddMinusEditItem("PaddingTop（dp）", element, EditTextItem.Type.TYPE_PADDING_TOP, DimenUtil.px2dip(view.paddingTop.toFloat())))
            add(AddMinusEditItem("PaddingBottom（dp）", element, EditTextItem.Type.TYPE_PADDING_BOTTOM, DimenUtil.px2dip(view.paddingBottom.toFloat())))
        }
    }

    internal object AttrsManager {
        fun createAttrs(view: View?): IAttrs? {
            if (view is TextView) {
                return UETTextView()
            } else if (view is ImageView) {
                return UETImageView()
            }
            return null
        }
    }

    internal class UETTextView : IAttrs {
        override fun getAttrs(element: Element): List<Item> {
            val items: MutableList<Item> = ArrayList()
            val textView = element.view as TextView
            items.add(TitleItem("TextView"))
            items.add(
                EditTextItem(
                    "Text",
                    element,
                    EditTextItem.Type.TYPE_TEXT,
                    textView.text.toString()
                )
            )
            items.add(
                AddMinusEditItem(
                    "TextSize（sp）",
                    element,
                    EditTextItem.Type.TYPE_TEXT_SIZE,
                    DimenUtil.px2sp(textView.textSize)
                )
            )
            items.add(
                EditTextItem(
                    "TextColor",
                    element,
                    EditTextItem.Type.TYPE_TEXT_COLOR,
                    Util.intToHexColor(textView.currentTextColor)
                )
            )
            val pairs = Util.getTextViewBitmap(textView)
            for (pair in pairs) {
                items.add(BitmapItem(pair.first, pair.second))
            }
            items.add(
                SwitchItem(
                    "IsBold",
                    element,
                    SwitchItem.Type.TYPE_IS_BOLD,
                    if (textView.typeface != null) textView.typeface.isBold else false
                )
            )
            return items
        }
    }

    internal class UETImageView : IAttrs {
        override fun getAttrs(element: Element): List<Item> {
            val items: MutableList<Item> = ArrayList()
            val imageView = element.view as ImageView
            items.add(TitleItem("ImageView"))
            items.add(BitmapItem("Bitmap", Util.getImageViewBitmap(imageView)))
            items.add(TextItem("ScaleType", Util.getImageViewScaleType(imageView)))
            return items
        }
    }
}