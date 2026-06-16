package com.gentleai.colorrush.ui.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gentleai.colorrush.ui.game.GameScreen
import com.gentleai.colorrush.ui.gameover.GameOverScreen
import com.gentleai.colorrush.ui.main.MainScreen

/**
 * Top-level navigation route constants.
 */
object Routes {
    const val MAIN = "main"
    const val GAME = "game"
    const val GAME_OVER = "game_over/{score}"

    /** Builds a concrete [GAME_OVER] route with the given [score]. */
    fun gameOver(score: Int) = "game_over/$score"
}

/**
 * Root composable that sets up the [NavHost] with all three screens.
 *
 * Routes:
 * - [MAIN] → [MainScreen] (language selector, play button, rankings)
 * - [GAME] → [GameScreen] (3×3 grid, timer, score display)
 * - [GAME_OVER/{score}] → [GameOverScreen] (name entry, score persistence)
 *
 * @param navController The [NavHostController] driving navigation.
 */
@Composable
fun ColorRushNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN,
    ) {
        // ── Main screen ──────────────────────────────────────────────────
        composable(Routes.MAIN) {
            MainScreen(
                onPlayClick = {
                    navController.navigate(Routes.GAME)
                },
            )
        }

        // ── Game screen ───────────────────────────────────────────────────
        composable(Routes.GAME) {
            GameScreen(
                onGameOver = { score ->
                    navController.navigate(Routes.gameOver(score)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
                onBackToMain = {
                    navController.popBackStack(Routes.MAIN, inclusive = false)
                },
            )
        }

        // ── Game-over screen ─────────────────────────────────────────────
        composable(
            route = Routes.GAME_OVER,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
            ),
        ) {
            // GameOverScreen uses hiltViewModel() internally, which reads
            // "score" from SavedStateHandle (auto-populated from nav args).
            GameOverScreen(
                onPlayAgain = {
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.MAIN)
                    }
                },
                onMainMenu = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
            )
        }
    }
}
