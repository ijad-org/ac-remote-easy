package com.ijad.acremoteeasy.ui.add

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Original geometric brand marks for UI only (not manufacturer trademarks).
 * Large colored badge + initials so Select AC cards read as logos.
 */
data class BrandMarkStyle(
    val initials: String,
    val background: Color,
    val accent: Color,
    val foreground: Color = Color.White
)

fun brandMarkFor(brandId: String, brandName: String): BrandMarkStyle {
    val initials = when (brandId) {
        "lg" -> "LG"
        "samsung" -> "S"
        "daikin" -> "DK"
        "haier" -> "H"
        "panasonic" -> "P"
        "carrier" -> "CR"
        "voltas" -> "V"
        "blue_star" -> "BS"
        else -> brandName.take(2).uppercase().ifBlank { "?" }
    }
    val bg = when (brandId) {
        "lg" -> Color(0xFF3B6EA5)
        "samsung" -> Color(0xFF1F4E79)
        "daikin" -> Color(0xFF0D9488)
        "haier" -> Color(0xFF475569)
        "panasonic" -> Color(0xFF4B5563)
        "carrier" -> Color(0xFF5B4B7A)
        "voltas" -> Color(0xFF9A6B3F)
        "blue_star" -> Color(0xFF2563EB)
        else -> Color(0xFF64748B)
    }
    val accent = when (brandId) {
        "lg" -> Color(0xFF93C5FD)
        "samsung" -> Color(0xFF7DD3FC)
        "daikin" -> Color(0xFF5EEAD4)
        "haier" -> Color(0xFFCBD5E1)
        "panasonic" -> Color(0xFFFBBF24)
        "carrier" -> Color(0xFFC4B5FD)
        "voltas" -> Color(0xFFFCD34D)
        "blue_star" -> Color(0xFF93C5FD)
        else -> Color(0xFFE2E8F0)
    }
    return BrandMarkStyle(initials = initials, background = bg, accent = accent)
}

@Composable
fun BrandLettermark(
    brandId: String,
    brandName: String,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val mark = brandMarkFor(brandId, brandName)
    val shape = RoundedCornerShape(size * 0.22f)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(mark.background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            // Soft inner plate
            drawRoundRect(
                color = Color.White.copy(alpha = 0.12f),
                topLeft = Offset(w * 0.1f, h * 0.1f),
                size = Size(w * 0.8f, h * 0.8f),
                cornerRadius = CornerRadius(w * 0.14f, h * 0.14f)
            )
            // Accent ring / mark (original geometry, not trademark)
            when (brandId) {
                "lg", "samsung", "haier", "panasonic" -> {
                    drawCircle(
                        color = mark.accent.copy(alpha = 0.55f),
                        radius = w * 0.34f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = w * 0.045f)
                    )
                }
                "daikin", "voltas" -> {
                    drawRoundRect(
                        color = mark.accent.copy(alpha = 0.5f),
                        topLeft = Offset(w * 0.18f, h * 0.18f),
                        size = Size(w * 0.64f, h * 0.64f),
                        cornerRadius = CornerRadius(w * 0.08f, h * 0.08f),
                        style = Stroke(width = w * 0.045f)
                    )
                }
                "carrier", "blue_star" -> {
                    val path = Path().apply {
                        moveTo(w * 0.5f, h * 0.16f)
                        lineTo(w * 0.82f, h * 0.72f)
                        lineTo(w * 0.18f, h * 0.72f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = mark.accent.copy(alpha = 0.45f),
                        style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
                    )
                }
                else -> {
                    drawCircle(
                        color = mark.accent.copy(alpha = 0.4f),
                        radius = w * 0.3f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = w * 0.04f)
                    )
                }
            }
        }
        Text(
            text = mark.initials,
            color = mark.foreground,
            fontWeight = FontWeight.Bold,
            fontSize = when {
                mark.initials.length >= 2 -> (size.value * 0.32f).sp
                else -> (size.value * 0.42f).sp
            },
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
