package com.ijad.acremoteeasy.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Simple legal-safe brand lettermarks (initials in colored tiles).
 * Not manufacturer trademarks — original geometric marks for UI only.
 */
data class BrandMarkStyle(
    val initials: String,
    val background: Color,
    val foreground: Color = Color.White
)

fun brandMarkFor(brandId: String, brandName: String): BrandMarkStyle {
    val initials = when (brandId) {
        "lg" -> "LG"
        "samsung" -> "S"
        "daikin" -> "D"
        "haier" -> "H"
        "panasonic" -> "P"
        "carrier" -> "C"
        "voltas" -> "V"
        "blue_star" -> "BS"
        else -> brandName.take(2).uppercase().ifBlank { "?" }
    }
    // Calm, distinct hues — not copied brand identity systems
    val bg = when (brandId) {
        "lg" -> Color(0xFF4A6FA5)
        "samsung" -> Color(0xFF3D5A80)
        "daikin" -> Color(0xFF2A9D8F)
        "haier" -> Color(0xFF577590)
        "panasonic" -> Color(0xFF6B705C)
        "carrier" -> Color(0xFF7B6B8A)
        "voltas" -> Color(0xFF8A6A4A)
        "blue_star" -> Color(0xFF4C6A8A)
        else -> Color(0xFF64748B)
    }
    return BrandMarkStyle(initials = initials, background = bg)
}

@Composable
fun BrandLettermark(
    brandId: String,
    brandName: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val mark = brandMarkFor(brandId, brandName)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
            .background(mark.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mark.initials,
            color = mark.foreground,
            fontWeight = FontWeight.Bold,
            fontSize = if (mark.initials.length > 1) 18.sp else 22.sp,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
