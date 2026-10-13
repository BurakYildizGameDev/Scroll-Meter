package com.scrollmeter.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// SCROLL-METER PROFESSIONAL DESIGN TOKENS
// Focused, high-contrast dark theme inspired by modern engineering tools
// =========================================================================

// Deep Navy Slate Background Surfaces (Modern, energetic, not suffocating)
val ObsidianVoid = Color(0xFF0F172A)           // Rich midnight navy canvas
val ObsidianSurface = Color(0xFF1E293B)        // Sleek slate blue card surface
val ObsidianSurfaceElevated = Color(0xFF26354A)// Elevated slate card surface
val ObsidianSurfaceGlass = Color(0x2E38BDF8)   // Subtle cyan-tinted hairline glass

// Precision Telemetry Accents (Vibrant & High-Contrast)
val ElectricMint = Color(0xFF10B981)          // Primary Telemetry Emerald (Active metrics)
val CyberCyan = Color(0xFF38BDF8)             // Secondary Telemetry Sky/Cyan (Data visualization)
val RoyalBlue = Color(0xFF3B82F6)             // Dynamic Royal Indigo
val HyperViolet = Color(0xFF818CF8)           // Lab Mode Iris (Experimental features)
val ChampagneGold = Color(0xFFFBBF24)         // Achievement Warm Amber (Milestones)
val AcidCoral = Color(0xFFF87171)             // Alert / High Screen-time Red

// Light Theme Tokens (Crisp Editorial Contrast)
val EmeraldPrimaryLight = Color(0xFF059669)
val EmeraldOnPrimaryLight = Color(0xFFFFFFFF)
val EmeraldPrimaryContainerLight = Color(0xFFD1FAE5)
val EmeraldOnPrimaryContainerLight = Color(0xFF064E3B)

val TurquoiseSecondaryLight = Color(0xFF0284C7)
val TurquoiseOnSecondaryLight = Color(0xFFFFFFFF)
val TurquoiseSecondaryContainerLight = Color(0xFFE0F2FE)
val TurquoiseOnSecondaryContainerLight = Color(0xFF0369A1)

val AccentTertiaryLight = Color(0xFF6366F1)
val AccentOnTertiaryLight = Color(0xFFFFFFFF)

val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF64748B)

// Dark Theme Tokens (Engineered Precision with Navy Glow)
val EmeraldPrimaryDark = ElectricMint
val EmeraldOnPrimaryDark = Color(0xFF022C22)
val EmeraldPrimaryContainerDark = Color(0xFF064E3B)
val EmeraldOnPrimaryContainerDark = Color(0xFFA7F3D0)

val TurquoiseSecondaryDark = CyberCyan
val TurquoiseOnSecondaryDark = Color(0xFF082F49)
val TurquoiseSecondaryContainerDark = Color(0xFF075985)
val TurquoiseOnSecondaryContainerDark = Color(0xFFBAE6FD)

val AccentTertiaryDark = ChampagneGold
val AccentOnTertiaryDark = Color(0xFF451A03)

val BackgroundDark = ObsidianVoid
val OnBackgroundDark = Color(0xFFF8FAFC)
val SurfaceDark = ObsidianSurface
val OnSurfaceDark = Color(0xFFF8FAFC)
val SurfaceVariantDark = ObsidianSurfaceElevated
val OnSurfaceVariantDark = Color(0xFF94A3B8)

// Hairline Borders & Subtle Gradients
val GlassBorderGradient = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.12f),
        Color.White.copy(alpha = 0.03f)
    )
)

val ActiveGlowBorderGradient = Brush.linearGradient(
    colors = listOf(
        ElectricMint.copy(alpha = 0.40f),
        CyberCyan.copy(alpha = 0.15f),
        Color.Transparent
    )
)

val ProgressGradientStart = ElectricMint
val ProgressGradientEnd = CyberCyan
val MetricCardBackground = ObsidianSurface

// Semantic Aliases for backwards compatibility
val EmeraldNeonPrimary = ElectricMint
val EmeraldNeonSecondary = CyberCyan
val EmeraldContainerDark = ObsidianSurfaceElevated
val MidnightDark = ObsidianVoid
