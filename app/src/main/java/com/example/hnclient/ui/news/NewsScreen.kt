package com.example.hnclient.ui.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.hnclient.Database
import com.example.hnclient.R
import com.example.hnclient.data.HnRemote
import com.example.hnclient.data.HnRepository
import com.example.hnclient.data.StoriesDao
import com.example.hnclient.data.Story
import com.example.hnclient.formatTime
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data object NewsScreenRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(openPost: (String) -> Unit) {
    val context = LocalContext.current.applicationContext
    val viewModel = viewModel<NewsViewModel> {
        val database = Database(AndroidSqliteDriver(Database.Schema, context, "app.db"))
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
        NewsViewModel(
            HnRepository(
                HnRemote(httpClient),
                StoriesDao(database.storiesQueries, Json),
            )
        )
    }
    val newsListState by viewModel.newsListState.collectAsState()
    PullToRefreshBox(
        isRefreshing = newsListState.isLoading,
        onRefresh = { viewModel.reloadNews() },
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(newsListState.stories.size) { index ->
                NewsItem(newsListState.stories[index], openPost)
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

