package me.ele.uetool.cat

import android.view.View

fun getViewLayer(view: View?): Int {
    // 初始化层级
    var layer = 0

    // 我们将使用一个临时变量来遍历
    var currentView = view

    // 遍历视图树，每找到一个父视图，层级加一
    while (currentView?.parent != null && currentView.parent is View) {
        layer++
        // 更新当前视图到父视图，以便继续向上遍历
        currentView = currentView.parent as View
    }

    // 返回计算出的层级
    return layer
}