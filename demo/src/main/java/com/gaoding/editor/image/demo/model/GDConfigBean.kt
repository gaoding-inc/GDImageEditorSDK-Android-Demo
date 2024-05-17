package com.gaoding.editor.image.demo.model

/**
 * 稿定配置，包含ak、sk、thirdCateId和thirdParty等
 * 对应配置文件：GDImageEditorSDKConfiguration.json
 * Created by kongran on 2023/1/12
 * E-Mail Address：kongran@gaoding.com
 */
data class GDConfigBean(
    val thirdParty: String,
    val thirdCateId: String,
    val ak: String,
    val sk: String
)
