package com.daykon.irontracker.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest


class CropTransformation(_top: Int,
                         _right: Int,
                         _bottom: Int,
                         _left: Int) : BitmapTransformation() {

  private val top = _top
  private val left = _left
  private val right = _right
  private val bottom = _bottom

  override fun transform(pool: BitmapPool,
                         source: Bitmap, outWidth: Int,
                         outHeight: Int): Bitmap {
    val sourceRect = Rect(left, top, right, bottom)
    val targetRect = Rect(0, 0, right - left, bottom - top)
    val bitmap = pool.get(right - left,
        bottom - top,
        Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    canvas.drawBitmap(source, sourceRect, targetRect, null)

    return bitmap
  }

  override fun updateDiskCacheKey(messageDigest: MessageDigest) {
    messageDigest.update(("crop($top,$right,$bottom,$left)").toByteArray())
  }
}