package com.example.hnclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.hnclient.ui.theme.HnclientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel = viewModel<SettingsViewModel>()
            val themeKind by settingsViewModel.themeKindState.collectAsState()
            HnclientTheme(darkTheme = when(themeKind) {
                ThemeKind.Light -> false
                ThemeKind.Dark -> true
                ThemeKind.System -> isSystemInDarkTheme()
            }) {
                RootScreen(settingsViewModel)
            }
        }
    }
}

@Composable
fun RootScreen(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf(Tabs.News) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                Tabs.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == currentTab,
                        onClick = {
                            currentTab = tab
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.title) }
                    )
                }
            }
        }
    ) { innerPaddings ->
        NavHost(navController, NewsScreenRoute, Modifier.padding(innerPaddings)) {
            composable<NewsScreenRoute> {
                NewsScreen { id ->
                    navController.navigate(
                        StoryScreenRoute(
                            id
                        )
                    )
                }
            }
            composable<StoryScreenRoute> { backStackEntry ->
                StoryScreen(
                    backStackEntry.toRoute<StoryScreenRoute>().id,
                    navController::popBackStack
                )
            }
            composable<SettingsScreenRoute> { SettingsScreen(settingsViewModel) }
        }
    }
}

enum class Tabs(
    val title: String,
    val icon: ImageVector,
    val route: Any,
) {
    News("Новости", Icons.Filled.Home, NewsScreenRoute),
    Settings("Настройки", Icons.Filled.Settings, SettingsScreenRoute)
}
