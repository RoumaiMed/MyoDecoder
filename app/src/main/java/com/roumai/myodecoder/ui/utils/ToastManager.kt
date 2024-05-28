package com.roumai.myodecoder.ui.utils

import android.content.Context
import android.widget.Toast

object ToastManager {
    private var toast: Toast? = null

    fun showToast(context: Context, message: String) {
        toast?.cancel()
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }
}
