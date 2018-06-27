package com.appunite.swipelib.widget

import android.view.View
import android.view.ViewGroup

inline fun ViewGroup.forEachIndexed(action: (view: View, index: Int) -> Unit) {
    for (index in 0 until childCount) {
        action(getChildAt(index), index)
    }
}

fun ViewGroup.safeMeasureChild(child: View,
        parentWidthMeasureSpec: Int, widthUsed: Int,
        parentHeightMeasureSpec: Int, heightUsed: Int) {
    if (child.visibility != View.GONE) {
        val childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec,
                paddingLeft + paddingRight + child.marginLeft + child.marginRight
                        + widthUsed, child.layoutParams.width)
        val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec,
                (paddingTop + paddingBottom + child.marginTop + child.marginTop
                        + heightUsed), child.layoutParams.height)

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }
}

val View.marginStart: Int
    get() = ((layoutParams as? ViewGroup.MarginLayoutParams)?.marginStart ?: 0)

val View.marginLeft: Int
    get() = ((layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0)

val View.marginRight: Int
    get() = ((layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin ?: 0)

val View.marginTop: Int
    get() = ((layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0)

val View.marginBottom: Int
    get() = ((layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0)

fun View.getMeasuredMarginsWidth(): Int = if (visibility == View.GONE) 0 else measuredWidth + marginLeft + marginRight

fun View.getMeasuredMarginsHeight(): Int = if (visibility == View.GONE) 0 else measuredHeight + marginTop + marginBottom

fun View.layoutView(left: Int, top: Int) {
    if (visibility != View.GONE) {
        val leftWithMargins = left + marginStart
        val topWithMargins = top + marginTop
        layout(
                leftWithMargins,
                topWithMargins,
                leftWithMargins + measuredWidth,
                topWithMargins + measuredHeight)
    }
}