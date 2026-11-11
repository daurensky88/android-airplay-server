package io.github.jqssun.airplay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.jqssun.airplay.R

/**
 * Фирменная палитра LILU AIR.
 * Фиолетовый — основное действие, бирюзовый — фокус и техническая информация,
 * розовый — только опасные действия.
 */
object LiluColors {
    val Violet = Color(0xFF8A3EE0)
    val VioletBright = Color(0xFFA24BF0)
    val VioletDeep = Color(0xFF6A2EC8)
    val Magenta = Color(0xFFC45BFF)
    val Cyan = Color(0xFF19E8E0)
    val CyanDim = Color(0xFF14B8B2)
    val Danger = Color(0xFFFF5C8A)
    val DangerSoft = Color(0xFFFF8FB0)

    val Bg = Color(0xFF0A0718)
    val BgDeep = Color(0xFF05030B)
    val Surface = Color(0xFF160C2E)
    val SurfaceFocus = Color(0xFF1D1138)
    val Line = Color(0xFF2C1C52)
    val Text = Color(0xFFEDE6FF)
    val TextDim = Color(0xFF9A8CC4)
    val RainGreen = Color(0xFF19E8A0)
}

private val LiluScheme = darkColorScheme(
    primary = LiluColors.Violet,
    onPrimary = Color.White,
    primaryContainer = LiluColors.VioletDeep,
    onPrimaryContainer = Color.White,
    secondary = LiluColors.Cyan,
    onSecondary = Color(0xFF04201F),
    secondaryContainer = LiluColors.Surface,
    onSecondaryContainer = LiluColors.Cyan,
    tertiary = LiluColors.Magenta,
    onTertiary = Color.White,
    background = LiluColors.Bg,
    onBackground = LiluColors.Text,
    surface = LiluColors.Surface,
    onSurface = LiluColors.Text,
    surfaceVariant = LiluColors.SurfaceFocus,
    onSurfaceVariant = LiluColors.TextDim,
    surfaceContainer = LiluColors.Surface,
    surfaceContainerHigh = LiluColors.SurfaceFocus,
    outline = LiluColors.Line,
    outlineVariant = LiluColors.Line,
    error = LiluColors.Danger,
    onError = Color.White,
    scrim = Color(0xCC05030B),
)

val Geologica = FontFamily(
    Font(R.font.geologica_regular, FontWeight.Normal),
    Font(R.font.geologica_semibold, FontWeight.SemiBold),
    Font(R.font.geologica_bold, FontWeight.Bold),
)

/** Отдельное начертание с подрезанными углами — для капса и рубрикаторов. */
val GeologicaSharp = FontFamily(Font(R.font.geologica_sharp, FontWeight.Bold))

/** Моноширинный для адресов, разрешений и битрейта: цифры не пляшут при обновлении. */
val LiluMono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold),
)

private fun geo(size: Int, weight: FontWeight, lh: Int, ls: Double = 0.0) = TextStyle(
    fontFamily = Geologica, fontWeight = weight,
    fontSize = size.sp, lineHeight = lh.sp, letterSpacing = ls.sp,
)

private val LiluType = Typography(
    displayLarge = geo(44, FontWeight.Bold, 50),
    displayMedium = geo(34, FontWeight.Bold, 40),
    headlineLarge = geo(28, FontWeight.SemiBold, 34),
    headlineMedium = geo(24, FontWeight.SemiBold, 30),
    headlineSmall = geo(20, FontWeight.SemiBold, 26),
    titleLarge = geo(19, FontWeight.SemiBold, 25),
    titleMedium = geo(16, FontWeight.SemiBold, 22),
    titleSmall = geo(14, FontWeight.SemiBold, 20),
    bodyLarge = geo(16, FontWeight.Normal, 23),
    bodyMedium = geo(14, FontWeight.Normal, 20),
    bodySmall = geo(12, FontWeight.Normal, 17),
    labelLarge = geo(15, FontWeight.SemiBold, 20),
    labelMedium = geo(13, FontWeight.SemiBold, 18),
    labelSmall = TextStyle(
        fontFamily = GeologicaSharp, fontWeight = FontWeight.Bold,
        fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 1.1.sp,
    ),
)

/** Стиль для технических данных. */
val MonoStyle = TextStyle(
    fontFamily = LiluMono, fontWeight = FontWeight.Normal,
    fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp,
)

@Composable
fun AirPlayTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Палитра фиксированная: системные динамические цвета на Android TV
    // дают серо-бурый дефолт и ломают фирменный вид.
    MaterialTheme(colorScheme = LiluScheme, typography = LiluType, content = content)
}
