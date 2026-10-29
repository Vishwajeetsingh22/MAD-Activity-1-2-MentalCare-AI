package com.aimentalcare.app.theme

import android.graphics.Color

/**
 * MentalCare AI - Brand & Semantic Color Design Tokens (Phase 1)
 *
 * Brand Palette:
 * Primary Green: #10B981
 * Dark Green:    #047857
 * Mint:          #ECFDF5
 * Light Mint:    #D1FAE5
 * AI Purple:     #8B5CF6
 * Moderate:      #F59E0B
 * High:          #EF4444
 * Main Text:     #1F2937
 * Secondary Text:#6B7280
 * Light Background: #F8FAFC
 * Dark Background:  #0F172A
 * Dark Surface:     #1E293B
 * Dark Secondary:   #94A3B8
 */
object AppColors {
    // Brand Colors
    val PrimaryGreen = Color.parseColor("#10B981")
    val DarkGreen = Color.parseColor("#047857")
    val Mint = Color.parseColor("#ECFDF5")
    val LightMint = Color.parseColor("#D1FAE5")
    val AiPurple = Color.parseColor("#8B5CF6")

    // Semantic Status Colors
    val Moderate = Color.parseColor("#F59E0B")
    val High = Color.parseColor("#EF4444")
    val Low = Color.parseColor("#10B981")

    // Light Theme Neutrals
    val MainText = Color.parseColor("#1F2937")
    val SecondaryText = Color.parseColor("#6B7280")
    val LightBackground = Color.parseColor("#F8FAFC")
    val White = Color.parseColor("#FFFFFF")
    val CardBorder = Color.parseColor("#E2E8F0")

    // Dark Theme Neutrals
    val DarkBackground = Color.parseColor("#0F172A")
    val DarkSurface = Color.parseColor("#1E293B")
    val DarkSecondaryText = Color.parseColor("#94A3B8")
    val DarkMainText = Color.parseColor("#F8FAFC")
    val DarkCardBorder = Color.parseColor("#334155")
}
