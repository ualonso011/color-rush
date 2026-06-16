package com.gentleai.colorrush.ui.gameover

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Game-over screen composable.
 *
 * Displays the final score with a counting animation, an arcade-style name
 * input field, a Save button, and navigation buttons.
 *
 * @param onPlayAgain Callback to start a new game.
 * @param onMainMenu  Callback to return to the main menu.
 */
@Composable
fun GameOverScreen(
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameOverViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Animate score counting up
    val animatedScore by animateIntAsState(
        targetValue = state.finalScore,
        animationSpec = tween(durationMillis = 1500),
        label = "scoreAnimation",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // ── Game Over title ──────────────────────────────────────────────
        Text(
            text = androidx.compose.ui.res.stringResource(
                com.gentleai.colorrush.R.string.game_over_title
            ),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Score display (animated) ────────────────────────────────────
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.final_score,
                        animatedScore,
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Name input ──────────────────────────────────────────────────
        OutlinedTextField(
            value = state.playerName,
            onValueChange = { viewModel.onNameChanged(it) },
            label = {
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.name_hint
                    ),
                )
            },
            placeholder = {
                Text(
                    text = "ARCADE",
                    fontFamily = FontFamily.Monospace,
                )
            },
            singleLine = true,
            maxLines = 1,
            enabled = !state.isSaved,
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() },
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            supportingText = {
                Text(
                    text = "${state.playerName.length}/10",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Save button ─────────────────────────────────────────────────
        Button(
            onClick = {
                keyboardController?.hide()
                viewModel.saveScore()
            },
            enabled = state.playerName.trim().isNotEmpty() && !state.isSaving && !state.isSaved,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isSaved) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                },
            ),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else if (state.isSaved) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.save_button
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.save_button
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Navigation buttons ──────────────────────────────────────────
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Replay,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.padding(start = 8.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(
                    com.gentleai.colorrush.R.string.play_again
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onMainMenu,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.padding(start = 8.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(
                    com.gentleai.colorrush.R.string.main_menu
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
