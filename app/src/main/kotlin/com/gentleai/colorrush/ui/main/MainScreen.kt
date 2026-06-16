package com.gentleai.colorrush.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gentleai.colorrush.BuildConfig
import com.gentleai.colorrush.domain.model.RankingEntry

/**
 * Main menu screen composable.
 *
 * Displays:
 * - Language selector (Euskara / Español / English)
 * - Play button to start a new game
 * - App version label
 * - Top 10 ranking list
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // ── Language selector ─────────────────────────────────────────────
        LanguageSelector(
            currentLanguage = state.currentLanguage,
            onLanguageSelected = { viewModel.setLanguage(it) },
        )

        Spacer(modifier = Modifier.weight(1f))

        // ── Play button ───────────────────────────────────────────────────
        Button(
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(
                    com.gentleai.colorrush.R.string.play
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        // ── Version label ─────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(
                com.gentleai.colorrush.R.string.version,
                BuildConfig.VERSION_NAME,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Rankings card ─────────────────────────────────────────────────
        RankingsSection(
            rankings = state.rankings,
            isLoading = state.isLoading,
            onRefresh = { viewModel.loadRankings() },
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Language selector row with three [FilterChip]s for Euskara, Español, English.
 *
 * The currently active language is visually indicated by a filled chip style.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
            FilterChip(
                selected = currentLanguage == code,
                onClick = { onLanguageSelected(code) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Displays the top 10 rankings in a [Card] with a trophy header.
 *
 * Shows a loading indicator while data is being fetched, an empty-state
 * message when no rankings exist, or the ranked list otherwise.
 */
@Composable
private fun ColumnScope.RankingsSection(
    rankings: List<RankingEntry>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.gentleai.colorrush.R.string.ranking_title
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
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
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    itemsIndexed(rankings) { index, entry ->
                        RankingRow(index = index + 1, entry = entry)
                        if (index < rankings.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 0.5.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single ranking row with position number, player name, and score.
 *
 * The top 3 positions show a star icon instead of a plain number.
 */
@Composable
private fun RankingRow(index: Int, entry: RankingEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Position indicator
        if (index <= 3) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = when (index) {
                    1 -> MaterialTheme.colorScheme.primary
                    2 -> MaterialTheme.colorScheme.secondary
                    3 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp),
            )
        } else {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Player name
        Text(
            text = entry.playerName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Score
        Text(
            text = "${entry.score}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
