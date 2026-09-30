package com.ijad.acremoteeasy.ui.components

import kotlinx.coroutines.launch
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.border
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
    modifier: Modifier = Modifier,
    expanded: Boolean = false
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
        val vPad = if (expanded) 28.dp else 12.dp
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = vPad)
                .fillMaxWidth()
                .then(if (expanded) Modifier.fillMaxHeight() else Modifier),
            verticalArrangement = if (expanded) Arrangement.SpaceBetween else Arrangement.Top
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
                    fontSize = if (expanded) 42.sp else 28.sp,
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
        shadowElevation = 8.dp,
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

/**
 * Distinct Power hero control: soft squircle (not a flat Mi-style circle),
 * teal primary, outer ring, and a short scale pulse when pressed/sent.
 */
@Composable
fun PowerHeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    poweredOn: Boolean = true,
    size: Dp = 128.dp,
    icon: ImageVector,
    pulseKey: Int = 0
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pulseKey) {
        if (pulseKey <= 0) return@LaunchedEffect
        scale.snapTo(1f)
        scale.animateTo(0.88f, animationSpec = tween(70))
        scale.animateTo(1.06f, animationSpec = tween(110))
        scale.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
    }
    val shape = RoundedCornerShape(36.dp)
    val fill = if (poweredOn) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (poweredOn) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val ring = MaterialTheme.colorScheme.primary.copy(alpha = if (poweredOn) 0.28f else 0.12f)

    Box(
        modifier = modifier
            .size(size)
            .scale(scale.value)
            .shadow(elevation = 10.dp, shape = shape, clip = false)
            .border(width = 3.dp, color = ring, shape = shape)
            .clip(shape)
            .background(fill)
            .semantics { contentDescription = "Power" },
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                scope.launch {
                    scale.snapTo(1f)
                    scale.animateTo(0.9f, tween(60))
                    scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                }
                onClick()
            },
            enabled = enabled,
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = content,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = content.copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp, disabledElevation = 0.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(size * 0.36f))
                Spacer(Modifier.height(4.dp))
                Text("Power", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun PowerRemoteButton(
    poweredOn: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    pulseKey: Int = 0
) {
    PowerHeroButton(
        onClick = onClick,
        enabled = enabled,
        poweredOn = poweredOn,
        size = 112.dp,
        icon = icon,
        pulseKey = pulseKey,
        modifier = modifier
    )
}


/** Light elevated grid cell for full-screen remote layout (ref structure, our teal look). */
@Composable
fun GridRemoteCell(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    emphasize: Boolean = false,
    subtitle: String? = null
) {
    val bg = if (emphasize) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val fg = if (emphasize) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = 64.dp)
            .semantics {
                contentDescription = if (subtitle != null) "$label, $subtitle" else label
            },
        shape = RoundedCornerShape(18.dp),
        color = bg,
        tonalElevation = 1.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = if (emphasize) MaterialTheme.colorScheme.primary else fg, modifier = Modifier.size(26.dp))
                Spacer(Modifier.height(6.dp))
            }
            Text(label, style = MaterialTheme.typography.titleMedium, color = fg, textAlign = TextAlign.Center)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
