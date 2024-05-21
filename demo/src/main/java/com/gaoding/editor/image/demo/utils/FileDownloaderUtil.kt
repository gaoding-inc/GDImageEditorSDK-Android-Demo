package com.gaoding.editor.image.demo.utils

import android.content.Context
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import java.io.File
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 描述: 文件下载类
 */
object FileDownloaderUtil {

    fun init(context: Context){
        FileDownloader.setup(context)
    }

    /**
     * 下载文件到指定路径
     */
    suspend fun downloadFileToAlbum(
        context: Context,
        url: String
    ): Boolean {
        return suspendCoroutine { cont ->
            val uri = URI(url)
            val lastDot = uri.path.lastIndexOf(".")
            if (lastDot < 0) {
                cont.resume(false)
            }
            val suffix = uri.path.substring(lastDot)
            val filePath =
                FilePathUtils.getPrivateExternalFilesDir(context) + File.separator + System.currentTimeMillis() + suffix
            FileDownloader.getImpl().create(url)
                .setPath(filePath)
                .setAutoRetryTimes(1)
                .setListener(object : FileDownloadListener() {
                    override fun pending(
                        task: BaseDownloadTask?,
                        soFarBytes: Int,
                        totalBytes: Int
                    ) {

                    }

                    override fun progress(
                        task: BaseDownloadTask?,
                        soFarBytes: Int,
                        totalBytes: Int
                    ) {
                    }

                    override fun completed(task: BaseDownloadTask?) {
                        CopyFileUtils.copyFileToShareDirAndDelete(context, filePath)
                        cont.resume(true)
                    }

                    override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                    }

                    override fun error(task: BaseDownloadTask?, e: Throwable?) {
                        cont.resume(false)
                    }

                    override fun warn(task: BaseDownloadTask?) {
                    }
                })
                .start()
        }
    }

}