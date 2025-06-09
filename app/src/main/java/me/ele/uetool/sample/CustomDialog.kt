package me.ele.uetool.sample

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager

class CustomDialog(
    context: Context
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_layout)
    }

    override fun show() {
        super.show()
        window?.apply {
            val layoutParams = attributes.apply {
                title = "???"
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            attributes = layoutParams
        }
    }
}