package com.gentleai.colorrush.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
 * Current routes:
 * - [MAIN] → [MainScreen]
 * - [GAME] → Placeholder (will be replaced by [GameScreen] in Phase 5)
 * - [GAME_OVER/{score}] → [GameOverScreen] (gets score via SavedStateHandle)
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

        // ── Game screen (placeholder until Phase 5) ──────────────────────
        composable(Routes.GAME) {
            GamePlaceholderScreen(
                onSimulateGameOver = { score ->
                    navController.navigate(Routes.gameOver(score))
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

/**
 * Placeholder game screen shown until the real [GameScreen] is implemented
 * in Phase 5. Allows testing the full navigation flow by simulating a game-over.
 */
@Composable
private fun GamePlaceholderScreen(
    onSimulateGameOver: (Int) -> Unit,
    onBackToMain: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(
                com.gentleai.colorrush.R.string.game_placeholder
            ),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { onSimulateGameOver(25) }) {
            Text(
                text = androidx.compose.ui.res.stringResource(
                    com.gentleai.colorrush.R.string.simulate_game_over
                ),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackToMain) {
            Text(
                text = androidx.compose.ui.res.stringResource(
                    com.gentleai.colorrush.R.string.main_menu
                ),
            )
        }
    }
}
