package com.dr.qck.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.dr.qck.R

object AndroidUtils {
    fun viewToBitmap(view: View, theme: String): Bitmap {
        view as ViewGroup
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}