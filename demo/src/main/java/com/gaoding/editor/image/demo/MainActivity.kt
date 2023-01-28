package com.gaoding.editor.image.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.Toast
import com.gaoding.editor.image.api.EditorMode
import com.gaoding.editor.image.api.GDImageEditorSDK
import com.gaoding.editor.image.api.IGDImageEditorSDKCallback
import com.gaoding.editor.image.config.GDEnvType
import com.gaoding.editor.image.config.GDUrls
import com.gaoding.editor.image.demo.databinding.ActivityMainBinding
import com.gaoding.editor.image.utils.EnvUtil
import com.gaoding.editor.image.utils.LogUtils


/**
 * demo activity
 * Created by kongran on 2023/1/11
 * E-Mail Address：kongran@gaoding.com
 */
class MainActivity : Activity() {

    private val mImageEditor by lazy {
        GDImageEditorSDK(this@MainActivity)
    }

    private var mWorkId: String? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initGDImageEditorSDK()
        initListener()
    }

    /**
     * 判断是否debug
     */
    private fun isDebug(context: Context): Boolean {
        return context.applicationInfo != null &&
                context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    /**
     * 初始化GDImageEditorSDK
     */
    private fun initGDImageEditorSDK() {
        // 如果需要显示SDK里头的日志，可将其设置为true，建议只有debug的时候才设置为true
        mImageEditor.setLogEnable(isDebug(this))

        mImageEditor.setCallback(object : IGDImageEditorSDKCallback() {

            override fun onMessage(type: String, params: Map<String, String>?): String? {
                // 如果是其他类型，可能需要有返回值，具体业务场景需要具体处理
                return when (type) {
                    "select_template" -> {
                        // 模版中心选择一个模版
                        mImageEditor.openPage(GDUrls.Path.EDITOR, params)
                        null
                    }
                    "editor_edit_complete" -> {
                        // 作图完成
                        mImageEditor.openPage(GDUrls.Path.WORKS_COMPLETE, params)
                        null
                    }
                    else -> null
                }
            }

            override fun onTemplateClick(templateId: String, mode: EditorMode) {
                // 1. 旧版接口
//                mImageEditor.openImageEditor(templateId, mode)
                // 2. 新版通用接口
                mImageEditor.openPage(
                    GDUrls.Path.EDITOR, mapOf(
                        GDUrls.QueryKey.KEY_ID to templateId,
                        GDUrls.QueryKey.KEY_MODE to mode.mode
                    )
                )
            }

            override fun onEditCompleted(workId: String, sourceId: String, imageUrl: String) {
                mWorkId = workId
                // 1. 旧版接口
//                mImageEditor.openCompletePage(workId, sourceId, imageUrl)
                // 2. 新版通用接口
                mImageEditor.openPage(
                    GDUrls.Path.WORKS_COMPLETE, mapOf(
                        GDUrls.QueryKey.KEY_TEMPLATE_ID to workId,
                        GDUrls.QueryKey.KEY_SOURCE_ID to sourceId,
                        GDUrls.QueryKey.KEY_IMAGE to imageUrl
                    )
                )
            }

            override fun onDownloadEditResult(imageUrl: String) {
                LogUtils.d(TAG, "onDownloadEditResult:$imageUrl")
            }

            override fun getUid(): String {
                // 获取uid，接入方系统内用户唯一标识，稿定系统根据该标识创建关联游客账号。 用于authCode的生成
                return "666"
            }

            override fun getAuthCode(context: Context, uid: String, callback: AuthCallback) {
                // 可重写该方法，自己实现获取authCode的逻辑
                super.getAuthCode(context, uid, callback)
            }

            override fun onEditorDestroyed() {
                Toast.makeText(this@MainActivity, "编辑器页面销毁", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * 打开模版中心
     */
    private fun initOpenTemplateCenter() {
        binding.layoutTestOpenPage.btnOpenTempalteCenter.setOnClickListener {
            // 1. 旧版打开模版中心接口
//            mImageEditor.openTemplateCenter()
            // 2. 新版通用接口
            mImageEditor.openPage(GDUrls.Path.TEMPLATE_CENTER, null)
        }
    }

    /**
     * 打开编辑器，分以下两种情况：
     * 1. 以指定模版id的形式打开编辑器
     * 2. 以作图记录id的形式打开编辑器
     */
    private fun initOpenImageEditor() {
        binding.layoutTestOpenPage.btnOpenImageEditor.setOnClickListener {
            val workId = mWorkId
            if (workId.isNullOrEmpty()) {
                // 以指定模版id的形式打开编辑器
                // 1. 旧版接口
//                mImageEditor.openImageEditor("14026604557898833", EditorMode.COMPANY)
                // 2. 新版通用接口
                // prod: {"mode": "company", "id": 14026604557898833}
                // fat: {"mode": "company", "id": 16530970696242224}
                mImageEditor.openPage(
                    GDUrls.Path.EDITOR, mapOf(
                        GDUrls.QueryKey.KEY_ID to "14026604557898833",
                        GDUrls.QueryKey.KEY_MODE to EditorMode.COMPANY.mode
                    )
                )
            } else {
                // 以作图记录id的形式打开编辑器
                // 1. 旧版接口
//                mImageEditor.openImageEditor(workId, EditorMode.USER)
                // 2. 新版通用接口
                mImageEditor.openPage(
                    GDUrls.Path.EDITOR, mapOf(
                        GDUrls.QueryKey.KEY_ID to workId,
                        GDUrls.QueryKey.KEY_MODE to EditorMode.USER.mode
                    )
                )
            }
        }
    }

    /**
     * 打开作品完成页
     */
    private fun initOpenComplete() {
        binding.layoutTestOpenPage.btnOpenComplete.setOnClickListener {
            // 1. 旧版接口
//            mImageEditor.openCompletePage(
//                "16519865385752592", "14026604557898833",
//                "https:\\/\\/gd-filems.dancf.com\\/saas\\/84jbso\\/49573\\/c264858d-fdd0-40ec-89a4-84e95f40463d8327319.png"
//            )
            // 2. 新版通用接口
            mImageEditor.openPage(
                GDUrls.Path.WORKS_COMPLETE, mapOf(
                    GDUrls.QueryKey.KEY_TEMPLATE_ID to "16519865385752592",
                    GDUrls.QueryKey.KEY_SOURCE_ID to "14026604557898833",
                    GDUrls.QueryKey.KEY_IMAGE to "https:\\/\\/gd-filems.dancf.com\\/saas\\/84jbso\\/49573\\/c264858d-fdd0-40ec-89a4-84e95f40463d8327319.png"
                )
            )
        }
    }

    /**
     * 清理本地缓存
     * 本地缓存存放着token等信息
     */
    private fun initClearCache() {
        binding.btnClearCache.setOnClickListener { mImageEditor.clearCache() }
    }

    /**
     * 测试on_message（内部使用，接入方客户无需关心）
     */
    private fun initTestOnMessage() {
        binding.layoutTestOpenPage.btnOpenTestOnMessagePage.setOnClickListener {
            mImageEditor.openPage(GDUrls.Path.TEST_ON_MESSAGE, null)
        }
    }

    /**
     * 初始化切换环境
     */
    private fun initChangeEnv() {
        val rgId2ChangeEnvFunMap = mapOf<Int, @GDEnvType String>(
            binding.layoutSwitchEnv.rbEnvLocal.id to GDEnvType.LOCAL,
            binding.layoutSwitchEnv.rbEnvDev.id to GDEnvType.DEV,
            binding.layoutSwitchEnv.rbEnvFat.id to GDEnvType.FAT,
            binding.layoutSwitchEnv.rbEnvStage.id to GDEnvType.STAGE,
            binding.layoutSwitchEnv.rbEnvProduct.id to GDEnvType.PRODUCT
        )

        // 从sp中读取之前缓存的env，设置到单选框中
        EnvUtil.getEnv(this).let {
            when (it) {
                GDEnvType.LOCAL -> binding.layoutSwitchEnv.rbEnvLocal.isChecked = true
                GDEnvType.DEV -> binding.layoutSwitchEnv.rbEnvDev.isChecked = true
                GDEnvType.FAT -> binding.layoutSwitchEnv.rbEnvFat.isChecked = true
                GDEnvType.STAGE -> binding.layoutSwitchEnv.rbEnvStage.isChecked = true
                GDEnvType.PRODUCT -> binding.layoutSwitchEnv.rbEnvProduct.isChecked = true
            }
        }

        binding.layoutSwitchEnv.rgChangeEnv.setOnCheckedChangeListener { _, checkedId ->
            val envType = rgId2ChangeEnvFunMap[checkedId] ?: return@setOnCheckedChangeListener
            EnvUtil.saveEnv(this, envType)
            mImageEditor.clearCache()
            // 切换环境最好重启app，这里主要是为了重启MainActivity
            restartApp()
        }
    }

    private fun restartApp() {
        finish()
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(packageName)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(intent)
//        exitProcess(0)
    }

    /**
     * 测试自定义参数的openPage，接入方客户无需关心
     */
    private fun initCustomOpenPage() {
        binding.layoutTestOpenPage.btnConfirmOpenPage.setOnClickListener {
            val path = binding.layoutTestOpenPage.etOpenPatePath.text.toString()
            val queryParams = binding.layoutTestOpenPage.etOpenPateQuery.text.toString()
            mImageEditor.openPage(path, queryParams)
        }
    }

    private fun initListener() {
        initOpenTemplateCenter()
        initOpenImageEditor()
        initOpenComplete()
        initClearCache()
        initCustomOpenPage()

        // 以下仅稿定内部开发测试使用，接入方客户无需关心
        initChangeEnv()
        initTestOnMessage()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}