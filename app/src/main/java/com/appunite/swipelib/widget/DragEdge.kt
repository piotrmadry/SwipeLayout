package com.appunite.swipelib.widget

sealed class DragEdge {
    object Left : DragEdge()
    object Right : DragEdge()
    object Side : DragEdge()
}