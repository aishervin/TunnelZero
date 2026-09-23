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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TunnelNode
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald

@Composable
fun SelectedNodeCard(
  selectedNode: TunnelNode?,
  allNodes: List<TunnelNode>,
  onSelectNode: (TunnelNode) -> Unit,
  onOpenAllNodes: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth()
  ) {
    // Main Selected Node Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(CyberCardBg)
        .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
        .clickable { onOpenAllNodes() }
        .padding(16.dp)
        .testTag("selected_node_card")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Flag Container
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF161E2E))
              .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = selectedNode?.flagEmoji ?: "🌐",
              fontSize = 24.sp
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = selectedNode?.countryName ?: "Select Relay Node",
                color = CyberTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "· ${selectedNode?.city ?: ""}",
                color = CyberTextSecondary,
                fontSize = 13.sp
              )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "IP: ${selectedNode?.ipAddress ?: "0.0.0.0"}",
                color = CyberTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .size(5.dp)
                  .clip(CircleShape)
                  .background(CyberCardBorder)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = selectedNode?.tag ?: "Stealth",
                color = NeonCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        // Latency Badge & Arrow
        Row(verticalAlignment = Alignment.CenterVertically) {
          selectedNode?.let { node ->
            val latencyColor = when {
              node.latencyMs in 1..70 -> NeonEmerald
              node.latencyMs in 71..150 -> CyberAmber
              node.latencyMs > 150 -> CyberRed
              else -> CyberTextMuted
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(latencyColor.copy(alpha = 0.15f))
                .border(1.dp, latencyColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(latencyColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = node.latencyDisplay,
                  color = latencyColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View all nodes",
            tint = CyberTextMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Quick Select Location Chips (Canada, Germany, USA, Israel, Netherlands, Finland, UK, Japan)
    Text(
      text = "QUICK RELAY SELECTOR",
      color = CyberTextMuted,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      fontFamily = FontFamily.Monospace,
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(allNodes) { node ->
        val isSelected = selectedNode?.id == node.id
        val borderColor = if (isSelected) NeonEmerald else CyberCardBorder
        val bgColor = if (isSelected) Color(0xFF0C241B) else CyberCardBg

        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onSelectNode(node) }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("quick_node_${node.countryCode}"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = node.flagEmoji, fontSize = 16.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = node.countryName,
            color = if (isSelected) NeonEmerald else CyberTextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
          if (node.latencyMs > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${node.latencyMs}ms",
              color = if (node.latencyMs < 60) NeonEmerald else CyberAmber,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}
