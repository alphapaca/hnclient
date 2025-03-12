package com.example.hnclient

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

@Serializable
class PostScreenRoute(val id: String)

@Composable
fun PostScreen(id: String) {
    Box(Modifier.fillMaxSize()) {
        Text("Post #$id", Modifier.align(Alignment.Center))
    }
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
