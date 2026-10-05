package com.mypeals.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyPealsTheme(darkMode: Boolean = isSystemInDarkTheme(),content: @Composable () -> Unit ) {

    val colorScheme = if (darkMode) myPealsDarkColorPallete else myPealsLightColorPallete


    MaterialTheme(
        colorScheme = colorScheme,
        typography = myPealsTypography,
        shapes = myPealsShapes,
        content = content
    )

}

val myPealsLightColorPallete = lightColorScheme(
    primary = Color(0xFF4A7C59),
    secondary = Color(0xFFD1E8E2),
    background = Color.White,
    surface = Color(0xFFF4F9F7),
)

val myPealsDarkColorPallete = darkColorScheme(
    primary = Color(0xFF6FA07E),
    secondary = Color(0xFF3B524C),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
)

val myPealsTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
)

private val myPealsShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)