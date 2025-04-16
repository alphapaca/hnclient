package com.example.hnclient.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
