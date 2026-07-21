package com.mewtype.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mewtype.player.data.model.CoverEntry
import com.mewtype.player.data.model.SongEntity
import com.mewtype.player.ui.screens.*
import com.mewtype.player.ui.theme.MewtypeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MewtypeTheme {
                MewtypeApp()
            }
        }
    }
}

@Composable
fun MewtypeApp(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    NavHost(navController, startDestination = "song_list") {
        composable("song_list") {
            SongListScreen(
                state = state,
                onPlaySong = { song ->
                    viewModel.playSong(song)
                    navController.navigate("player")
                },
                onPlayCover = { cover ->
                    viewModel.playCover(cover)
                    navController.navigate("player")
                },
                onNavigateToPlayer = { navController.navigate("player") },
                onTabChange = { viewModel.selectTab(it) },
                onSettings = { navController.navigate("settings") },
                onLogin = { navController.navigate("login") }
            )
        }
        composable("player") {
            PlayerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = { cookie ->
                    viewModel.setCookie(cookie)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
