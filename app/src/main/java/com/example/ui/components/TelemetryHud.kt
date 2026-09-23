package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelemetryStats
import com.example.model.TunnelState
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald

@Composable
fun TelemetryHud(
  telemetry: TelemetryStats,
  tunnelState: TunnelState,
  modifier: Modifier = Modifier
) {
  val isConnected = tunnelState == TunnelState.CONNECTED

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CyberCardBg)
      .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
      .padding(16.dp)
      .testTag("telemetry_hud")
  ) {
    // Top Row: Download & Upload Speeds
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      MetricTile(
        title = "DOWNLOAD",
        value = if (isConnected) telemetry.formatSpeed(telemetry.downloadSpeedKbps) else "0.0 Kbps",
        icon = Icons.Default.ArrowDownward,
        accentColor = NeonCyan,
        modifier = Modifier.weight(1f)
      )

      Spacer(modifier = Modifier.width(12.dp))

      MetricTile(
        title = "UPLOAD",
        value = if (isConnected) telemetry.formatSpeed(telemetry.uploadSpeedKbps) else "0.0 Kbps",
        icon = Icons.Default.ArrowUpward,
        accentColor = NeonEmerald,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Middle Row: Uptime & Total Data
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      MetricTile(
        title = "SESSION UPTIME",
        value = if (isConnected) telemetry.formatDuration() else "00:00",
        icon = Icons.Default.Timer,
        accentColor = CyberBlue,
        modifier = Modifier.weight(1f)
      )

      Spacer(modifier = Modifier.width(12.dp))

      MetricTile(
        title = "DATA TRANSFERRED",
        value = if (isConnected) telemetry.formatBytes(telemetry.totalUploadedBytes + telemetry.totalDownloadedBytes) else "0 B",
        icon = Icons.Default.DataUsage,
        accentColor = Color(0xFFA855F7),
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Bottom Status Strip: Protocol & DNS Guard
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFF090E17))
        .border(1.dp, Color(0xFF172033), RoundedCornerShape(10.dp))
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = if (isConnected) NeonEmerald else CyberTextMuted,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "WIREGUARD 0-TRUST",
            color = CyberTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.VpnKey,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "CHACHA20 · 1.1.1.1 DNS",
            color = NeonCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun MetricTile(
  title: String,
  value: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF0A0F1B))
      .border(1.dp, Color(0xFF161F30), RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = title,
          color = CyberTextMuted,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = value,
        color = CyberTextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
