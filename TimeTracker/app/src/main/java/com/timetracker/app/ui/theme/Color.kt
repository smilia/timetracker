package com.timetracker.app.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Color Palette - Soft & Professional
// Primary - Soft Blue
val Primary500 = Color(0xFF6366F1)
val Primary400 = Color(0xFF818CF8)
val Primary300 = Color(0xFFA5B4FC)
val Primary200 = Color(0xFFC7D2FE)
val Primary100 = Color(0xFFE0E7FF)
val Primary50 = Color(0xFFEEF2FF)

// Secondary - Soft Teal
val Secondary500 = Color(0xFF14B8A6)
val Secondary400 = Color(0xFF2DD4BF)
val Secondary300 = Color(0xFF5EEAD4)
val Secondary100 = Color(0xFFCCFBF1)
val Secondary50 = Color(0xFFF0FDFA)

// Accent - Soft Coral
val Accent500 = Color(0xFFFB7185)
val Accent400 = Color(0xFFFB7185)
val Accent300 = Color(0xFFFDA4AF)
val Accent100 = Color(0xFFFFE4E6)

// Neutral - Slate
val Neutral900 = Color(0xFF0F172A)
val Neutral800 = Color(0xFF1E293B)
val Neutral700 = Color(0xFF334155)
val Neutral600 = Color(0xFF475569)
val Neutral500 = Color(0xFF64748B)
val Neutral400 = Color(0xFF94A3B8)
val Neutral300 = Color(0xFFCBD5E1)
val Neutral200 = Color(0xFFE2E8F0)
val Neutral100 = Color(0xFFF1F5F9)
val Neutral50 = Color(0xFFF8FAFC)

// Semantic Colors
val Success500 = Color(0xFF22C55E)
val Success100 = Color(0xFFDCFCE7)
val Warning500 = Color(0xFFF59E0B)
val Warning100 = Color(0xFFFEF3C7)
val Error500 = Color(0xFFEF4444)
val Error100 = Color(0xFFFEE2E2)
val Info500 = Color(0xFF3B82F6)
val Info100 = Color(0xFFDBEAFE)

// Category Colors - Harmonized Palette
val CategoryWork = Color(0xFFFB7185)      // Soft Red
val CategoryStudy = Color(0xFF2DD4BF)     // Teal
val CategoryRest = Color(0xFF34D399)      // Green
val CategorySport = Color(0xFFFBBF24)     // Amber
val CategoryEntertainment = Color(0xFFA78BFA) // Purple
val CategoryReading = Color(0xFF60A5FA)   // Blue
val CategoryMeeting = Color(0xFFFB923C)   // Orange
val CategoryOther = Color(0xFF94A3B8)     // Gray

// Productive/Unproductive Colors
val ProductiveColor = Color(0xFF86EFAC)   // Soft Green
val UnproductiveColor = Color(0xFFFDBA74) // Soft Orange

val CategoryColors = listOf(
    CategoryWork,
    CategoryStudy,
    CategoryRest,
    CategorySport,
    CategoryEntertainment,
    CategoryReading,
    CategoryMeeting,
    CategoryOther
)

fun getCategoryColor(index: Int): Color {
    return CategoryColors.getOrElse(index) { CategoryOther }
}

// Legacy compatibility
val PrimaryBlue = Primary500
val PrimaryLight = Primary100
val PrimaryDark = Primary400
val WorkRed = CategoryWork
val StudyCyan = CategoryStudy
val RestGreen = CategoryRest
val SportYellow = CategorySport
val EntertainmentPurple = CategoryEntertainment
val ReadingTeal = CategoryReading
val MeetingOrange = CategoryMeeting
val OtherGray = CategoryOther
val BackgroundLight = Neutral50
val SurfaceLight = Color.White
val DividerLight = Neutral200
val TextPrimary = Neutral800
val TextSecondary = Neutral600
val TextTertiary = Neutral400
val SuccessGreen = Success500
val WarningYellow = Warning500
val ErrorRed = Error500
