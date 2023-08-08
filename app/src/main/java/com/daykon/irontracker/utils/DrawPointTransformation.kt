package com.daykon.irontracker.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class DrawPointTransformation(
    _points: List<Float>,
    _size:Float=5f,
    _color: Int =0xff0000) : BitmapTransformation() {

    private val points = _points
    private val size = _size
    private val color = _color

    override fun transform(pool: BitmapPool,
                           source: Bitmap, outWidth: Int,
                           outHeight: Int): Bitmap {
        val w = source.width
        val h = source.height
        val bitmap = pool.get(w,h, Bitmap.Config.ARGB_8888)//Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.density = source.density
        //setCanvasBitmapDensity(source, bitmap)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = color
        paint.alpha = 0xff
        paint.style = Paint.Style.FILL

        Log.d("TESTDEBUG", "point ${points[0]},${points[1]}")
        canvas.drawBitmap(source, 0f, 0f, paint)
        canvas.drawCircle(points[0], points[1], size, paint)
        //source.recycle()

        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest){
        messageDigest.update(("drawPoint($points,$size,$color)").toByteArray())
    }
}