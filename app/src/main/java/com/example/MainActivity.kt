package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.GameMode
import com.example.model.AIDifficulty
import com.example.ui.screens.*
import com.example.ui.theme.LudoMasterTheme
import com.example.viewmodel.LudoViewModel

class MainActivity : ComponentActivity() {
    private val ludoViewModel: LudoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LudoMasterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onStartGame = { mode, playerCount ->
                                    navController.navigate("setup/${mode.name}")
                                },
                                onNavigateOnline = { navController.navigate("online") },
                                onNavigateStats = { navController.navigate("stats") },
                                onNavigateRules = { navController.navigate("rules") },
                                onNavigateProfile = { navController.navigate("profile") }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                viewModel = ludoViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "setup/{mode}",
                            arguments = listOf(navArgument("mode") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val modeStr = backStackEntry.arguments?.getString("mode") ?: GameMode.PASS_AND_PLAY.name
                            val mode = try { GameMode.valueOf(modeStr) } catch (e: Exception) { GameMode.PASS_AND_PLAY }
                            SetupScreen(
                                mode = mode,
                                onStartGame = { selectedMode, playerCount, aiDiff ->
                                    ludoViewModel.startNewGame(selectedMode, playerCount, aiDiff)
                                    navController.navigate("game")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("game") {
                            GameScreen(
                                viewModel = ludoViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("online") {
                            OnlineLobbyScreen(
                                onStartOnlineGame = { mode, playerCount ->
                                    ludoViewModel.startNewGame(mode, playerCount)
                                    navController.navigate("game")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("stats") {
                            StatsScreen(
                                viewModel = ludoViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("rules") {
                            RulesScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
