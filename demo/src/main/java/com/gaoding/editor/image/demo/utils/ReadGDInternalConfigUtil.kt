package com.gaoding.editor.image.demo.utils

import android.content.Context
import org.json.JSONObject

/**
 * 读取稿定内部开发测试配置工具类
 */
object ReadGDInternalConfigUtil {

    private const val GD_INTERNAL_CONFIG_FILE_NAME = "GDInternalConfig.json"

    /**
     * 是否显示"测试on_message"入口
     * 从assets目录下读取GDInternalConfig.json文件
     */
    fun needShowTestOnMessage(context: Context): Boolean {
        return try {
            val jsonStr =
                ReadConfigUtil.readFileContent(context.assets.open(GD_INTERNAL_CONFIG_FILE_NAME)) ?: return false
            val jsonObj = JSONObject(jsonStr)
            return jsonObj.optBoolean("showTestOnMessage")
        } catch (t: Throwable) {
            false
        }
    }

    fun needShowSwitchEnv(context: Context): Boolean {
        return try {
            val jsonStr =
                ReadConfigUtil.readFileContent(context.assets.open(GD_INTERNAL_CONFIG_FILE_NAME)) ?: return false
            val jsonObj = JSONObject(jsonStr)
            return jsonObj.optBoolean("showSwitchEnv")
        } catch (t: Throwable) {
            false
        }
    }

}