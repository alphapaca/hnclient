package com.example.hnclient.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ApiItem

@Serializable
@SerialName("story")
class Story(
    val by: String,
    val descendants: Long,
    val id: String,
    val kids: List<String> = emptyList(),
    val score: Long,
    val time: Long,
    val title: String,
    val url: String? = null,
) : ApiItem

@Serializable
@SerialName("comment")
class Comment(
    val by: String = "",
    val id: String,
    val kids: List<String> = emptyList(),
    val parent: String,
    val text: String = "",
    val time: Long,
) : ApiItem

@Serializable
@SerialName("job")
class Job : ApiItem
