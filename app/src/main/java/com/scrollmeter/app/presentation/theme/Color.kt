package com.scrollmeter.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// CYBER LUXURY & MINERAL GLOW PALETTE (2026)
// ==========================================

// Obsidian & Void Bases (Gen Z Deep Dark Aesthetic)
val ObsidianVoid = Color(0xFF030508)
val ObsidianSurface = Color(0xFF0A0E14)
val ObsidianSurfaceElevated = Color(0xFF131922)
val ObsidianSurfaceGlass = Color(0x0DFFFFFF) // 5% White Frosted Glass

// High-Voltage Dopamine Accents
val ElectricMint = Color(0xFF00F5A0)       // Primary glow (Opal mineral green)
val CyberCyan = Color(0xFF00F0FF)          // High-tech telemetry cyan
val HyperViolet = Color(0xFF7A22FF)        // Secondary ambient glow
val ChampagneGold = Color(0xFFF5E6B8)      // Milestone unlock prestige gold
val AcidCoral = Color(0xFFFF4D6D)          // Warning / heavy screen-time ceiling

// Light Theme - Crisp Mineral Aesthetics
val EmeraldPrimaryLight = Color(0xFF00897B)
val EmeraldOnPrimaryLight = Color(0xFFFFFFFF)
val EmeraldPrimaryContainerLight = Color(0xFFA7F0D8)
val EmeraldOnPrimaryContainerLight = Color(0xFF00201A)

val TurquoiseSecondaryLight = Color(0xFF00796B)
val TurquoiseOnSecondaryLight = Color(0xFFFFFFFF)
val TurquoiseSecondaryContainerLight = Color(0xFFB2DFDB)
val TurquoiseOnSecondaryContainerLight = Color(0xFF00201C)

val AccentTertiaryLight = Color(0xFF0288D1)
val AccentOnTertiaryLight = Color(0xFFFFFFFF)

val BackgroundLight = Color(0xFFF8FAF9)
val OnBackgroundLight = Color(0xFF191C1B)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF191C1B)
val SurfaceVariantLight = Color(0xFFE2EBE7)
val OnSurfaceVariantLight = Color(0xFF3F4945)

// Dark Theme - Cyber Luxury Tokens
val EmeraldPrimaryDark = ElectricMint
val EmeraldOnPrimaryDark = Color(0xFF003820)
val EmeraldPrimaryContainerDark = Color(0xFF005132)
val EmeraldOnPrimaryContainerDark = Color(0xFFA7F0D8)

val TurquoiseSecondaryDark = CyberCyan
val TurquoiseOnSecondaryDark = Color(0xFF00363A)
val TurquoiseSecondaryContainerDark = Color(0xFF004F55)
val TurquoiseOnSecondaryContainerDark = Color(0xFFB2DFDB)

val AccentTertiaryDark = ChampagneGold
val AccentOnTertiaryDark = Color(0xFF3E2E00)

val BackgroundDark = ObsidianVoid
val OnBackgroundDark = Color(0xFFF1F5F9)
val SurfaceDark = ObsidianSurface
val OnSurfaceDark = Color(0xFFF1F5F9)
val SurfaceVariantDark = ObsidianSurfaceElevated
val OnSurfaceVariantDark = Color(0xFF94A3B8)

// Gradients & Ambient Lighting Brushes
val GlassBorderGradient = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.28f),
        Color.White.copy(alpha = 0.04f)
    )
)

val ActiveGlowBorderGradient = Brush.linearGradient(
    colors = listOf(
        ElectricMint.copy(alpha = 0.6f),
        CyberCyan.copy(alpha = 0.2f),
        Color.Transparent
    )
)

val ProgressGradientStart = ElectricMint
val ProgressGradientEnd = CyberCyan
val MetricCardBackground = ObsidianSurface

// Cyber Luxury & Mineral Glow Semantic Aliases
val EmeraldNeonPrimary = ElectricMint
val EmeraldNeonSecondary = CyberCyan
val EmeraldContainerDark = ObsidianSurfaceElevated
val MidnightDark = ObsidianVoid

