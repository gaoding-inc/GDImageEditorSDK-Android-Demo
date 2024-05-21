package com.gaoding.editor.image.demo.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File

/**
 * 描述: 文件工具类
 */
object FileUtils {

    /**
     * 文件是否存在
     */
    fun isExist(path: String?): Boolean {
        if (path.isNullOrEmpty()) {
            return false
        }
        val file = File(path)
        return file.exists()
    }

    /**
     * 创建目录
     */
    fun makeDirectoryExist(dirPath: String?): Boolean {
        if (dirPath == null
            || dirPath.isEmpty()
            || !dirPath.contains(File.separator)
        ) {
            return false
        }
        var realDirPath = dirPath
        if (!dirPath.endsWith(File.separator)) {
            val index = dirPath.lastIndexOf(File.separator)
            realDirPath = dirPath.substring(0, index)
        }
        val dir = File(realDirPath)
        return !(!dir.exists() && !dir.mkdirs())
    }

    /**
     * 删除文件及文件下的子文件
     */
    fun delete(filePath: String) {
        delete(File(filePath))
    }

    /**
     * 删除文件及文件下的子文件
     */
    private fun delete(file: File) {
        if (file.isFile) {
            file.delete()
            return
        }
        if (file.isDirectory) {
            val childFiles = file.listFiles()
            if (childFiles == null || childFiles.size == 0) {
                file.delete()
                return
            }
            for (i in childFiles.indices) {
                delete(childFiles[i])
            }
            file.delete()
        }
    }

    /**
     * 获取文件名
     */
    fun getFileName(fileName: String): String {
        return fileName.replace(".*[\\\\/]|\\.[^\\.]*$‌​".toRegex(), "")
    }

    /**
     * 图片 uri转 path 绝对路径
     */
    fun getUriPath(context: Context, uri: Uri?): String? {
        if (null == uri) {
            return null
        }
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context, uri)) {
            val authority = uri.authority
            if ("com.android.externalstorage.documents" == authority) { //外部存储
                val docId = DocumentsContract.getDocumentId(uri)
                val divide = docId.split(":".toRegex()).toTypedArray()
                val type = divide[0]
                return if ("primary" == type) {
                    Environment.getExternalStorageDirectory().absolutePath + "/" + divide[1]
                } else {
                    "/storage/" + type + "/" + divide[1]
                }
            } else if ("com.android.providers.downloads.documents" == authority) { //下载目录
                val docId = DocumentsContract.getDocumentId(uri)
                if (docId.startsWith("raw:")) {
                    return docId.replaceFirst("raw:".toRegex(), "")
                }
                val downloadUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    docId.toLong()
                )
                return getAbsolutePath(
                    context,
                    downloadUri
                )
            } else if ("com.android.providers.media.documents" == authority) { //图片、影音档案
                val docId = DocumentsContract.getDocumentId(uri)
                val divide = docId.split(":".toRegex()).toTypedArray()
                val type = divide[0]
                var mediaUri: Uri? = null
                mediaUri = if ("image" == type) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                } else {
                    return null
                }
                mediaUri = ContentUris.withAppendedId(mediaUri, divide[1].toLong())
                return getAbsolutePath(context, mediaUri)
            }
        } else {
            val scheme = uri.scheme
            var path: String? = null
            if (scheme == null) {
                path = uri.path
            } else if (ContentResolver.SCHEME_FILE == scheme) {
                path = uri.path
            } else if (ContentResolver.SCHEME_CONTENT == scheme) {
                path = getAbsolutePath(context, uri)
            }
            return path
        }
        return null
    }

    private fun getAbsolutePath(context: Context, uri: Uri): String? {
        var path: String? = null
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
            if (null != cursor && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
                if (index > -1) path = cursor.getString(index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return path
    }


}