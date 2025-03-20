package com.example.hnclient

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

@Serializable
data object SettingsScreenRoute

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val themeKind by settingsViewModel.themeKindState.collectAsState()
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = { Text("Тема") },
        trailingContent = {
            var expanded by remember { mutableStateOf(false) }
            Button(onClick = { expanded = !expanded }) {
                Text(themeKind.title)
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                )
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                ThemeKind.entries.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.title, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            settingsViewModel.updateTheme(item)
                            expanded = false
                        },
                    )
                }
            }
        }
    )
}

class SettingsViewModel : ViewModel() {
    val themeKindState = MutableStateFlow(ThemeKind.System)

    fun updateTheme(newTheme: ThemeKind) {
        themeKindState.value = newTheme
    }
}

enum class ThemeKind(val title: String) {
    Light("Светлая"), Dark("Темная"), System("Системная")
}
