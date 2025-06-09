package me.ele.uetool.sample

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import me.ele.uetool.UETool
import me.ele.uetool.sample.ui.recyclerviewsample.RecyclerViewActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<SwitchCompat>(R.id.control).apply {
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    if(!UETool.showUETMenu()) {
                        isChecked = false
                    }
                } else {
                    UETool.dismissUETMenu()
                }
            }
            isChecked = true
        }

        updateDraweeView()
        updateSpanTextView()
        updateCustomView()
        updateFontView()
    }

    @SuppressLint("NonConstantResourceId")
    fun onClick(view: View) {
        when (view.id) {
            R.id.btn1 -> startActivity(Intent(this, SecondActivity::class.java))
            R.id.btn2 -> CustomDialog(this).show()
            R.id.btn3 -> startActivity(Intent(this, FragmentSampleActivity::class.java))
            R.id.btn4 -> startActivity(Intent(this, RecyclerViewActivity::class.java))
        }
    }

    private fun updateDraweeView() {
        findViewById<SimpleDraweeView>(R.id.drawee_view).apply {
            controller = Fresco.newDraweeControllerBuilder()
                .setUri("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1561443230828&di=066c39a584cfe5cdcb244cc3af74afff&imgtype=0&src=http%3A%2F%2Fzkres1.myzaker.com%2F201905%2F5cda353b77ac6420a360a53f_320.jpg")
                .setAutoPlayAnimations(true)
                .build()
        }
    }

    private fun updateSpanTextView() {
        findViewById<TextView>(R.id.span).apply {
            val spannableString = SpannableString("  海底捞火锅")
            ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_food_new)?.let { drawable ->
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                val imageSpan = VerticalImageSpan(drawable)
                spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            text = spannableString
        }
    }

    private fun updateCustomView() {
        findViewById<CustomView>(R.id.custom).apply {
            moreAttribution = "more attribution"
            ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_up_vote)?.let { drawable ->
                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                setCompoundDrawables(null, drawable, null, null)
            }
        }
    }

    private fun updateFontView() {
        findViewById<TextView>(R.id.font_test).apply {
            typeface = Typeface.create("casual", Typeface.BOLD)
        }
    }
}