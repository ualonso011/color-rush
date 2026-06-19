package com.gentleai.colorrush.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gentleai.colorrush.BuildConfig
import com.gentleai.colorrush.domain.model.RankingEntry
import com.gentleai.colorrush.ui.theme.MedalBronze
import com.gentleai.colorrush.ui.theme.MedalGold
import com.gentleai.colorrush.ui.theme.MedalSilver
import com.gentleai.colorrush.ui.theme.NeonCyan
import com.gentleai.colorrush.ui.theme.NeonMagenta
import com.gentleai.colorrush.ui.theme.NeonYellow

/**
 * Main menu screen composable with arcade styling.
 *
 * Displays:
 * - "COLORRUSH" title with neon glow
 * - Language selector with neon-styled buttons
 * - Massive Play button with pulsing neon border
 * - App version label
 * - Top 10 ranking list with medal badges
 *
 * @param onPlayClick Callback invoked when the Play button is tapped.
 */
@Composable
fun MainScreen(
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // ── Title glow animation ──────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "titleGlow")
    val titleGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "titleGlow",
    )

    // ── Play button pulse ─────────────────────────────────────────────────
    val playPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "playPulse",
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

        // ── Language selector ─────────────────────────────────────────────
        LanguageSelector(
            currentLanguage = state.currentLanguage,
            onLanguageSelected = { viewModel.setLanguage(it) },
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // ── Title "COLORRUSH" with neon glow ──────────────────────────────
        // Build annotated string with larger C and first R (used for both layers)
        val titleText = buildAnnotatedString {
            withStyle(SpanStyle(fontSize = 68.sp)) { append("C") }
            withStyle(SpanStyle(fontSize = 48.sp)) { append("OLOR") }
            withStyle(SpanStyle(fontSize = 68.sp)) { append("R") }
            withStyle(SpanStyle(fontSize = 48.sp)) { append("USH") }
        }
        val titleLetterSpacing = 8.sp

        Box(contentAlignment = Alignment.Center) {
            // Glow/shadow layer — identical metrics to foreground
            Text(
                text = titleText,
                fontWeight = FontWeight.Black,
                color = NeonCyan.copy(alpha = titleGlowAlpha * 0.4f),
                letterSpacing = titleLetterSpacing,
                textAlign = TextAlign.Center,
            )
            // Foreground
            Text(
                text = titleText,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                letterSpacing = titleLetterSpacing,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = androidx.compose.ui.res.stringResource(
                com.gentleai.colorrush.R.string.tap_color_beat_clock
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(0.8f))

        // ── Play button (massive, with neon border and glow) ──────────────
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(72.dp)
                .drawBehind {
                    // Outer glow
                    drawRoundRect(
                        color = NeonCyan.copy(alpha = playPulse * 0.25f),
                        cornerRadius = CornerRadius(36.dp.toPx(), 36.dp.toPx()),
                        style = Stroke(width = 8f * density),
                        size = size,
                    )
                    // Border glow
                    drawRoundRect(
                        color = NeonCyan.copy(alpha = playPulse * 0.6f),
                        cornerRadius = CornerRadius(36.dp.toPx(), 36.dp.toPx()),
                        style = Stroke(width = 2f * density),
                        size = size,
                    )
                }
                .clip(RoundedCornerShape(36.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonCyan,
                            NeonCyan.copy(alpha = 0.8f),
                        ),
                    ),
                )
                .clickable(enabled = true, onClick = onPlayClick),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF0A0E27),
                    modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.play
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0A0E27),
                    letterSpacing = 2.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Version label ─────────────────────────────────────────────────
        Text(
            text = androidx.compose.ui.res.stringResource(
                com.gentleai.colorrush.R.string.version,
                BuildConfig.VERSION_NAME,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Rankings card ─────────────────────────────────────────────────
        RankingsSection(
            rankings = state.rankings,
            isLoading = state.isLoading,
            onRefresh = { viewModel.loadRankings() },
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Language selector row with three neon-styled selectable buttons.
 */
@Composable
private fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
) {
    val languages = listOf(
        "eu" to androidx.compose.ui.res.stringResource(com.gentleai.colorrush.R.string.language_eu),
        "es" to androidx.compose.ui.res.stringResource(com.gentleai.colorrush.R.string.language_es),
        "en" to androidx.compose.ui.res.stringResource(com.gentleai.colorrush.R.string.language_en),
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        languages.forEach { (code, label) ->
            val isSelected = currentLanguage == code
            val bgColor = if (isSelected) NeonMagenta else Color(0xFF1A1F3A)
            val borderColor = if (isSelected) NeonMagenta else Color(0xFF2A2F4A)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .drawBehind {
                        drawRoundRect(
                            color = borderColor.copy(alpha = 0.5f),
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                            style = Stroke(width = 1.5f * density),
                            size = size,
                        )
                    }
                    .clickable(enabled = true, onClick = { onLanguageSelected(code) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF0A0E27) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Displays the top 10 rankings in a neon-styled card with medal badges.
 */
@Composable
private fun RankingsSection(
    rankings: List<RankingEntry>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1117),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2F4A)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = NeonYellow,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.ranking_title
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Content ───────────────────────────────────────────────────
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = NeonCyan,
                    )
                }
            }

            AnimatedVisibility(
                visible = !isLoading && rankings.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(
                            com.gentleai.colorrush.R.string.no_rankings
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AnimatedVisibility(
                visible = !isLoading && rankings.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    itemsIndexed(rankings) { index, entry ->
                        RankingRow(index = index + 1, entry = entry)
                    }
                }
            }
        }
    }
}

/**
 * Single ranking row with position medal/indicator, player name, and score.
 *
 * Top 3 get gold/silver/bronze medals. Lower positions show a numbered badge.
 */
@Composable
private fun RankingRow(index: Int, entry: RankingEntry) {
    val isTop3 = index <= 3

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .background(
                color = if (isTop3) Color(0x0CFFFFFF) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Position badge ────────────────────────────────────────────────
        val badgeColor = when (index) {
            1 -> MedalGold
            2 -> MedalSilver
            3 -> MedalBronze
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Box(
            modifier = Modifier
                .size(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isTop3) {
                // Medal circle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .drawBehind {
                            drawCircle(
                                color = badgeColor.copy(alpha = 0.2f),
                            )
                            drawCircle(
                                color = badgeColor,
                                style = Stroke(width = 1.5f * density),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = badgeColor,
                    )
                }
            } else {
                Text(
                    text = "$index.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // ── Player name ──────────────────────────────────────────────────
        Text(
            text = entry.playerName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = if (isTop3) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.width(6.dp))

        // ── Score ─────────────────────────────────────────────────────────
        Text(
            text = "${entry.score}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (index == 1) NeonYellow
            else if (index == 2) MedalSilver
            else if (index == 3) MedalBronze
            else MaterialTheme.colorScheme.primary,
        )
    }
}
