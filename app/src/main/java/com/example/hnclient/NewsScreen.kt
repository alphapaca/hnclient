package com.example.hnclient

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data object NewsScreenRoute

@Composable
fun NewsScreen(openPost: (String) -> Unit) {
    val viewModel = viewModel<NewsViewModel>()
    val newsListState by viewModel.newsListState.collectAsState()
    when (val newsListState = newsListState) {
        is NewsListState.Progress -> {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }

        is NewsListState.Loaded -> {
            LazyColumn(Modifier.fillMaxSize()) {
                items(newsListState.stories.size) { index ->
                    NewsItem(newsListState.stories[index], openPost)
                }
            }
        }
    }
}

@Composable
fun NewsItem(story: Story, openPost: (String) -> Unit) {
    val context = LocalContext.current
    val formattedTime = remember(story.time) { formatTime(story.time) }
    ListItem(
        modifier = if (story.url != null) {
            Modifier.clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(story.url)))
            }
        } else Modifier,
        headlineContent = { Text(story.title) },
        supportingContent = {
            Text("${story.score} points by ${story.by} $formattedTime")
        },
        trailingContent = {
            IconButton(onClick = { openPost(story.id) }) {
                Icon(
                    painterResource(R.drawable.ic_chat_bubble_outline_24),
                    contentDescription = null,
                )
                Text(
                    story.descendants.toString(),
                    fontSize = 8.sp,
                )
            }
        },
    )
}

class NewsViewModel : ViewModel() {
    val newsListState = MutableStateFlow<NewsListState>(NewsListState.Progress)

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
            val response = client.get("https://hacker-news.firebaseio.com/v0/topstories.json")
            val newsList = response.body<List<Int>>()
                .map { id ->
                    async {
                        client.get("https://hacker-news.firebaseio.com/v0/item/$id.json")
                            .body<ApiItem>()
                    }
                }
                .take(30)
                .awaitAll()
                .filterIsInstance<Story>()
            newsListState.value = NewsListState.Loaded(newsList)
        }
    }
}

sealed interface NewsListState {
    data object Progress : NewsListState
    data class Loaded(val stories: List<Story>) : NewsListState
}

// Сделать экран списка новостей:
// использует https://hacker-news.firebaseio.com/v0/topstories.json для взятия всех id
// и после этого для каждого id берет item при помощи
// https://hacker-news.firebaseio.com/v0/item/%%yourItemId%%.json и десериализует его при
// помощи ApiItem.
// Вся загрузка из сети внутри ViewModel у которой есть поле state: StateFlow,
// на которое подписывается Screen и отображает его. В идеале сделать чтобы
// итемы грузились параллельно.
// В момент загрузки экрана в начале должен крутиться CircullarProgressIndicator.
// Хинты:
// 1. Для подписки можно использовать collectAsState
// 2. Для того чтобы дождаться сразу несколько async можно использовать экстеншн awaitAll()
