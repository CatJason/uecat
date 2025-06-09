package me.ele.uetool

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children

class TransparentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TYPE = "extra_type"

        const val TYPE_UNKNOWN = -1
        const val TYPE_EDIT_ATTR = 1
        const val TYPE_SHOW_GRIDDING = 2
        const val TYPE_RELATIVE_POSITION = 3

        private val SUPPORTED_TYPES = setOf(
            TYPE_EDIT_ATTR,
            TYPE_RELATIVE_POSITION,
            TYPE_SHOW_GRIDDING
        )
    }

    private lateinit var vContainer: ViewGroup
    private var type: Int = TYPE_UNKNOWN
    private val initialTouch = FloatArray(2)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            finish()
            return
        }

        setupWindow()
        setContentView(R.layout.uet_activity_transparent)
        initializeViews()
    }

    private fun setupWindow() {
        setStatusBarColor(window, Color.TRANSPARENT)
        enableFullscreen(window)
    }

    private fun initializeViews() {
        vContainer = findViewById(R.id.container)
        type = intent.getIntExtra(EXTRA_TYPE, TYPE_UNKNOWN)

        if (type !in SUPPORTED_TYPES) {
            showComingSoonToast()
            return
        }

        setupFeatureViews()
    }

    private fun showComingSoonToast() {
        Toast.makeText(this, R.string.uet_coming_soon, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupFeatureViews() {
        addFeatureView()
    }

    private fun addFeatureView() {
        when (type) {
            TYPE_EDIT_ATTR -> setupEditAttrLayout()
            TYPE_RELATIVE_POSITION -> setupRelativePositionLayout()
            TYPE_SHOW_GRIDDING -> setupGriddingLayout()
        }
    }

    private fun setupEditAttrLayout() {
        EditAttrLayout(this).apply {
            vContainer.addView(this)
        }
    }

    private fun setupRelativePositionLayout() {
        vContainer.addView(RelativePositionLayout(this))
    }

    private fun setupGriddingLayout() {
        vContainer.addView(GriddingLayout(this))
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    fun dismissAttrsDialog() {
        vContainer.children.filterIsInstance<EditAttrLayout>().forEach {
            it.dismissAttrsDialog()
        }
    }
}