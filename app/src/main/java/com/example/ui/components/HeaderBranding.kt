package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TunnelState
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald

@Composable
fun HeaderBranding(
  tunnelState: TunnelState,
  isProbing: Boolean,
  onScanClick: () -> Unit,
  onConfigClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            Brush.linearGradient(
              listOf(CyberCardBg, Color(0xFF1E283C))
            )
          )
          .border(1.dp, if (tunnelState == TunnelState.CONNECTED) NeonEmerald else CyberCardBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Security Shield",
          tint = if (tunnelState == TunnelState.CONNECTED) NeonEmerald else NeonCyan,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "SHΞN™",
            color = NeonEmerald,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "tunnel",
            color = CyberTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "ᴢᴇʀᴏ",
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 2.sp
          )
        }

        Text(
          text = "Zero-Trust Stealth Engine",
          color = CyberTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      // Refresh / Probe button
      IconButton(
        onClick = onScanClick,
        enabled = !isProbing,
        modifier = Modifier
          .size(40.dp)
          .testTag("scan_nodes_button")
      ) {
        Icon(
          imageVector = Icons.Default.Cached,
          contentDescription = "Scan Nodes",
          tint = if (isProbing) CyberAmber else CyberTextSecondary,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Backup config inspector
      IconButton(
        onClick = onConfigClick,
        modifier = Modifier
          .size(40.dp)
          .testTag("config_dialog_button")
      ) {
        Icon(
          imageVector = Icons.Default.Description,
          contentDescription = "Config & Backup",
          tint = CyberTextSecondary,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}
