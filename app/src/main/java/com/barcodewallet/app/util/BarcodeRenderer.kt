package com.barcodewallet.app.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

object BarcodeRenderer {
    fun render(value: String, format: String, width: Int, height: Int): Bitmap? {
        return try {
            val zxingFormat = runCatching { BarcodeFormat.valueOf(format) }
                .getOrDefault(BarcodeFormat.CODE_128)
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            val matrix = MultiFormatWriter().encode(value, zxingFormat, width, height, hints)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }
}
