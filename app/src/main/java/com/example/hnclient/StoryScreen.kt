package com.example.hnclient

import android.content.Intent
import android.net.Uri
import android.text.Selection
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.MediumTopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
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
class StoryScreenRoute(val id: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryScreen(id: String, back: () -> Unit) {
    val viewModel = viewModel<StoryViewModel>()

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
    MediumTopAppBar(
        navigationIcon = {
            IconButton(onClick = back) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        title = {
            Text(
                story.title,
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

class StoryViewModel : ViewModel() {
    val storyState = MutableStateFlow<StoryState>(StoryState.Loading)

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    fun loadStory(id: String) {
        storyState.value = StoryState.Loading
        viewModelScope.launch {
            val story = client.get("https://hacker-news.firebaseio.com/v0/item/$id.json")
                .body<Story>()
            val comments = story.kids
                .map { kidId ->
                    async {
                        val comment =
                            client.get("https://hacker-news.firebaseio.com/v0/item/$kidId.json")
                                .body<Comment>()
                        CommentsListItem(
                            comment = comment,
                            state = if (comment.kids.isEmpty()) CommentState.WithoutChildren else CommentState.Collapsed,
                            nestingLevel = 0,
                        )
                    }
                }
                .awaitAll()
                .filterNot { comment -> comment.comment.text == "" || comment.comment.by == "" }
            storyState.value = StoryState.Loaded(story, comments)
        }
    }

    fun onShowChildrenTap(id: String) {
        when (val state = storyState.value) {
            is StoryState.Loading -> Unit
            is StoryState.Loaded -> {
                val targetComment = state.comments.first { comment -> comment.comment.id == id }
                when (targetComment.state) {
                    CommentState.WithoutChildren, CommentState.Expanding -> Unit
                    CommentState.Expanded -> {
                        storyState.value = state.copy(
                            comments = state.comments
                                .filter { comment -> comment.comment.parent != id }
                                .map { comment ->
                                    if (comment.comment.id == id) {
                                        comment.copy(state = CommentState.Collapsed)
                                    } else {
                                        comment
                                    }
                                }
                        )
                    }

                    CommentState.Collapsed -> expandComment(targetComment, state)
                }
            }
        }
    }

    private fun expandComment(comment: CommentsListItem, state: StoryState.Loaded) {
        val index = state.comments.indexOf(comment)
        viewModelScope.launch {
            val childComments = comment.comment.kids
                .map { kidId ->
                    async {
                        val childComment =
                            client.get("https://hacker-news.firebaseio.com/v0/item/$kidId.json")
                                .body<Comment>()
                        CommentsListItem(
                            comment = childComment,
                            state = if (childComment.kids.isEmpty()) CommentState.WithoutChildren else CommentState.Collapsed,
                            nestingLevel = comment.nestingLevel + 1,
                        )
                    }
                }
                .awaitAll()
                .filterNot { comment -> comment.comment.text == "" || comment.comment.by == "" }
            storyState.value = state.copy(
                comments = buildList {
                    addAll(state.comments.subList(0, index))
                    add(comment.copy(state = CommentState.Expanded))
                    addAll(childComments)
                    addAll(state.comments.subList(index + 1, state.comments.size))
                },
            )
        }
    }
}

sealed interface StoryState {
    data object Loading : StoryState
    data class Loaded(
        val story: Story,
        val comments: List<CommentsListItem>,
    ) : StoryState
}

data class CommentsListItem(
    val comment: Comment,
    val state: CommentState,
    val nestingLevel: Int,
)

enum class CommentState {
    Expanded, Expanding, Collapsed, WithoutChildren
}

// Сделать экран который отображает пост с id из прошлого экрана и ниже - комментарии к нему.
// Все данные брать из https://hacker-news.firebaseio.com/v0/item/%%yourItemId%%.json
// Комментарии могут раскрываться если у них непустой kids.
// Раскрывать комментарии можно и те что внутри уже раскрытых.
// В момент загрузки экрана в начале и при раскрытии коммента
// должен крутиться CircullarProgressIndicator.
// Хинты:
// - Сам пост - тоже элемент списка, просто отдельный в начале
// - При раскрытии проще всего добавлять обычные элементы списка,
//   у которых отступом выделена вложенность
