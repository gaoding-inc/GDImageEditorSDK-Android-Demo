package com.gaoding.editor.image.demo.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.File

/**
 * 描述: 文件路径工具类
 */
object FilePathUtils {

    private var sdCachePath: String? = null
    private var externalFilesCachePath: String? = null

    /**
     * 获取外部存储的私有的目录
     *
     * @param context 上下文
     * @return storage/sdcard/Android/data/包名/files
     */
    @JvmStatic
    fun getPrivateExternalFilesDir(context: Context?): String {
        context ?: return ""
        if (!TextUtils.isEmpty(externalFilesCachePath)) {
            return externalFilesCachePath ?: ""
        }

        try {
            var targetFilesDirPath: String? = null
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                targetFilesDirPath = context.getExternalFilesDir("")?.absolutePath
            } else {
                val cacheDir = context.cacheDir
                if (cacheDir != null && cacheDir.exists()) {
                    targetFilesDirPath = cacheDir.path
                }
            }
            if (targetFilesDirPath == null) {
                return ""
            }
            val targetFilesDir = File(targetFilesDirPath)
            if (!targetFilesDir.exists()) {
                targetFilesDir.mkdirs()
            }
            externalFilesCachePath = targetFilesDirPath
            return targetFilesDirPath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    /**
     * 获取图片sdcard存储目录，如编辑器导出的图片目录
     *
     * @param context 上下文
     * @return 图片sdcard存储目录
     */
    @JvmStatic
    fun getImageSaveDir(context: Context?): String {
        context ?: return ""
        var saveDir: String? = null
        if (isVivo()) {
            saveDir = getSDCacheDir(context, "相机/")
        }
        if (saveDir != null && FileUtils.isExist(saveDir)) {
            return saveDir
        }

        // 优先存在/sdcard/pictures/sdk/目录下
        // 如果这个不存在，则创建一下
        // 如果创建不成功，则直接使用/sdcard/pictures/目录
        // 如果这个目录也不存在，则使用/sdcard/DCIM/目录
        // 记住，不要使用/sdcard/DCIM/Camera/目录，因为这个目录在部分手机（如小米）会有单独权限（跟写外部存储权限不一样），可能会导致无法访问
        val picturesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + File.separator
        saveDir = picturesDir + "sdk" + File.separator
        val isDirExist = isDirExists(saveDir)
        if (isDirExist) {
            return saveDir
        }

        // 创建/sdcard/Pictures/sdk/失败，则尝试使用/sdcard/pictures/目录
        // 注意/sdcard/pictures/并不会尝试去创建
        // 如果/sdcard/pictures/目录也不存在，则直接使用/sdcard/DCIM/目录
        return if (FileUtils.isExist(picturesDir)) {
            picturesDir
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path + File.separator
        }
    }


    /**
     * 获取在sd的根目录的地址
     *
     * @param context 上下文
     * @param dirName 指定路径
     */
    @JvmStatic
    fun getSDCacheDir(context: Context?, dirName: String): String {
        context ?: return ""
        if (!TextUtils.isEmpty(sdCachePath)) {
            return sdCachePath + File.separator + dirName
        }

        var cachePath: String? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            cachePath = Environment.getExternalStorageDirectory().absolutePath
        }
        if (cachePath == null) {
            val cacheDir = context.cacheDir
            if (cacheDir != null && cacheDir.exists()) {
                cachePath = cacheDir.path
            }
        }
        sdCachePath = cachePath
        return cachePath + File.separator + dirName
    }

    /**
     * 判断dir是否存在，如果不存在则尝试创建
     */
    private fun isDirExists(dir: String): Boolean {
        if (FileUtils.isExist(dir)) {
            return true
        }
        var mkdirSuccess = false
        try {
            mkdirSuccess = File(dir).mkdirs()
        } catch (throwable: Throwable) {
            // do nothing
            Log.d("FilePathUtils", dir + " mkdirs exception: " + throwable.message)
        }
        return mkdirSuccess
    }

    /**
     * 判断品牌：vivo
     */
    private fun isVivo(): Boolean {
        return "vivo".equals(Build.BRAND, ignoreCase = true)
    }
}