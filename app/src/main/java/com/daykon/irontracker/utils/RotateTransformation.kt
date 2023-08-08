package com.daykon.irontracker.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class RotateTransformation(
    _angle: Float) : BitmapTransformation() {

    private val angle = _angle

    override fun transform(pool: BitmapPool,
                           source: Bitmap, outWidth: Int,
                           outHeight: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(source, 0,0,source.width, source.height,matrix, true)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest){
        messageDigest.update(("rotate($angle)").toByteArray())
    }
}