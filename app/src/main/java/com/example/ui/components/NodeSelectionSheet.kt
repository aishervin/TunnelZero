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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeSelectionSheet(
  nodes: List<TunnelNode>,
  selectedNode: TunnelNode?,
  isProbing: Boolean,
  onSelectNode: (TunnelNode) -> Unit,
  onScanAll: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var searchQuery by remember { mutableStateOf("") }
  var filterHealthyOnly by remember { mutableStateOf(false) }

  val filteredNodes = nodes.filter { node ->
    val matchesSearch = node.countryName.contains(searchQuery, ignoreCase = true) ||
      node.city.contains(searchQuery, ignoreCase = true) ||
      node.ipAddress.contains(searchQuery, ignoreCase = true)
    val matchesHealth = if (filterHealthyOnly) node.isHealthy else true
    matchesSearch && matchesHealth
  }.sortedBy { if (it.latencyMs > 0) it.latencyMs else 9999L }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = CyberDarkNavy,
    contentColor = CyberTextPrimary,
    modifier = modifier.testTag("node_selection_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .navigationBarsPadding()
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "SECURE RELAY NETWORK",
            color = NeonEmerald,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "${nodes.count { it.isHealthy }}/${nodes.size} Nodes Online & Verified",
            color = CyberTextSecondary,
            fontSize = 12.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onScanAll,
            enabled = !isProbing,
            modifier = Modifier.testTag("sheet_scan_nodes_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Scan Nodes",
              tint = if (isProbing) CyberAmber else NeonCyan
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("sheet_close_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = CyberTextMuted
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Field
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search location or IP…", color = CyberTextMuted, fontSize = 13.sp) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("node_search_field"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = CyberCardBg,
          unfocusedContainerColor = CyberCardBg,
          focusedBorderColor = NeonCyan,
          unfocusedBorderColor = CyberCardBorder,
          focusedTextColor = CyberTextPrimary,
          unfocusedTextColor = CyberTextPrimary
        ),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Healthy filter chip
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "SORTED BY LOWEST LATENCY",
          color = CyberTextMuted,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (filterHealthyOnly) NeonEmeraldDarkSurface else CyberCardBg)
            .border(1.dp, if (filterHealthyOnly) NeonEmerald else CyberCardBorder, RoundedCornerShape(8.dp))
            .clickable { filterHealthyOnly = !filterHealthyOnly }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = if (filterHealthyOnly) "✓ Active Only" else "Show All",
            color = if (filterHealthyOnly) NeonEmerald else CyberTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Node List
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(380.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredNodes, key = { it.id }) { node ->
          val isSelected = selectedNode?.id == node.id
          val cardBorder = if (isSelected) NeonEmerald else CyberCardBorder
          val cardBg = if (isSelected) Color(0xFF0C241B) else CyberCardBg

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(cardBg)
              .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
              .clickable { onSelectNode(node) }
              .padding(14.dp)
              .testTag("node_item_${node.id}")
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = node.flagEmoji, fontSize = 26.sp)

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = node.countryName,
                      color = if (isSelected) NeonEmerald else CyberTextPrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "· ${node.city}",
                      color = CyberTextSecondary,
                      fontSize = 12.sp
                    )
                  }

                  Spacer(modifier = Modifier.height(2.dp))

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "IP: ${node.ipAddress}",
                      color = CyberTextMuted,
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = node.tag,
                      color = NeonCyan,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                // Latency Badge
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
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = node.latencyDisplay,
                    color = latencyColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }

                if (isSelected) {
                  Spacer(modifier = Modifier.width(8.dp))
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = NeonEmerald,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

private val NeonEmeraldDarkSurface = Color(0xFF072B1F)
