package com.example.hnclient.ui.story

import com.example.hnclient.data.Comment
import com.example.hnclient.data.Story

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
