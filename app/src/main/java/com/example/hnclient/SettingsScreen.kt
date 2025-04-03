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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object SettingsScreenRoute

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val themeKind by settingsViewModel.themeKindState.collectAsState(ThemeKind.System)
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

class SettingsViewModel(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {
    private val themeKindKey = stringPreferencesKey("themeKind")
    val themeKindState: Flow<ThemeKind> = dataStore.data
        .map { preferences -> preferences[themeKindKey]?.let(ThemeKind::valueOf) ?: ThemeKind.System }

    fun updateTheme(newTheme: ThemeKind) {
        viewModelScope.launch {
            dataStore.edit { mutablePreferences ->
                mutablePreferences[themeKindKey] = newTheme.name
            }
        }
    }
}

enum class ThemeKind(val title: String) {
    Light("Светлая"), Dark("Темная"), System("Системная")
}
