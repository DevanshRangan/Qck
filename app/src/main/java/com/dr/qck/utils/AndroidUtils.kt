package com.dr.qck.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

object AndroidUtils {
    fun viewToBitmap(view: View, theme: String): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap

    }

    private fun extractColor(drawable: Drawable): Int {
        val bm = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val c = Canvas(bm)
        drawable.draw(c)
        return bm.getPixel(0, 0)
    }

    private fun snapshotTextureViews(view: View, loc: IntArray, x: Int, y: Int, canvas: Canvas) {
        Log.d("Called", "hehh")
        if (view is TextureView) {
            view.getLocationInWindow(loc)
            val snapshot = view.bitmap
            snapshot?.let {
                canvas.save()
                canvas.drawBitmap(it, (loc[0] - x).toFloat(), (loc[1] - y).toFloat(), null)
                canvas.restore()
                it.recycle()
            }
        }
        if (view is ViewGroup) {
            view.children.forEach {
                snapshotTextureViews(it, loc, x, y, canvas)
            }
        }
    }
}