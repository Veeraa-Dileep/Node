package com.node.fitness.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.node.fitness.ui.theme.*

object GlassTokens {
    val BlurRadius = 20.dp
    val BackgroundAlpha = 0.12f
    val BorderAlpha = 0.10f
    val CornerRadiusDefault = 16.dp
    val CornerRadiusSmall = 12.dp
    val CornerRadiusLarge = 24.dp
    val BorderWidth = 1.dp
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CornerRadiusDefault,
    backgroundAlpha: Float = GlassTokens.BackgroundAlpha,
    borderAlpha: Float = GlassTokens.BorderAlpha,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .graphicsLayer {
                // Hardware-accelerated blur (API 31+, guaranteed by minSdk=31)
                renderEffect = RenderEffect
                    .createBlurEffect(
                        GlassTokens.BlurRadius.toPx(),
                        GlassTokens.BlurRadius.toPx(),
                        Shader.TileMode.CLAMP
                    )
                    .asComposeRenderEffect()
                clip = true
                this.shape = shape
            }
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = backgroundAlpha),
                        Color.White.copy(alpha = backgroundAlpha * 0.5f)
                    )
                )
            )
            .border(
                width = GlassTokens.BorderWidth,
                color = Color.White.copy(alpha = borderAlpha),
                shape = shape
            ),
        content = content
    )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CornerRadiusDefault,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark.copy(alpha = 0.85f),
                        SurfaceDark.copy(alpha = 0.6f)
                    )
                )
            )
            .border(
                width = GlassTokens.BorderWidth,
                color = Color.White.copy(alpha = GlassTokens.BorderAlpha),
                shape = shape
            ),
        content = content
    )
}

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(GlassTokens.CornerRadiusSmall)
    val alpha = if (enabled) 1f else 0.5f

    Surface(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer { this.alpha = alpha },
        shape = shape,
        color = Color.Transparent,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(AccentOrangeStart, AccentOrangeEnd)
                    ),
                    shape
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun StatBadge(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = SurfaceVariantDark,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        content = content
    )
}

@Composable
fun FormatBadge(
    text: String,
    color: Color = AccentOrange,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
