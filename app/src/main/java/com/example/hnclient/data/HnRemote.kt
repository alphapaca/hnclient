package com.example.hnclient.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HnRemote(
    private val client: HttpClient,
) {
    suspend fun getTopStories(): List<String> {
        return withContext(Dispatchers.IO) {
            client.get("https://hacker-news.firebaseio.com/v0/topstories.json").body()
        }
    }

    suspend fun getItem(id: String): ApiItem {
        return  withContext(Dispatchers.IO) {
            client.get("https://hacker-news.firebaseio.com/v0/item/$id.json").body()
        }
    }
}