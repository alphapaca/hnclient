package com.example.hnclient.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hnclient.data.ApiItem
import com.example.hnclient.data.StoriesDao
import com.example.hnclient.data.Story
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class NewsViewModel(
    private val storiesDao: StoriesDao,
) : ViewModel() {
    val newsListState = MutableStateFlow(NewsListState(isLoading = true, emptyList()))

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    init {
        viewModelScope.launch {
            storiesDao.getAllStories().collect { stories ->
                newsListState.value = newsListState.value.copy(stories = stories)
            }
        }
        reloadNews()
    }

    fun reloadNews() {
        viewModelScope.launch(Dispatchers.Default) {
            newsListState.value = newsListState.value.copy(isLoading = true)
            val storiesFromNetwork = getNewsFromNetwork()
            storiesDao.clearAndInsert(storiesFromNetwork)
            newsListState.value = newsListState.value.copy(isLoading = false)
        }
    }

    private suspend fun getNewsFromNetwork(): List<Story> {
        return withContext(Dispatchers.IO) {
            val response = client.get("https://hacker-news.firebaseio.com/v0/topstories.json")
            response.body<List<Int>>()
                .map { id ->
                    async {
                        client.get("https://hacker-news.firebaseio.com/v0/item/$id.json")
                            .body<ApiItem>()
                    }
                }
                .take(30)
                .awaitAll()
                .filterIsInstance<Story>()
        }
    }
}
