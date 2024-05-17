package com.gaoding.editor.image.demo.utils

import android.content.Context
import com.gaoding.editor.image.config.GDEnvType
import com.gaoding.editor.image.demo.model.GDConfigBean
import com.gaoding.editor.image.utils.EnvUtil
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * 读取配置文件（assets目录下的GDImageEditorSDKConfiguration.json）工具类
 * Created by kongran on 2023/1/12
 * E-Mail Address：kongran@gaoding.com
 */
object ReadConfigUtil {

    /**
     * 根据不同的网络环境获取到不同的json配置文件名
     */
    private fun getConfigFileName(context: Context): String {
        return when (EnvUtil.getEnv(context)) {
            GDEnvType.LOCAL -> "GDImageEditorSDKConfiguration_local.json"
            GDEnvType.DEV -> "GDImageEditorSDKConfiguration_dev.json"
            GDEnvType.FAT -> "GDImageEditorSDKConfiguration_fat.json"
            GDEnvType.STAGE -> "GDImageEditorSDKConfiguration_stage.json"
            GDEnvType.PRODUCT -> "GDImageEditorSDKConfiguration.json"
            else -> "GDImageEditorSDKConfiguration.json"
        }
    }

    /**
     * 从输入流中读取文本
     */
    fun readFileContent(inputStream: InputStream?): String? {
        inputStream ?: return null
        val inputStreamReader = InputStreamReader(inputStream)
        var reader: BufferedReader? = null
        val sb = StringBuilder()
        try {
            reader = BufferedReader(inputStreamReader)
            var tempStr: String?
            while (reader.readLine().also { tempStr = it } != null) {
                sb.append(tempStr)
            }
            reader.close()
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }
        return sb.toString()
    }

    /**
     * 从assets目录下读取GDImageEditorSDKConfiguration.json文件，然后返回model
     */
    fun read(context: Context, configFileName: String? = null): GDConfigBean? {
        return try {
            val curConfigFileName = configFileName ?: getConfigFileName(context)
            val jsonStr =
                readFileContent(context.assets.open(curConfigFileName)) ?: return null
            val jsonObj = JSONObject(jsonStr)
            val resConfigBean = GDConfigBean(
                thirdParty = jsonObj.optString("thirdParty"),
                thirdCateId = jsonObj.optString("thirdCateId"),
                ak = jsonObj.optString("AK"),
                sk = jsonObj.optString("SK")
            )
            resConfigBean
        } catch (t: Throwable) {
            null
        }
    }

}