package com.example.hnclient.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class HnRepository(
    private val remote: HnRemote,
    private val storiesDao: StoriesDao
) {
    fun subscribeToTopStories(): Flow<List<Story>> {
        return storiesDao.getAllStories()
    }

    suspend fun reloadTopStories(maxCount: Int) {
        val ids = remote.getTopStories()
        val stories = ids
            .take(maxCount)
            .map { id ->
                coroutineScope { async { remote.getItem(id) } }
            }
            .awaitAll()
            .filterIsInstance<Story>()
        storiesDao.clearAndInsert(stories)
    }

    suspend fun getStory(id: String): Story {
        return remote.getItem(id) as Story
    }

    suspend fun getComment(id: String): Comment {
        return remote.getItem(id) as Comment
    }
}