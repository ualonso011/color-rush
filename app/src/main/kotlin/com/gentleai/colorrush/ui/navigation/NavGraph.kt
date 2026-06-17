package com.gentleai.colorrush.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
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
 * Root composable that sets up the [NavHost] with all three screens
 * and animated transitions between them.
 *
 * Transitions:
 * - Main → Game : slide in from right
 * - Game → GameOver : fade + scale
 * - GameOver → Main : slide out to left / fade
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
        composable(
            route = Routes.MAIN,
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200))
            },
        ) {
            MainScreen(
                onPlayClick = {
                    navController.navigate(Routes.GAME)
                },
            )
        }

        // ── Game screen ───────────────────────────────────────────────────
        composable(
            route = Routes.GAME,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(350),
                    initialOffsetX = { it },
                ) + fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(350),
                    targetOffsetX = { -it },
                ) + fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(350),
                    initialOffsetX = { -it },
                ) + fadeIn(animationSpec = tween(200))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(350),
                    targetOffsetX = { it },
                ) + fadeOut(animationSpec = tween(200))
            },
        ) {
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
            enterTransition = {
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(300),
                ) + fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(200),
                ) + fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200))
            },
        ) {
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
