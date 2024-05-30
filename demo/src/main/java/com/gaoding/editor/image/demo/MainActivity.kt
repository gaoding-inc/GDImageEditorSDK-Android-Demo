package com.gaoding.editor.image.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.gaoding.editor.image.api.EditorMode
import com.gaoding.editor.image.api.FetchAuthCodeBean
import com.gaoding.editor.image.api.GDImageEditorSDK
import com.gaoding.editor.image.api.IGDImageEditorSDKCallback
import com.gaoding.editor.image.config.GDEnvType
import com.gaoding.editor.image.config.GDUrls
import com.gaoding.editor.image.demo.bean.OnMessageInputModel
import com.gaoding.editor.image.demo.bean.OnMessageOutputModel
import com.gaoding.editor.image.demo.databinding.ActivityMainBinding
import com.gaoding.editor.image.demo.utils.FileDownloaderUtil
import com.gaoding.editor.image.demo.utils.SPUtils
import com.gaoding.editor.image.demo.widget.LoadingDialog
import com.gaoding.editor.image.utils.EnvUtil
import com.gaoding.editor.image.utils.LogUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * demo activity
 * Created by kongran on 2023/1/11
 * E-Mail Address：kongran@gaoding.com
 */
class MainActivity : Activity() {

    private val mImageEditor by lazy {
        GDImageEditorSDK(this@MainActivity)
    }

    // sp存储
    private val mSPUtils by lazy {
        SPUtils.getInstance(applicationContext, SP_NAME)
    }

    private lateinit var binding: ActivityMainBinding

    // loading
    private var mLoadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initGDImageEditorSDK()
        initListener()
        initViews()
        initData()
        FileDownloaderUtil.init(this)
        Toast.makeText(this, if (isDebug(this)) "当前是debug" else "当前是release", Toast.LENGTH_SHORT)
            .show()
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

            override fun onMessage(
                type: String,
                params: Map<String, String>?,
                callback: (Any?) -> Unit
            ) {
                // onMessage用于js与接入方通信的通用方法，新增接口会使用onMessage进行通信，这样可以避免频繁升级SDK
                // 如果是其他类型，可能需要有返回值，具体业务场景需要具体处理
                binding.layoutTestOpenPage.tvOnMessageInputParams.text =
                    wrapOnMessageInput(type, params)
                // 将输入框的返回值通过sdk返回给js(仅测试使用)
                getOnMessageOutput()?.let {
                    Toast.makeText(this@MainActivity, Gson().toJson(it), Toast.LENGTH_LONG).show()
                    callback(it)
                    return
                }
                when (type) {
                    "template.select" -> {
                        // 模版中心选择一个模版
                        val templateId = params?.get("id") ?: ""
                        val mode = params?.get("mode")
                        val editorMode: EditorMode =
                            if (TextUtils.equals(EditorMode.USER.mode, mode)) {
                                EditorMode.USER
                            } else {
                                EditorMode.TEMPLATE
                            }
                        mImageEditor.openPage(
                            GDUrls.Path.EDITOR,
                            mapOf(
                                GDUrls.QueryKey.KEY_ID to templateId,
                                GDUrls.QueryKey.KEY_MODE to editorMode.mode
                            )
                        )
                        callback(null)
                    }
                    "editor.save.complete" -> {
                        // 作图记录保存完成
                        val isAutoSave = params?.get("isAutoSave")
                        val workId = params?.get("workId")
                        val hasRiskMaterials = params?.get("hasRiskMaterials")
                        // 作图完成之后将作图记录id设置到作图记录id输入框中，同时将sourceId和imageUrl也设置到"打开结果页"的对应输入框中
                        binding.layoutTestOpenPage.etWorksId.setText(workId)

                        // 可以在这里进行控制是否继续导出的逻辑，如弹出付费弹窗等

                        // 根据回调的结果，来判断是否继续导出（true：继续，false：中断）
                        callback(true)
                    }
                    "editor.export.complete" -> {
                        GlobalScope.launch(Dispatchers.Main) {
                            // 导出完成
                            val workId = params?.get("workId")
                            val urls = params?.get("urls")
                            val urlArray = JSONArray(urls)
                            val urlList = arrayListOf<String>()
                            for (index in 0 until urlArray.length()) {
                                urlList.add(urlArray.getString(index))
                            }

                            // 下载保存到相册
                            showLoadingDialog()
                            withContext(Dispatchers.IO) {
                                urlList.forEach {
                                    FileDownloaderUtil.downloadFileToAlbum(this@MainActivity, it)
                                }
                            }
                            dismissLoadingDialog()

                            // 导出完成后，可以自定义跳转到指定页面，如完成页等
                            // 跳转到自定义的页面，可以参考如下逻辑
                            startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                            // 关闭编辑器所有页面
                            mImageEditor.dismiss()

                            callback(null)
                        }
                    }
                    else -> callback(null)
                }
            }

