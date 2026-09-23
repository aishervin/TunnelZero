package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

@Composable
fun BackupConfigDialog(
  selectedNode: TunnelNode?,
  rawConfig: String,
  onImportBackup: (String) -> Boolean,
  onResetDefaults: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  var isEditingImport by remember { mutableStateOf(false) }
  var importText by remember { mutableStateOf("") }
  var copyToast by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(CyberDarkNavy)
        .border(1.dp, CyberCardBorder, RoundedCornerShape(20.dp))
        .padding(20.dp)
        .testTag("backup_config_dialog")
    ) {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "WIREGUARD INTERNAL BACKUP",
              color = NeonCyan,
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = if (isEditingImport) "Paste WireGuard .conf or JSON" else "Embedded Active Configuration",
              color = CyberTextSecondary,
              fontSize = 11.sp
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).testTag("dialog_close_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = CyberTextMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!isEditingImport) {
          // Display current config
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 240.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF070B12))
              .border(1.dp, Color(0xFF141D2D), RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Column(
              modifier = Modifier
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
            ) {
              Text(
                text = rawConfig,
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Copy Button & Switch to Import
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                clipboardManager.setText(AnnotatedString(rawConfig))
                copyToast = true
              },
              modifier = Modifier.weight(1f).testTag("copy_config_button"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CyberTextPrimary
              )
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (copyToast) "COPIED!" else "COPY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { isEditingImport = true },
              modifier = Modifier.weight(1f).testTag("enter_import_mode_button"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E283C),
                contentColor = NeonCyan
              )
            ) {
              Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("IMPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        } else {
          // Input text field for import
          OutlinedTextField(
            value = importText,
            onValueChange = { importText = it },
            placeholder = {
              Text(
                "[Interface]\nPrivateKey = ...\nAddress = 10.66.66.2/32\n\n[Peer]\nPublicKey = ...\nEndpoint = ...",
                color = CyberTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .testTag("import_config_input"),
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

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { isEditingImport = false },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("CANCEL", fontSize = 11.sp, color = CyberTextSecondary)
            }

            Button(
              onClick = {
                if (importText.isNotBlank()) {
                  val success = onImportBackup(importText)
                  if (success) {
                    isEditingImport = false
                  }
                }
              },
              modifier = Modifier.weight(1f).testTag("confirm_import_button"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = NeonEmerald,
                contentColor = CyberBlack
              )
            ) {
              Text("SAVE & LOAD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reset to Factory Embedded
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          OutlinedButton(
            onClick = {
              onResetDefaults()
              onDismiss()
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = CyberAmber
            ),
            modifier = Modifier.testTag("reset_defaults_button")
          ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("RESET TO EMBEDDED PRESETS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
