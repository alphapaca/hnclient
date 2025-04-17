package com.example.hnclient.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hnclient.data.HnRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(
    private val hnRepository: HnRepository,
) : ViewModel() {
    val newsListState = MutableStateFlow(NewsListState(isLoading = true, emptyList()))

    init {
        viewModelScope.launch {
            hnRepository.subscribeToTopStories().collect { stories ->
                newsListState.value = newsListState.value.copy(stories = stories)
            }
        }
        reloadNews()
    }

    fun reloadNews() {
        viewModelScope.launch(Dispatchers.Default) {
            newsListState.value = newsListState.value.copy(isLoading = true)
            hnRepository.reloadTopStories(maxCount = 30)
            newsListState.value = newsListState.value.copy(isLoading = false)
        }
    }
}
