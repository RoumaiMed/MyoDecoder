package com.roumai.myodecoder.ui.utils

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

object ToastManager {
    private var toast: Toast? = null

    fun showToast(context: Context, message: String) {
        toast?.cancel()
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            val layoutParams = LinearLayout.LayoutParams(
                dpToPx(context, 50),
                dpToPx(context, 16)
            )
            addView(contentLayout, layoutParams)
        }
        val textView = TextView(context).apply {
            text = message
            textSize = 16f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        mainLayout.addView(textView)
        toast = Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = mainLayout
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
        }
        toast?.show()
    }
}

fun dpToPx(context: Context, dp: Int): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density).toInt()
}