            override fun getParamsForFetchAuthCode(): FetchAuthCodeBean? {
                val etAkContent = binding.layoutTestOpenPage.etAkContent.text?.toString()
                val etSkContent = binding.layoutTestOpenPage.etSkContent.text?.toString()
                val etUidContent = binding.layoutTestOpenPage.etUidContent.text?.toString()
                return if (!etAkContent.isNullOrEmpty()
                    && !etSkContent.isNullOrEmpty()
                    && !etUidContent.isNullOrEmpty()
                ) {
                    FetchAuthCodeBean(uid = etUidContent, ak = etAkContent, sk = etSkContent)
                } else {
                    FetchAuthCodeBean(uid = "666", ak = "您的ak值", sk = "您的sk值")
                }
            }

            override fun getAuthCode(
                context: Context,
                authCodeBean: FetchAuthCodeBean?,
                abilityCode: String,
                callback: AuthCallback
            ) {
                // 可重写该方法，自己实现获取authCode的逻辑
                super.getAuthCode(context, authCodeBean, abilityCode, callback)
            }

            override fun getPageConfig(): Map<String, Any>? {
                // 可重写该方法，自己实现要配置的功能，不重写则使用默认配置
//                 return super.getPageConfig()
                return mapOf(
                    // 禁止功能列表
                    "forbidFunctionList" to arrayOf<String>("watermark"),
                    // 保存按钮文案
                    "saveBtnName" to "下载"
                )
            }
        })
    }

    private fun wrapOnMessageInput(type: String, params: Map<String, String>?): String {
        // {type: 'select_template', data: xxx}
        val onMessageModel = OnMessageInputModel(
            type = type,
            data = params
        )

        return Gson().toJson(onMessageModel)
    }

    /**
     * 获取onMessage返回值
     */
    private fun getOnMessageOutput(): Any? {
        if (binding.layoutTestOpenPage.llTestOnMessage.visibility != View.VISIBLE) {
            return null
        }

        // 优先获取对象输入框的内容，然后进行反序列化
        // 如果对象输入框的内容为空或者不合法，则取字符串输入框的内容
        val etOnMessageOutputObj = binding.layoutTestOpenPage.etOnMessageOutputObject
        if (etOnMessageOutputObj.text.isNotBlank()) {
            return try {
                Gson().fromJson(
                    etOnMessageOutputObj.text.toString(),
                    OnMessageOutputModel::class.java
                )
            } catch (e: Throwable) {
                null
            }
        } else {
            // 取字符串输入框
            val etOnMessageOutputStr = binding.layoutTestOpenPage.etOnMessageOutputStr
            return etOnMessageOutputStr.text.toString()
        }
    }

    /**
     * 打开模版中心
     */
    private fun initOpenTemplateCenter() {
        binding.layoutTestOpenPage.btnOpenTempalteCenter.setOnClickListener {
            mImageEditor.openPage(GDUrls.Path.TEMPLATE_CENTER, null)
        }
    }

    /**
     * 打开编辑器，分以下两种情况：
     * 1. 以指定模版id的形式打开编辑器
     * 2. 以作图记录id的形式打开编辑器
     */
    private fun initOpenImageEditor() {
        // 指定模版id打开编辑器
        binding.layoutTestOpenPage.btnOpenTemplateConfirm.setOnClickListener {
            // 获取editText输入框中的模板id
            val templateId = binding.layoutTestOpenPage.etTemplateId.text?.toString()
                ?: return@setOnClickListener

            if (templateId.isBlank()) {
                Toast.makeText(this, "模版id不能为空", Toast.LENGTH_SHORT).show()
                binding.layoutTestOpenPage.etTemplateId.requestFocus()
                return@setOnClickListener
            }

            // 以指定模版id的形式打开编辑器
            // prod: {"mode": "company", "id": 14026604557898833}
            // fat: {"mode": "company", "id": 20359890549361684}
            mImageEditor.openPage(
                GDUrls.Path.EDITOR, mapOf(
                    GDUrls.QueryKey.KEY_ID to templateId,
                    GDUrls.QueryKey.KEY_MODE to EditorMode.TEMPLATE.mode
                )
            )
        }

        // 以workId的方式打开编辑器（作图记录再编辑）
        binding.layoutTestOpenPage.btnOpenWorksConfirm.setOnClickListener {
            // 获取editText输入框中的模板id
            val workId =
                binding.layoutTestOpenPage.etWorksId.text?.toString() ?: return@setOnClickListener

            if (workId.isBlank()) {
                Toast.makeText(this, "作图记录id不能为空", Toast.LENGTH_SHORT).show()
                binding.layoutTestOpenPage.etWorksId.requestFocus()
                return@setOnClickListener
            }

            mImageEditor.openPage(
                GDUrls.Path.EDITOR, mapOf(
                    GDUrls.QueryKey.KEY_ID to workId,
                    GDUrls.QueryKey.KEY_MODE to EditorMode.USER.mode
                )
            )
        }
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
            if (path.isBlank()) {
                Toast.makeText(this, "path不能为空", Toast.LENGTH_SHORT).show()
                binding.layoutTestOpenPage.etOpenPatePath.requestFocus()
                return@setOnClickListener
            }
            val queryParams = binding.layoutTestOpenPage.etOpenPateQuery.text.toString()
            mImageEditor.openPage(path, queryParams)
        }
    }

    /**
     * 保存自定义参数
     */
    private fun initSaveParams() {
        binding.layoutTestOpenPage.btnConfirmParams.setOnClickListener {
            saveParams()
        }
    }

    private fun initData() {
        val ak = mSPUtils.getString(AK_KEY, "")
        val sk = mSPUtils.getString(SK_KEY, "")
        val uid = mSPUtils.getString(UID_KEY, "")
        if (ak.isNotEmpty()) {
            binding.layoutTestOpenPage.etAkContent.setText(ak)
        }
        if (sk.isNotEmpty()) {
            binding.layoutTestOpenPage.etSkContent.setText(sk)
        }
        if (uid.isNotEmpty()) {
            binding.layoutTestOpenPage.etUidContent.setText(uid)
        }
    }

    private fun initViews() {
        // 如果是稿定内部使用，则显示"测试on_message"入口、显示切换环境操作
        val needShowTestOnMessageBtn = false
        binding.layoutTestOpenPage.btnOpenTestOnMessagePage.visibility =
            if (needShowTestOnMessageBtn) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.layoutTestOpenPage.llTestOnMessage.visibility = if (needShowTestOnMessageBtn) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.layoutTestOpenPage.llCustomParams.visibility = View.GONE

        val needShowSwitchEnv = false
        binding.layoutSwitchEnv.llSwitchEnv.visibility = if (needShowSwitchEnv) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun initListener() {
        initOpenTemplateCenter()
        initOpenImageEditor()
        initCustomOpenPage()
        initSaveParams()

        // 以下仅稿定内部开发测试使用，接入方客户无需关心
        initChangeEnv()
        initTestOnMessage()
    }

    private fun showLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog?.isShowing == true) {
            return
        }

        if (mLoadingDialog == null) {
            mLoadingDialog = LoadingDialog(
                mImageEditor.getTopImageEditorActivity(),
                "保存相册中"
            )
            mLoadingDialog?.setShowCancel(true)
            mLoadingDialog?.setOnCancelListener { dismissLoadingDialog() }
        }
        mLoadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog?.isShowing == true) {
            mLoadingDialog?.dismiss()
            mLoadingDialog = null
        }
    }

    private fun saveParams() {
        val etAkContent = binding.layoutTestOpenPage.etAkContent.text?.toString() ?: ""
        val etSkContent = binding.layoutTestOpenPage.etSkContent.text?.toString() ?: ""
        val etUidContent = binding.layoutTestOpenPage.etUidContent.text?.toString() ?: ""
        mSPUtils.put(AK_KEY, etAkContent)
        mSPUtils.put(SK_KEY, etSkContent)
        mSPUtils.put(UID_KEY, etUidContent)
    }

    companion object {
        private const val TAG = "MainActivity"
        const val SP_NAME = "image_editor_demo"
        const val AK_KEY = "AK"
        const val SK_KEY = "SK"
        const val UID_KEY = "UID"
    }
}