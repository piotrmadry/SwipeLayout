package com.appunite.swipelib.widget

import android.content.Context
import android.graphics.Rect
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.appunite.swipelib.R

class DragLayout constructor(
        context: Context,
        attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var mainView: View? = null
    private var rightView: View? = null

    private var rectOpen = Rect()
    private var rectClose = Rect()

    override fun onFinishInflate() {
        super.onFinishInflate()
        rightView = getChildAt(0)
        mainView = getChildAt(1)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setupRect()
    }

    private fun setupRect() {
        rectClose = Rect(notNull(mainView).left, notNull(mainView).top, notNull(mainView).right, notNull(mainView).bottom)
        rectOpen = Rect(rectClose.left - notNull(rightView).width, rectClose.top, rectClose.right - notNull(rightView).width, rectClose.bottom)
    }

    private val dragHelperCallbacks = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child.id == R.id.kot
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val leftBound = -notNull(rightView).width
            val rightBound = 0

            return Math.min(Math.max(left, leftBound), rightBound)
        }

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            dragHelper.captureChildView(notNull(mainView), pointerId)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            if (notNull(mainView).right < getHalfwayRightViewPosition()) {
                open(true, 0)
            } else {
                close(true, 0)
            }
        }
    }
    private val dragHelper: ViewDragHelper = ViewDragHelper.create(this, 1.0f, dragHelperCallbacks)
            .apply { setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL) }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return dragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun notNull(view: View?): View = view!!

    private fun getHalfwayRightViewPosition(): Int {
        return rectClose.right - (notNull(rightView).width / 2)
    }

    fun open(animate: Boolean, direction: Int) {
        if (animate) {
            dragHelper.smoothSlideViewTo(notNull(mainView), rectOpen.left, rectOpen.top)
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun close(animate: Boolean, direction: Int) {
        if (animate) {
            dragHelper.smoothSlideViewTo(notNull(mainView), rectClose.left, rectClose.top)
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }
}