package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ShenTunnelViewModel
import com.example.ui.components.BackupConfigDialog
import com.example.ui.components.HeaderBranding
import com.example.ui.components.NodeSelectionSheet
import com.example.ui.components.SelectedNodeCard
import com.example.ui.components.TelemetryHud
import com.example.ui.components.TunnelPowerSwitch
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan

class MainActivity : ComponentActivity() {

  private val viewModel: ShenTunnelViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme(darkTheme = true) {
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }

        val tunnelState by viewModel.tunnelState.collectAsState()
        val nodes by viewModel.nodes.collectAsState()
        val selectedNode by viewModel.selectedNode.collectAsState()
        val isProbing by viewModel.isProbing.collectAsState()
        val telemetry by viewModel.telemetry.collectAsState()
        val showBackupDialog by viewModel.showBackupDialog.collectAsState()
        val showNodeSelector by viewModel.showNodeSelector.collectAsState()
        val statusNotice by viewModel.statusNotice.collectAsState()

        val vpnPrepareLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
          if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onVpnPermissionGranted(context)
          }
        }

        LaunchedEffect(statusNotice) {
          statusNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotice()
          }
        }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = CyberBlack,
          snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(CyberBlack)
              .statusBarsPadding()
              .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // Top Bar / Branding
              HeaderBranding(
                tunnelState = tunnelState,
                isProbing = isProbing,
                onScanClick = { viewModel.probeAllNodes() },
                onConfigClick = { viewModel.setShowBackupDialog(true) }
              )

              // Notice Banner if active
              AnimatedVisibility(
                visible = isProbing,
                enter = fadeIn(),
                exit = fadeOut()
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E283C))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Info,
                      contentDescription = null,
                      tint = NeonCyan,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "Probing node reachability & cryptographic health…",
                      color = CyberTextPrimary,
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Central High-Tech Power Switch
              TunnelPowerSwitch(
                tunnelState = tunnelState,
                isProbing = isProbing,
                onClick = {
                  viewModel.handleConnectClick(context, vpnPrepareLauncher)
                }
              )

              Spacer(modifier = Modifier.height(24.dp))

              // Active Node Selector Card & Quick Buttons (🇨🇦 Canada, 🇩🇪 Germany, 🇺🇸 USA, 🇮🇱 Israel, etc.)
              SelectedNodeCard(
                selectedNode = selectedNode,
                allNodes = nodes,
                onSelectNode = { viewModel.selectNode(it) },
                onOpenAllNodes = { viewModel.setShowNodeSelector(true) },
                modifier = Modifier.padding(horizontal = 20.dp)
              )

              Spacer(modifier = Modifier.height(20.dp))

              // Live Telemetry HUD (Speeds, Uptime, Transfer Counters, WireGuard specs)
              TelemetryHud(
                telemetry = telemetry,
                tunnelState = tunnelState,
                modifier = Modifier.padding(horizontal = 20.dp)
              )

              Spacer(modifier = Modifier.height(24.dp))

              // Footer Branding Watermark
              Text(
                text = "☬ Exclusive SHΞN™ made",
                color = CyberTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 24.dp)
              )
            }

            // Node Selection Bottom Sheet
            if (showNodeSelector) {
              NodeSelectionSheet(
                nodes = nodes,
                selectedNode = selectedNode,
                isProbing = isProbing,
                onSelectNode = { viewModel.selectNode(it) },
                onScanAll = { viewModel.probeAllNodes() },
                onDismiss = { viewModel.setShowNodeSelector(false) }
              )
            }

            // WireGuard Backup & Custom Import Modal
            if (showBackupDialog) {
              BackupConfigDialog(
                selectedNode = selectedNode,
                rawConfig = viewModel.getBackupExportString(),
                onImportBackup = { raw -> viewModel.importBackupConfig(raw) },
                onResetDefaults = { viewModel.resetToEmbeddedBackup() },
                onDismiss = { viewModel.setShowBackupDialog(false) }
              )
            }
          }
        }
      }
    }
  }
}
