package com.gaoding.editor.image.demo.bean

/**
 * OnMessageModel
 * Created by kongran on 2023/2/6
 * E-Mail Address：kongran@gaoding.com
 */
data class OnMessageInputModel(
    val type: String,
    val data: Map<String, String>?
) : java.io.Serializable
