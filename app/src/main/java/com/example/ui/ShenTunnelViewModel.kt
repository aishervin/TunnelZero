package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EmbeddedConfigRepository
import com.example.model.TelemetryStats
import com.example.model.TunnelNode
import com.example.model.TunnelState
import com.example.service.ShenTunnelVpnService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShenTunnelViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = EmbeddedConfigRepository(application)

  private val _nodes = MutableStateFlow<List<TunnelNode>>(emptyList())
  val nodes: StateFlow<List<TunnelNode>> = _nodes.asStateFlow()

  private val _selectedNode = MutableStateFlow<TunnelNode?>(null)
  val selectedNode: StateFlow<TunnelNode?> = _selectedNode.asStateFlow()

  private val _isProbing = MutableStateFlow(false)
  val isProbing: StateFlow<Boolean> = _isProbing.asStateFlow()

  private val _showBackupDialog = MutableStateFlow(false)
  val showBackupDialog: StateFlow<Boolean> = _showBackupDialog.asStateFlow()

  private val _showNodeSelector = MutableStateFlow(false)
  val showNodeSelector: StateFlow<Boolean> = _showNodeSelector.asStateFlow()

  private val _statusNotice = MutableStateFlow<String?>(null)
  val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

  val tunnelState: StateFlow<TunnelState> = ShenTunnelVpnService.tunnelState
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TunnelState.DISCONNECTED)

  val activeNode: StateFlow<TunnelNode?> = ShenTunnelVpnService.activeNode
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val telemetry: StateFlow<TelemetryStats> = ShenTunnelVpnService.telemetry
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TelemetryStats())

  init {
    loadNodesAndProbe()
  }

  fun loadNodesAndProbe() {
    viewModelScope.launch {
      val loaded = repository.getNodes()
      _nodes.value = loaded
      if (_selectedNode.value == null && loaded.isNotEmpty()) {
        _selectedNode.value = loaded.first()
      }
      probeAllNodes()
    }
  }

  fun selectNode(node: TunnelNode) {
    _selectedNode.value = node
    _showNodeSelector.value = false
  }

  fun setShowBackupDialog(show: Boolean) {
    _showBackupDialog.value = show
  }

  fun setShowNodeSelector(show: Boolean) {
    _showNodeSelector.value = show
  }

  fun clearNotice() {
    _statusNotice.value = null
  }

  fun probeAllNodes() {
    if (_isProbing.value) return
    viewModelScope.launch {
      _isProbing.value = true
      _statusNotice.value = "Scanning & testing all secure nodes…"

      val currentList = _nodes.value
      val deferreds = currentList.map { node ->
        async {
          val latency = repository.probeNode(node)
          node.copy(
            latencyMs = latency,
            isHealthy = latency > 0,
            isProbing = false
          )
        }
      }

      val tested = deferreds.awaitAll()
      _nodes.value = tested
      repository.saveNodes(tested)

      // Keep selected node updated
      _selectedNode.value?.let { current ->
        val updatedSelected = tested.find { it.id == current.id }
        if (updatedSelected != null) {
          _selectedNode.value = updatedSelected
        } else {
          _selectedNode.value = tested.firstOrNull { it.isHealthy } ?: tested.firstOrNull()
        }
      } ?: run {
        _selectedNode.value = tested.firstOrNull { it.isHealthy } ?: tested.firstOrNull()
      }

      _isProbing.value = false
      _statusNotice.value = "Probe complete: ${tested.count { it.isHealthy }}/${tested.size} nodes verified"
    }
  }

  fun handleConnectClick(
    context: Context,
    prepareLauncher: ActivityResultLauncher<Intent>
  ) {
    if (tunnelState.value == TunnelState.CONNECTED || tunnelState.value == TunnelState.CONNECTING) {
      ShenTunnelVpnService.stopService(context)
      return
    }

    val targetNode = _selectedNode.value ?: _nodes.value.firstOrNull()
    if (targetNode == null) {
      _statusNotice.value = "No nodes available. Please reset or import backup."
      return
    }

    val prepareIntent = VpnService.prepare(context)
    if (prepareIntent != null) {
      prepareLauncher.launch(prepareIntent)
    } else {
      startVpn(context, targetNode)
    }
  }

  fun onVpnPermissionGranted(context: Context) {
    val targetNode = _selectedNode.value ?: _nodes.value.firstOrNull() ?: return
    startVpn(context, targetNode)
  }

  private fun startVpn(context: Context, node: TunnelNode) {
    ShenTunnelVpnService.startService(context, node)
  }

  fun importBackupConfig(raw: String): Boolean {
    val imported = repository.importBackupConfig(raw)
    return if (imported != null) {
      val updated = listOf(imported) + _nodes.value.filter { it.id != imported.id }
      _nodes.value = updated
      _selectedNode.value = imported
      repository.saveNodes(updated)
      _statusNotice.value = "Backup imported successfully: ${imported.name}"
      true
    } else {
      _statusNotice.value = "Invalid configuration format. Must be WireGuard .conf or JSON."
      false
    }
  }

  fun resetToEmbeddedBackup() {
    val defaults = repository.resetToDefaults()
    _nodes.value = defaults
    _selectedNode.value = defaults.firstOrNull()
    _statusNotice.value = "Restored default zero-trust embedded nodes"
    probeAllNodes()
  }

  fun getBackupExportString(): String {
    val current = _selectedNode.value ?: _nodes.value.firstOrNull()
    return current?.toWireGuardConfig() ?: "# No active config"
  }
}
