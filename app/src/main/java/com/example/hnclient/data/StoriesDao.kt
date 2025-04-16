package com.example.hnclient.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.hnclient.StoriesQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class StoriesDao(
    private val storiesQueries: StoriesQueries,
    private val json: Json,
) {
    fun getAllStories(): Flow<List<Story>> {
        return storiesQueries
            .getStories { by, descendants, id, kidsString, score, time, title, url ->
                val kids = json.decodeFromString<List<String>>(kidsString)
                Story(by, descendants, id, kids, score, time, title, url)
            }
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun clearAndInsert(stories: List<Story>) {
        withContext(Dispatchers.IO) {
            storiesQueries.transaction {
                storiesQueries.clearStories()
                stories.forEach { story ->
                    with(story) {
                        val kidsString = json.encodeToString(kids)
                        storiesQueries.insert(by, descendants, id, kidsString, score, time, title, url)
                    }
                }
            }
        }
    }
}
