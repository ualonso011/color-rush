package com.gentleai.colorrush.ui.gameover

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gentleai.colorrush.ui.theme.NeonCyan
import com.gentleai.colorrush.ui.theme.NeonMagenta
import com.gentleai.colorrush.ui.theme.NeonRed
import com.gentleai.colorrush.ui.theme.NeonYellow

/**
 * Game-over screen composable with dramatic arcade styling.
 *
 * Displays:
 * - "GAME OVER" title with neon glow
 * - Final score with animated counting and gradient neon border
 * - Neon-styled name input field
 * - Save button with glow effects
 * - Play Again and Main Menu neon buttons
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

    // ── Title glow animation ──────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "gameOverGlow")
    val titleGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "titlePulse",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF0D1117),
                        Color(0xFF0A0E27),
                    ),
                ),
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // ── Game Over title with neon glow ────────────────────────────────
        Box(contentAlignment = Alignment.Center) {
            // Glow layer
            Text(
                text = androidx.compose.ui.res.stringResource(
                    com.gentleai.colorrush.R.string.game_over_title
                ),
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = NeonRed.copy(alpha = titleGlowAlpha * 0.3f),
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
            )
            // Foreground
            Text(
                text = androidx.compose.ui.res.stringResource(
                    com.gentleai.colorrush.R.string.game_over_title
                ),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = NeonRed,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Score display with gradient neon border ──────────────────────
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0D1117),
            ),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .drawBehind {
                    // Gradient neon border
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                NeonCyan,
                                NeonMagenta,
                                NeonYellow,
                                NeonCyan,
                            ),
                        ),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = Stroke(width = 2.5f * density),
                        size = size,
                    )
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.final_score_label
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 3.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$animatedScore",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonYellow,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Name input with neon border ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .drawBehind {
                    drawRoundRect(
                        color = if (state.isSaved) NeonCyan.copy(alpha = 0.5f)
                        else NeonMagenta.copy(alpha = 0.5f),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 1.5f * density),
                        size = size,
                    )
                },
        ) {
            OutlinedTextField(
                value = state.playerName,
                onValueChange = { viewModel.onNameChanged(it) },
                label = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(
                            com.gentleai.colorrush.R.string.name_hint
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                placeholder = { Text("") },
                singleLine = true,
                maxLines = 1,
                enabled = !state.isSaved,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = NeonCyan,
                    focusedLabelColor = NeonCyan,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                supportingText = {
                    Text(
                        text = "${state.playerName.length}/10",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Save button (neon styled) ────────────────────────────────────
        val canSave = state.playerName.trim().isNotEmpty() && !state.isSaving && !state.isSaved
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (state.isSaved) {
                            listOf(NeonCyan, NeonCyan.copy(alpha = 0.7f))
                        } else {
                            listOf(NeonMagenta, NeonMagenta.copy(alpha = 0.7f))
                        }
                    ),
                )
                .drawBehind {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.15f),
                        cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                        size = size.copy(height = size.height / 2),
                    )
                }
                .then(
                    if (canSave) {
                        Modifier.clickable {
                            keyboardController?.hide()
                            viewModel.saveScore()
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isSaving -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF0A0E27),
                        strokeWidth = 2.dp,
                    )
                }
                state.isSaved -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF0A0E27),
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(
                                com.gentleai.colorrush.R.string.save_button
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0A0E27),
                        )
                    }
                }
                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            tint = Color(0xFF0A0E27),
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(
                                com.gentleai.colorrush.R.string.save_button
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0A0E27),
                        )
                    }
                }
            }
        }

        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error!!,
                style = MaterialTheme.typography.bodySmall,
                color = NeonRed,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Play Again button (neon styled) ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(NeonCyan, NeonCyan.copy(alpha = 0.7f)),
                    ),
                )
                .drawBehind {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.15f),
                        cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                        size = size.copy(height = size.height / 2),
                    )
                }
                .clickable(enabled = true, onClick = onPlayAgain),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Replay,
                    contentDescription = null,
                    tint = Color(0xFF0A0E27),
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.play_again
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A0E27),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Main Menu button (outlined neon style) ──────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF1A1F3A))
                .drawBehind {
                    drawRoundRect(
                        color = NeonCyan.copy(alpha = 0.5f),
                        cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                        style = Stroke(width = 1.5f * density),
                        size = size,
                    )
                }
                .clickable(enabled = true, onClick = onMainMenu),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.main_menu
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
