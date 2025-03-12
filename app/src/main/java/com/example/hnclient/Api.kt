package com.example.hnclient

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ApiItem

@Serializable
@SerialName("story")
class Story(
    val by: String,
    val descendants: Int,
    val id: String,
    val kids: List<String>,
    val score: Int,
    val time: Int,
    val title: String,
    val url: String? = null,
) : ApiItem

@Serializable
@SerialName("comment")
class Comment(
    val by: String,
    val id: String,
    val kids: List<String>,
    val parent: String,
    val text: String,
    val time: Int,
) : ApiItem
