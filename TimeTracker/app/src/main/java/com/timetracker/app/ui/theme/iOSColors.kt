package com.timetracker.app.ui.theme

import androidx.compose.ui.graphics.Color

// iOS Style Color Palette - Clean & Minimal
// Based on iOS system colors

// iOS Blue
val iOSBlue = Color(0xFF007AFF)
val iOSBlueLight = Color(0xFF5AC8FA)
val iOSBlueDark = Color(0xFF5856D6)

// iOS Green
val iOSGreen = Color(0xFF34C759)
val iOSMint = Color(0xFF00C7BE)

// iOS Red/Orange
val iOSRed = Color(0xFFFF3B30)
val iOSOrange = Color(0xFFFF9500)
val iOSYellow = Color(0xFFFFCC00)

// iOS Pink/Purple
val iOSPink = Color(0xFFFF2D55)
val iOSPurple = Color(0xFFAF52DE)
val iOSTeal = Color(0xFF5AC8FA)

// iOS Grays - Light Mode
val iOSGray = Color(0xFF8E8E93)
val iOSGray2 = Color(0xFFAEAEB2)
val iOSGray3 = Color(0xFFC7C7CC)
val iOSGray4 = Color(0xFFD1D1D6)
val iOSGray5 = Color(0xFFE5E5EA)
val iOSGray6 = Color(0xFFF2F2F7)

// iOS Background Colors
val iOSSystemBackground = Color(0xFFFFFFFF)
val iOSSecondaryBackground = Color(0xFFF2F2F7)
val iOSTertiaryBackground = Color(0xFFFFFFFF)

// iOS Grouped Background
val iOSSystemGroupedBackground = Color(0xFFF2F2F7)
val iOSSecondaryGroupedBackground = Color(0xFFFFFFFF)
val iOSTertiaryGroupedBackground = Color(0xFFF2F2F7)

// iOS Label Colors
val iOSLabel = Color(0xFF000000)
val iOSSecondaryLabel = Color(0xFF3C3C4399)  // 60% opacity
val iOSTertiaryLabel = Color(0xFF3C3C434D)   // 30% opacity
val iOSQuaternaryLabel = Color(0xFF3C3C432E) // 18% opacity

// iOS Fill Colors
val iOSSystemFill = Color(0xFF78788033)  // 20% opacity
val iOSSecondaryFill = Color(0xFF78788029) // 16% opacity
val iOSTertiaryFill = Color(0xFF7676801F)  // 12% opacity
val iOSQuaternaryFill = Color(0xFF74748014) // 8% opacity

// iOS Separator
val iOSSeparator = Color(0xFFC6C6C8)
val iOSOpaqueSeparator = Color(0xFFC6C6C8)

// iOS Semantic Colors
val iOSSuccess = iOSGreen
val iOSWarning = iOSOrange
val iOSError = iOSRed
val iOSInfo = iOSBlue

// iOS Category Colors - Vibrant but not overwhelming
val iOSCategoryColors = listOf(
    iOSBlue,      // Work
    iOSGreen,     // Study
    iOSMint,      // Rest
    iOSOrange,    // Sport
    iOSPurple,    // Entertainment
    iOSTeal,      // Reading
    iOSPink,      // Meeting
    iOSGray       // Other
)

// iOS Productive/Unproductive
val iOSProductive = Color(0xFF34C759)  // Soft green
val iOSUnproductive = Color(0xFFFF9500) // Soft orange

// Helper function to get iOS color by index
fun getiOSCategoryColor(index: Int): Color {
    return iOSCategoryColors.getOrElse(index) { iOSGray }
}

// iOS Style Shadow
val iOSShadowColor = Color(0xFF000000).copy(alpha = 0.1f)
