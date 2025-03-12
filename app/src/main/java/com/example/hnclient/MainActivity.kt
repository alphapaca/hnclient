package com.example.hnclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            HnclientTheme {
                RootScreen()
            }
        }
    }
}

@Composable
fun RootScreen() {
    Scaffold { innerPaddings ->
        val navController = rememberNavController()
        NavHost(navController, NewsScreenRoute, Modifier.padding(innerPaddings)) {
            composable<NewsScreenRoute> { NewsScreen { id -> navController.navigate(PostScreenRoute(id)) } }
            composable<PostScreenRoute> { backStackEntry ->
                PostScreen(backStackEntry.toRoute<PostScreenRoute>().id)
            }
        }
    }
}
