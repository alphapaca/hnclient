package com.example.hnclient

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

@Serializable
data object NewsScreenRoute

@Composable
fun NewsScreen(openPost: (String) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("News")
            Spacer(Modifier.height(16.dp))
            Button({ openPost("42") }) {
                Text("Open post #42")
            }
        }
    }
}

// Сделать экран списка новостей:
// использует https://hacker-news.firebaseio.com/v0/topstories.json для взятия всех id
// и после этого для каждого id берет item при помощи
// https://hacker-news.firebaseio.com/v0/item/%%yourItemId%%.json и десериализует его при
// помощи ApiItem.
// Вся загрузка из сети внутри ViewModel у которой есть поле state: StateFlow,
// на которое подписывается Screen и отображает его. В идеале сделать чтобы
// итемы грузились параллельно.
// В момент загрузки экрана в начале должен крутиться CircullarProgressIndicator.
// Хинты:
// 1. Для подписки можно использовать collectAsState
// 2. Для того чтобы дождаться сразу несколько async можно использовать экстеншн awaitAll()
