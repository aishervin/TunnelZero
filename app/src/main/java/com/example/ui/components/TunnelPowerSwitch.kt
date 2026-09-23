package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TunnelState
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald

@Composable
fun TunnelPowerSwitch(
  tunnelState: TunnelState,
  isProbing: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isConnected = tunnelState == TunnelState.CONNECTED
  val isConnecting = tunnelState == TunnelState.CONNECTING || isProbing

  val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

  // Radar sweep rotation
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (isConnected) 3000 else 6000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "SweepRotation"
  )

  // Pulse scale
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (isConnected || isConnecting) 1.08f else 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "PulseScale"
  )

  val primaryAccentColor by animateColorAsState(
    targetValue = when (tunnelState) {
      TunnelState.CONNECTED -> NeonEmerald
      TunnelState.CONNECTING -> CyberAmber
      TunnelState.PROBING -> NeonCyan
      TunnelState.DISCONNECTING -> CyberRed
      TunnelState.DISCONNECTED -> Color(0xFF334155)
    },
    label = "AccentColor"
  )

  val buttonBgGradient = when (tunnelState) {
    TunnelState.CONNECTED -> Brush.radialGradient(
      listOf(Color(0xFF0F2B20), CyberBlack)
    )
    TunnelState.CONNECTING -> Brush.radialGradient(
      listOf(Color(0xFF2C220E), CyberBlack)
    )
    else -> Brush.radialGradient(
      listOf(Color(0xFF131B2A), CyberBlack)
    )
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(200.dp)
        .testTag("tunnel_power_button"),
      contentAlignment = Alignment.Center
    ) {
      // Outer radar canvas
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .scale(pulseScale)
      ) {
        val strokeWidth = 2.dp.toPx()
        val radius = size.minDimension / 2 - 8.dp.toPx()

        // Outer dashed halo
        drawCircle(
          color = primaryAccentColor.copy(alpha = if (isConnected) 0.35f else 0.15f),
          radius = radius,
          style = Stroke(width = strokeWidth)
        )

        // Sweeping radar arc
        drawArc(
          color = primaryAccentColor,
          startAngle = rotation,
          sweepAngle = if (isConnected) 90f else 60f,
          useCenter = false,
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
      }

      // Middle tactile button
      Box(
        modifier = Modifier
          .size(150.dp)
          .clip(CircleShape)
          .background(buttonBgGradient)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(bounded = true, color = primaryAccentColor)
          ) { onClick() },
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.PowerSettingsNew,
            contentDescription = "Toggle Tunnel",
            tint = primaryAccentColor,
            modifier = Modifier.size(54.dp)
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = when (tunnelState) {
              TunnelState.CONNECTED -> "ACTIVE"
              TunnelState.CONNECTING -> "LINKING"
              TunnelState.PROBING -> "PROBING"
              TunnelState.DISCONNECTING -> "HALT"
              TunnelState.DISCONNECTED -> "START"
            },
            color = primaryAccentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Descriptive tunnel status
    Text(
      text = when (tunnelState) {
        TunnelState.CONNECTED -> "TUNNEL ESTABLISHED & SHIELDED"
        TunnelState.CONNECTING -> "NEGOTIATING HANDSHAKE…"
        TunnelState.PROBING -> "VERIFYING RELAY REACHABILITY…"
        TunnelState.DISCONNECTING -> "TEARING DOWN TUNNEL…"
        TunnelState.DISCONNECTED -> "TAP TO INITIATE ZERO-TRUST TUNNEL"
      },
      color = if (isConnected) NeonEmerald else CyberTextPrimary,
      fontWeight = FontWeight.SemiBold,
      fontSize = 13.sp,
      letterSpacing = 0.5.sp
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = if (isConnected) "ChaCha20-Poly1305 · DNS Leak Guarded" else "Protected by End-to-End Encryption",
      color = CyberTextMuted,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}
