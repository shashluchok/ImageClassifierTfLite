package com.example.android.mlkitimagerecognition

import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


object Utils {

    fun writeResults(mapResults: Map<String, Float>): String? {
        var entryMax: Map.Entry<String, Float>? = null
        var entryMax1: Map.Entry<String, Float>? = null
        var entryMax2: Map.Entry<String, Float>? = null
        for (entry in mapResults.entries) {
            if (entryMax == null || entry.value.compareTo(entryMax.value) > 0) {
                entryMax = entry
            } else if (entryMax1 == null || entry.value.compareTo(entryMax1.value) > 0) {
                entryMax1 = entry
            } else if (entryMax2 == null || entry.value.compareTo(entryMax2.value) > 0) {
                entryMax2 = entry
            }
        }
        return """${entryMax!!.key.trim { it <= ' ' }} ${entryMax.value}
${entryMax1!!.key.trim { it <= ' ' }} ${entryMax1.value}
${entryMax2!!.key.trim { it <= ' ' }} ${entryMax2.value}
"""
    }

    fun getImageRotation(image: ImageProxy): Int {
        val rotation = image.imageInfo.rotationDegrees
        return rotation / 90
    }

    fun toBitmap(image: Image): Bitmap? {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        //U and V are swapped
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        val bitmap =  BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val bitmap2 = Bitmap.createScaledBitmap(bitmap, 224, 224, false);
        return bitmap2


    }
}