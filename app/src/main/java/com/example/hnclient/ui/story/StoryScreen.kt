package com.example.hnclient.ui.story

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
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
class StoryScreenRoute(val id: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryScreen(id: String, back: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val viewModel = viewModel<StoryViewModel> {
        val database = Database(AndroidSqliteDriver(Database.Schema, context, "app.db"))
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
        StoryViewModel(
            HnRepository(
                HnRemote(httpClient),
                StoriesDao(database.storiesQueries, Json),
            )
        )
    }

    LaunchedEffect(id) {
        viewModel.loadStory(id)
    }

    val storyState by viewModel.storyState.collectAsState()

    when (val storyState = storyState) {
        is StoryState.Loading -> {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }

        is StoryState.Loaded -> {
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = { StoryTopBar(storyState.story, back, scrollBehavior) }
            ) { paddingValues ->
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(storyState.comments.size) { index ->
                        val comment = storyState.comments[index]
                        CommentItem(comment) { viewModel.onShowChildrenTap(comment.comment.id) }
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryTopBar(story: Story, back: () -> Unit, scrollBehavior: TopAppBarScrollBehavior) {
    val context = LocalContext.current
    LargeTopAppBar(
        navigationIcon = {
            IconButton(onClick = back) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        title = {
            Text(
                story.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = if (story.url != null) {
                    Modifier.clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(story.url)))
                    }
                } else {
                    Modifier
                },
            )
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun CommentItem(comment: CommentsListItem, onDropDownTap: () -> Unit) {
    val formattedTime = remember(comment.comment.time) { formatTime(comment.comment.time) }
    ListItem(
        modifier = Modifier.padding(start = (8 * comment.nestingLevel).dp),
        leadingContent = {
            Box(Modifier.width(24.dp)) {
                when (comment.state) {
                    CommentState.Collapsed -> {
                        IconButton(onClick = { onDropDownTap() }) {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                modifier = Modifier.rotate(-90f),
                                contentDescription = null,
                            )
                        }
                    }

                    CommentState.Expanded -> {
                        IconButton(onClick = { onDropDownTap() }) {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                            )
                        }
                    }

                    CommentState.Expanding -> {
                        CircularProgressIndicator()
                    }

                    CommentState.WithoutChildren -> Unit
                }
            }
        },
        headlineContent = {
            SelectionContainer {
                Text(AnnotatedString.fromHtml(comment.comment.text), fontSize = 12.sp)
            }
        },
        supportingContent = { Text("${comment.comment.by} $formattedTime", fontSize = 10.sp) }
    )
}
