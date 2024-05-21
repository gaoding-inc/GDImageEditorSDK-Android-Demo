package com.gaoding.editor.image.demo.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.*

/**
 * 拷贝文件帮助类
 */
object CopyFileUtils {

    sealed class Result {
        data class Success(val path: String) : Result()
        data class Fail(val msg: String?) : Result()
    }

    private const val TAG = "CopyFileUtils"

    @JvmStatic
    fun copyFile(oldPath: String?, newPath: String?): Result {
        oldPath ?: return Result.Fail("oldPath is null")
        newPath ?: return Result.Fail("newPath is null")

        try {
            if (!FileUtils.makeDirectoryExist(newPath)) {
                return Result.Fail("makeDirectoryExist return false")
            }

            val isSuccess =
                writeInputStreamToOutputStream(FileInputStream(oldPath), FileOutputStream(newPath))
            if (!isSuccess) {
                return Result.Fail("writeInputStreamToOutputStream fail")
            }
            return Result.Success(newPath)
        } catch (t: Throwable) {
            return Result.Fail(t.message)
        }
    }

    @JvmStatic
    fun copyFileToShareDirAndDelete(context: Context, srcPath: String): Result {
        val result = copyFileToShareDir(context, srcPath)
        if (result is Result.Success) {
            FileUtils.delete(srcPath)
        }
        return result
    }

    /**
     * copy 文件到共享文件夹
     * @param srcPath
     */
    @JvmStatic
    fun copyFileToShareDir(context: Context, srcPath: String, fileName: String): Result {
        try {
            val copyResult = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // 扩展名，如.jpg, .png
                //  先尝试保存到固定的目录
                val newFilePath =
                    FilePathUtils.getImageSaveDir(context) + fileName
                val result = copyFile(srcPath, newFilePath)
                if (result is Result.Success) {
                    if (MediaUtils.isImageFileType(srcPath)) {
                        MediaUtils.insertSysPictureDatabase(
                            context,
                            newFilePath
                        )
                    } else if (MediaUtils.isVideoFileType(srcPath)) {
                        MediaUtils.insertSysVideoDatabase(
                            context,
                            newFilePath
                        )
                    }
                }
                result
            } else {
                copyFileToMediaStoreDir(context, srcPath, fileName)
            }
            return if (copyResult is Result.Success) {
                Result.Success(copyResult.path)
            } else {
                Result.Fail("保存失败")
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            return Result.Fail(t.message)
        }
    }

    /**
     * copy 文件到共享文件夹
     */
    @JvmStatic
    fun copyFileToShareDir(context: Context, srcPath: String): Result {
        val fileName = FileUtils.getFileName(srcPath)
        val lastDotIdx = fileName.lastIndexOf('.')
        // 扩展名，如.jpg, .png
        val extension = if (lastDotIdx == -1) "" else fileName.substring(lastDotIdx)
        //  先尝试保存到固定的目录
        val copyFileName = System.currentTimeMillis().toString() + extension
        return copyFileToShareDir(context, srcPath, copyFileName)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun copyFileToMediaStoreDir(
        context: Context,
        srcPath: String,
        destName: String
    ): Result {
        try {
            val mineType = srcPath.getMimeType(context) ?: return Result.Fail("获取文件类型失败")
            val uri =
                if (MediaUtils.isImageFileType(srcPath) || MediaUtils.isGifFileType(srcPath)) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else if (MediaUtils.isVideoFileType(srcPath)) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
            val contentValues = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, destName)
                put(MediaStore.Files.FileColumns.TITLE, destName)
                put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Files.FileColumns.MIME_TYPE, mineType)
            }
            val resolver = context.contentResolver
            val insertUri = resolver.insert(uri, contentValues)
            insertUri?.let { uri ->
                val isSuccess =
                    writeInputStreamToOutputStream(
                        FileInputStream(srcPath),
                        resolver.openOutputStream(uri)
                    )
                if (!isSuccess) {
                    return Result.Fail("writeInputStreamToOutputStream fail")
                }
            } ?: return Result.Fail("insertUri is null")
            val path = FileUtils.getUriPath(context, insertUri) ?: return Result.Fail("path is null")
            scaleFile(context, path, mineType)
            return Result.Success(path)
        } catch (t: Throwable) {
            Log.d(TAG, t.message ?: "")
            return Result.Fail(t.message)
        }
    }

    private fun scaleFile(context: Context, path: String, mimeType: String) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(path),
            arrayOf(mimeType),
            MediaScannerConnection.OnScanCompletedListener { path, uri ->
                uri?.let {
                    Log.d(TAG, "scaleFile Success $it")
                }
            })
    }

    private fun writeInputStreamToOutputStream(
        inputStream: InputStream?,
        outputStream: OutputStream?
    ): Boolean {
        inputStream ?: return false
        outputStream ?: return false

        var input: BufferedInputStream? = null
        var out: BufferedOutputStream? = null
        return try {
            input = BufferedInputStream(inputStream)
            out = BufferedOutputStream(outputStream)
            val buf = ByteArray(4096)
            var len: Int
            while (input.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            input?.close()
            out?.close()
            inputStream.close()
            outputStream.close()
        }
    }

}