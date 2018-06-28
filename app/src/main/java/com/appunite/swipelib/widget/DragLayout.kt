package com.appunite.swipelib.widget

import android.content.Context
import android.graphics.Rect
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.appunite.swipelib.R


class DragLayout constructor(
        context: Context,
        attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    companion object {
        private const val MIN_FLING_VELOCITY = 300
    }

    private var dragEdge: DragEdge = DragEdge.Left

    private var mainView: View? = null
    private var secondaryView: View? = null
    private var quadraryView: View? = null

    private var rectOpenLeftEdge = Rect()
    private var rectOpenRightEdge = Rect()
    private var rectClose = Rect()

    init {
        val typedArrayAttrs = context.theme.obtainStyledAttributes(attrs, R.styleable.DragLayout, 0, 0)
        dragEdge = when (typedArrayAttrs.getInteger(R.styleable.DragLayout_dragEdge, 0)) {
            0 -> DragEdge.Right
            1 -> DragEdge.Left
            2 -> DragEdge.Side
            else -> throw RuntimeException("Unknown DragEdge")
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        when (dragEdge) {
            DragEdge.Left, DragEdge.Right -> {
                if (childCount < 2) {
                    throw RuntimeException("DragEdge: $dragEdge needs at least 2 child views")
                }
                secondaryView = getChildAt(0)
                mainView = getChildAt(1)
            }
            else -> {
                if (childCount < 3) {
                    throw RuntimeException("DragEdge: $dragEdge needs at least 3 child views")
                }
                secondaryView = getChildAt(0)
                mainView = getChildAt(1)
                quadraryView = getChildAt(2)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        var desiredWidth = 0
        var desiredHeight = 0

        this@DragLayout.forEachIndexed { _, index ->
            val child = getChildAt(index)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            desiredWidth = Math.max(child.measuredWidth, desiredWidth)
            desiredHeight = Math.max(child.measuredHeight, desiredHeight)
        }

        val widthSpec = View.MeasureSpec.makeMeasureSpec(desiredWidth, widthMode)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(desiredHeight, heightMode)

        val measuredWidth = View.MeasureSpec.getSize(widthSpec)
        val measuredHeight = View.MeasureSpec.getSize(heightSpec)

        this@DragLayout.forEachIndexed { _, index ->

            val child = getChildAt(index)
            val childParams = child.layoutParams

            if (childParams != null) {
                if (childParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.minimumHeight = getMeasuredMarginsHeight()
                }

                if (childParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.minimumWidth = getMeasuredMarginsWidth()
                }
            }
            measureChild(child, widthSpec, heightSpec)
            desiredWidth = Math.max(child.getMeasuredMarginsWidth(), desiredWidth)
            desiredHeight = Math.max(child.getMeasuredMarginsHeight(), desiredHeight)
        }

        desiredWidth += paddingLeft + paddingRight
        desiredHeight += paddingTop + paddingBottom

        if (widthMode == View.MeasureSpec.EXACTLY) {
            desiredWidth = measuredWidth
        } else {
            if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                desiredWidth = measuredWidth
            }

            if (widthMode == View.MeasureSpec.AT_MOST) {
                desiredWidth = if (desiredWidth > measuredWidth) measuredWidth else desiredWidth
            }
        }
        if (heightMode == View.MeasureSpec.EXACTLY) {
            desiredHeight = measuredHeight
        } else {
            if (layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                desiredHeight = measuredHeight
            }
            if (heightMode == View.MeasureSpec.AT_MOST) {
                desiredHeight = if (desiredHeight > measuredHeight) measuredHeight else desiredHeight
            }
        }
        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

        when (dragEdge) {
            DragEdge.Left -> {
                this@DragLayout.forEachIndexed { child, _ -> child.layoutView(paddingLeft, paddingTop) }
            }
            DragEdge.Right -> {
                this@DragLayout.forEachIndexed { child, _ -> child.layoutView(right - child.getMeasuredMarginsWidth() - paddingRight - left, paddingTop) }
            }
            DragEdge.Side -> {
                getChildAt(0).layoutView(paddingLeft, paddingTop)
                getChildAt(1).layoutView(paddingLeft, paddingTop)
                getChildAt(2).layoutView(right - getChildAt(2).getMeasuredMarginsWidth() - paddingRight - left, paddingTop)
            }
        }
        storePositionsInRects()
    }

    private fun storePositionsInRects() {
        rectClose = Rect(notNull(mainView).left, notNull(mainView).top, notNull(mainView).right, notNull(mainView).bottom)
        rectOpenLeftEdge = Rect(rectClose.left + notNull(secondaryView).width, rectClose.top, rectClose.right + notNull(secondaryView).width, rectClose.bottom)
        rectOpenRightEdge = when (dragEdge) {
            DragEdge.Side -> {
                Rect(rectClose.left - notNull(quadraryView).width, rectClose.top, rectClose.right - notNull(quadraryView).width, rectClose.bottom)
            }
            else -> {
                Rect(rectClose.left - notNull(secondaryView).width, rectClose.top, rectClose.right - notNull(secondaryView).width, rectClose.bottom)
            }
        }
    }

    private val dragHelperCallbacks = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child.id == R.id.kot
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val leftBound = when (dragEdge) {
                DragEdge.Left -> 0
                DragEdge.Right -> -notNull(secondaryView).width
                DragEdge.Side -> -notNull(secondaryView).width
            }
            val rightBound = when (dragEdge) {
                DragEdge.Left -> notNull(secondaryView).width
                DragEdge.Right -> 0
                DragEdge.Side -> notNull(quadraryView).width
            }
            return Math.min(Math.max(left, leftBound), rightBound)
        }

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            dragHelper.captureChildView(notNull(mainView), pointerId)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)

            val velocityRightExceeded = pxToDp(xvel.toInt()) >= MIN_FLING_VELOCITY
            val velocityLeftExceeded = pxToDp(xvel.toInt()) <= -MIN_FLING_VELOCITY

            when (dragEdge) {
                DragEdge.Right -> {
                    when {
                        velocityRightExceeded -> close(true)
                        velocityLeftExceeded -> open(true)
                        else -> when {
                            notNull(mainView).right < getHalfwayVerticalViewPosition() -> open(true)
                            else -> close(true)
                        }
                    }
                }
                DragEdge.Left -> {
                    when {
                        velocityRightExceeded -> open(true)
                        velocityLeftExceeded -> close(true)
                        else -> when {
                            notNull(mainView).left < getHalfwayVerticalViewPosition() -> close(true)
                            else -> open(true)
                        }
                    }
                }
                DragEdge.Side -> {
                }
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

    private fun getHalfwayVerticalViewPosition(): Int {
        return when (dragEdge) {
            DragEdge.Left -> rectClose.left + (notNull(secondaryView).width / 2)
            DragEdge.Right -> rectClose.right - (notNull(secondaryView).width / 2)
            else -> 0
        }
    }

    fun open(animate: Boolean) {
        if (animate) {
            val rect = when (dragEdge) {
                DragEdge.Left -> rectOpenLeftEdge
                DragEdge.Right -> rectOpenRightEdge
                DragEdge.Side -> rectClose
            }
            dragHelper.smoothSlideViewTo(notNull(mainView), rect.left, rect.top)
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun close(animate: Boolean) {
        if (animate) {
            dragHelper.smoothSlideViewTo(notNull(mainView), rectClose.left, rectClose.top)
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun pxToDp(px: Int): Int {
        return (px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    private fun notNull(view: View?): View = view!!
}