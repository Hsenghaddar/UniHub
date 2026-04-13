package com.example.unihub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import java.io.InputStream

/**
 * ImageUtils provides helper methods for efficient image loading and manipulation.
 * It focuses on decoding bitmaps from Uris with downsampling to prevent OutOfMemory errors.
 */
object ImageUtils {

    /**
     * Decodes a sampled bitmap from a given Uri.
     * Uses two passes: first to get dimensions, then to decode the actual scaled-down bitmap.
     * @param context Application context
     * @param uri The Uri of the image to load
     * @param reqWidth The required width of the resulting bitmap
     * @param reqHeight The required height of the resulting bitmap
     * @return A scaled Bitmap or null if decoding fails
     */
    fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        try {
            // Pass 1: Decode with inJustDecodeBounds=true to check dimensions
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate the power-of-two scaling factor
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Pass 2: Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Calculates the largest inSampleSize value that is a power of 2 and keeps both
     * height and width larger than the requested height and width.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Loads an image from a Uri into an ImageView with automatic resizing based on the view's dimensions.
     * Uses a post block to ensure view dimensions are measured before loading.
     */
    fun loadImage(context: Context, uri: Uri, imageView: ImageView) {
        // Wait for the view to be laid out to get its actual dimensions
        imageView.post {
            val width = if (imageView.width > 0) imageView.width else 500
            val height = if (imageView.height > 0) imageView.height else 500
            
            val bitmap = decodeSampledBitmapFromUri(context, uri, width, height)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                // Set a default gallery icon if loading fails
                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }
}
