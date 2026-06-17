package com.gentleai.colorrush.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════
// NEON ARCADE PALETTE — Dark-only, vibrant, glow-friendly
// ═══════════════════════════════════════════════════════════════

// ── Core brand ─────────────────────────────────────────────────
val NeonCyan = Color(0xFF00FFFF)
val NeonMagenta = Color(0xFFFF00FF)
val NeonYellow = Color(0xFFFFEB3B)
val NeonRed = Color(0xFFFF0055)

// ── Dark backgrounds ───────────────────────────────────────────
val BackgroundDark = Color(0xFF0A0E27)    // Deep space blue
val SurfaceDark = Color(0xFF0D1117)       // Dark blue-black
val SurfaceVariantDark = Color(0xFF161B33) // Slightly lighter panel

// ── On-colors (text / icons on neon backgrounds) ───────────────
val OnNeonCyan = Color(0xFF0A0E27)
val OnNeonMagenta = Color(0xFF0A0E27)
val OnNeonYellow = Color(0xFF0A0E27)
val OnNeonRed = Color(0xFF0A0E27)

// ── Text on dark backgrounds ───────────────────────────────────
val OnBackgroundDark = Color(0xFFE8E8F0)
val OnSurfaceDark = Color(0xFFE8E8F0)
val OnSurfaceVariantDark = Color(0xFF9A9AB0)

// ── Outline ────────────────────────────────────────────────────
val OutlineDark = Color(0xFF2A2F4A)

// ── Glow / shadow helpers ──────────────────────────────────────
val GlowCyan = Color(0x4400FFFF)
val GlowMagenta = Color(0x44FF00FF)
val GlowYellow = Color(0x44FFFF00)
val GlowRed = Color(0x44FF0055)

// ── Game cell colors (more vibrant / neon) ─────────────────────
val CellGreen = Color(0xFF00E676)   // Bright neon green
val CellRed = Color(0xFFFF1744)     // Bright neon red
val CellYellow = Color(0xFFFFEA00)  // Bright neon yellow
val CellGray = Color(0xFF2A2F4A)    // Dark blue-gray for inactive cells

// ── Ranking medals ─────────────────────────────────────────────
val MedalGold = Color(0xFFFFD700)
val MedalSilver = Color(0xFFC0C0C0)
val MedalBronze = Color(0xFFCD7F32)

// ── Timer urgency ──────────────────────────────────────────────
val TimerNormal = NeonCyan
val TimerWarning = NeonYellow
val TimerDanger = NeonRed
