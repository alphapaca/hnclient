package com.example.hnclient.ui.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hnclient.data.Comment
import com.example.hnclient.data.HnRepository
import com.example.hnclient.data.StoriesDao
import com.example.hnclient.data.Story
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class StoryViewModel(
    private val repository: HnRepository,
) : ViewModel() {
    val storyState = MutableStateFlow<StoryState>(StoryState.Loading)

    fun loadStory(id: String) {
        viewModelScope.launch {
            val story = repository.getStory(id)
            val comments = story.kids
                .map { kidId ->
                    async {
                        val comment = repository.getComment(kidId)
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
                        val childComment = repository.getComment(kidId)
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
