package com.example.hnclient.ui.news

import com.example.hnclient.data.Story

data class NewsListState(
    val isLoading: Boolean,
    val stories: List<Story>,
)
