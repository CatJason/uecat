package me.ele.uetool

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import me.ele.uetool.base.DimenUtil.px2dip
import kotlin.math.roundToInt

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
        Util.setStatusBarColor(window, Color.TRANSPARENT)
        Util.enableFullscreen(window)
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
        val boardView = createBoardView().apply {
            addFeatureView(this)
            addToContainer()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createBoardView() = BoardTextView(this).apply {
        setOnClickListener {
            UETool.getTargetActivity()?.finish()
            finish()
        }
        gravity = Gravity.CENTER
        setOnTouchListener(createBoardTouchListener())
    }

    private fun createBoardTouchListener() = View.OnTouchListener { v, event ->
        v ?: return@OnTouchListener false
        event ?: return@OnTouchListener false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouch[0] = event.rawX
                initialTouch[1] = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                v.apply {
                    x += event.rawX - initialTouch[0]
                    y += event.rawY - initialTouch[1]
                }
                initialTouch[0] = event.rawX
                initialTouch[1] = event.rawY
            }
        }
        true
    }

    private fun addFeatureView(board: BoardTextView) {
        when (type) {
            TYPE_EDIT_ATTR -> setupEditAttrLayout(board)
            TYPE_RELATIVE_POSITION -> setupRelativePositionLayout()
            TYPE_SHOW_GRIDDING -> setupGriddingLayout(board)
        }
    }

    private fun setupEditAttrLayout(board: BoardTextView) {
        EditAttrLayout(this).apply {
            setOnDragListener(createDragListener(board))
            vContainer.addView(this)
        }
    }

    private fun createDragListener(board: BoardTextView) =
        object : EditAttrLayout.OnDragListener {
            override fun showOffset(offsetContent: String) {
                board.updateInfo(offsetContent)
            }
        }

    private fun setupRelativePositionLayout() {
        vContainer.addView(RelativePositionLayout(this))
    }

    private fun setupGriddingLayout(board: BoardTextView) {
        vContainer.addView(GriddingLayout(this))
        board.updateInfo("LINE_INTERVAL: ${px2dip(GriddingLayout.LINE_INTERVAL.toFloat(), true)}")
    }

    private fun BoardTextView.addToContainer() {
        with(resources.displayMetrics.density) {
            FrameLayout.LayoutParams(100.dpToPx(), 100.dpToPx()).apply {
                gravity = Gravity.BOTTOM
                setMargins(20.dpToPx(), 20.dpToPx(), 20.dpToPx(), 20.dpToPx())
                vContainer.addView(this@addToContainer, this)
            }
        }
    }

    private fun Int.dpToPx() = (this * resources.displayMetrics.density).roundToInt()

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        UETool.release()
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