package com.gaoding.editor.image.demo.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 获取 MimeType
 * https://stackoverflow.com/questions/8589645/how-to-determine-mime-type-of-file-in-android
 */
fun File.getMimeType(context: Context): String? {

    if (this.isDirectory) {
        return null
    }

    var mimeType: String? = null
    try {
        // 尝试使用 BitmapFactory 获取
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        mimeType = options.outMimeType
    } catch (e: Exception) {
        e.printStackTrace()
    }
    if (!TextUtils.isEmpty(mimeType)) {
        return mimeType
    }

    fun fallbackMimeType(uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase())
        }
    }

    fun catchUrlMimeType(): String? {
        val uri = Uri.fromFile(this)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val path = Paths.get(uri.toString())
            try {
                Files.probeContentType(path) ?: fallbackMimeType(uri)
            } catch (ignored: IOException) {
                fallbackMimeType(uri)
            }
        } else {
            fallbackMimeType(uri)
        }
    }

    var stream: FileInputStream? = null
    return try {
        stream = this.inputStream()
        URLConnection.guessContentTypeFromStream(stream) ?: catchUrlMimeType()
    } catch (ignored: Exception) {
        catchUrlMimeType()
    } finally {
        stream?.close()
    }
}

fun String.getMimeType(context: Context): String? {
    return File(this).getMimeType(context)
}
