package com.ijad.acremoteeasy.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ijad.acremoteeasy.ui.theme.Danger
import com.ijad.acremoteeasy.ui.theme.Sky

/** Large circular remote key — min ~48–72dp touch target with soft elevation. */
@Composable
fun RoundRemoteButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    size: Dp = 76.dp,
    primary: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null,
    accessibilityLabel: String? = null
) {
    val colors = when {
        containerColor != null -> ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor ?: Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = (contentColor ?: Color.White).copy(alpha = 0.6f)
        )
        primary -> ButtonDefaults.buttonColors()
        else -> ButtonDefaults.filledTonalButtonColors()
    }
    val elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 4.dp,
        pressedElevation = 1.dp,
        disabledElevation = 0.dp
    )
    val content: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(Modifier.height(2.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
    val a11y = accessibilityLabel ?: label
    val buttonModifier = modifier
        .size(size.coerceAtLeast(48.dp))
        .semantics { contentDescription = a11y }

    if (primary || containerColor != null) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
            shape = CircleShape,
            contentPadding = PaddingValues(8.dp),
            colors = colors,
            elevation = elevation,
            content = { content() }
        )
    } else {
        FilledTonalButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
            shape = CircleShape,
            contentPadding = PaddingValues(8.dp),
            colors = colors,
            elevation = elevation,
            content = { content() }
        )
    }
}

@Composable
fun WideRemoteButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { contentDescription = label },
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 2.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

/** Pill-shaped labeled key for Mode / Fan / Swing grid. */
@Composable
fun PillRemoteButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    enabled: Boolean = true,
    selected: Boolean = false
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.filledTonalButtonColors()
    }
    val content: @Composable () -> Unit = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(4.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
    val buttonModifier = modifier
        .heightIn(min = 64.dp)
        .semantics {
            contentDescription = if (subtitle != null) "$label, $subtitle" else label
        }

    if (selected) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
            colors = colors,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
            content = { content() }
        )
    } else {
        FilledTonalButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
            colors = colors,
            elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 2.dp),
            content = { content() }
        )
    }
}

@Composable
fun TempControlRow(
    temperature: Int,
    onDown: () -> Unit,
    onUp: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundRemoteButton(
            label = "−",
            onClick = onDown,
            enabled = enabled,
            size = 88.dp,
            primary = true,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            accessibilityLabel = "Temp Down"
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$temperature°",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Temp °C",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RoundRemoteButton(
            label = "+",
            onClick = onUp,
            enabled = enabled,
            size = 88.dp,
            primary = true,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            accessibilityLabel = "Temp Up"
        )
    }
}

/** Compact LCD-style status strip at the top of a physical remote. */
@Composable
fun LcdStatusStrip(
    brandName: String,
    deviceName: String,
    poweredOn: Boolean,
    modeLabel: String,
    temperature: Int,
    fanLabel: String,
    modifier: Modifier = Modifier
) {
    val lcdBg = Color(0xFF0A1628)
    val lcdFg = Color(0xFF7DFFB3)
    val lcdDim = Color(0xFF3D8F6A)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "$deviceName, $brandName, ${if (poweredOn) "on" else "off"}, " +
                        "$modeLabel, $temperature degrees, fan $fanLabel"
            },
        shape = RoundedCornerShape(12.dp),
        color = lcdBg,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = brandName.uppercase(),
                    color = lcdDim,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = if (poweredOn) "ON" else "OFF",
                    color = if (poweredOn) lcdFg else Danger.copy(alpha = 0.75f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deviceName,
                        color = lcdFg.copy(alpha = 0.85f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$modeLabel  ·  FAN $fanLabel",
                        color = lcdFg,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$temperature°C",
                    color = lcdFg,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Subtle scanline accent
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Sky.copy(alpha = 0.25f))
            )
        }
    }
}

/** Raised handset body that frames remote keys. */
@Composable
fun RemoteHandsetBody(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            content()
        }
    }
}

@Composable
fun PowerRemoteButton(
    poweredOn: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector
) {
    val onColor = Danger
    val offColor = MaterialTheme.colorScheme.surfaceVariant
    val onContent = Color.White
    val offContent = MaterialTheme.colorScheme.onSurfaceVariant
    RoundRemoteButton(
        label = "Power",
        icon = icon,
        onClick = onClick,
        enabled = enabled,
        size = 104.dp,
        primary = true,
        containerColor = if (poweredOn) onColor else offColor,
        contentColor = if (poweredOn) onContent else offContent,
        modifier = modifier
    )
}
