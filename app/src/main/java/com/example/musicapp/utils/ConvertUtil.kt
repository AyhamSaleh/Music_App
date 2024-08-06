package com.example.musicapp.utils

import android.content.Context
import android.view.ViewGroup

object ConvertUtil {

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
    fun ViewGroup.MarginLayoutParams.setMarginsDp(
        context: Context,
        leftDp: Int,
        topDp: Int,
        rightDp: Int,
        bottomDp: Int
    ) {
        val leftPx = dpToPx(context, leftDp)
        val topPx = dpToPx(context, topDp)
        val rightPx = dpToPx(context, rightDp)
        val bottomPx = dpToPx(context, bottomDp)
        setMargins(leftPx, topPx, rightPx, bottomPx)
    }
}