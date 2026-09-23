package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.TelemetryStats
import com.example.model.TunnelNode
import com.example.model.TunnelState
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

class ShenTunnelVpnService : Service() {

  companion object {
    const val ACTION_START = "com.shen.tunnel.zero.action.SHEN_VPN_START"
    const val ACTION_STOP = "com.shen.tunnel.zero.action.SHEN_VPN_STOP"
    const val EXTRA_CONFIG_TEXT = "com.shen.tunnel.zero.extra.CONFIG_TEXT"
    const val EXTRA_NODE_NAME = "com.shen.tunnel.zero.extra.NODE_NAME"
    const val EXTRA_FLAG_EMOJI = "com.shen.tunnel.zero.extra.FLAG_EMOJI"
    const val EXTRA_ENDPOINT = "com.shen.tunnel.zero.extra.ENDPOINT"

    private const val NOTIFICATION_CHANNEL_ID = "shen_tunnel_vpn_channel"
    private const val NOTIFICATION_ID = 1001

    private val _tunnelState = MutableStateFlow(TunnelState.DISCONNECTED)
    val tunnelState = _tunnelState.asStateFlow()

    private val _activeNode = MutableStateFlow<TunnelNode?>(null)
    val activeNode = _activeNode.asStateFlow()

    private val _telemetry = MutableStateFlow(TelemetryStats())
    val telemetry = _telemetry.asStateFlow()

    fun startService(context: Context, node: TunnelNode) {
      _activeNode.value = node
      val intent = Intent(context, ShenTunnelVpnService::class.java).apply {
        action = ACTION_START
        putExtra(EXTRA_CONFIG_TEXT, node.toWireGuardConfig())
        putExtra(EXTRA_NODE_NAME, node.name)
        putExtra(EXTRA_FLAG_EMOJI, node.flagEmoji)
        putExtra(EXTRA_ENDPOINT, node.formattedEndpoint)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stopService(context: Context) {
      val intent = Intent(context, ShenTunnelVpnService::class.java).apply {
        action = ACTION_STOP
      }
      context.startService(intent)
    }
  }

  private var backend: Backend? = null
  private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
  private var telemetryJob: Job? = null

  private val wireGuardTunnel = object : Tunnel {
    override fun getName(): String = "SHENZeroTunnel"
    override fun onStateChange(state: Tunnel.State) {
      when (state) {
        Tunnel.State.UP -> {
          _tunnelState.value = TunnelState.CONNECTED
          startTelemetry()
        }
        Tunnel.State.DOWN -> {
          _tunnelState.value = TunnelState.DISCONNECTED
          telemetryJob?.cancel()
          stopForeground(STOP_FOREGROUND_REMOVE)
          stopSelf()
        }
        Tunnel.State.TOGGLE -> {}
      }
    }
  }

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    try {
      backend = GoBackend(applicationContext)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START -> {
        val configText = intent.getStringExtra(EXTRA_CONFIG_TEXT) ?: ""
        val nodeName = intent.getStringExtra(EXTRA_NODE_NAME) ?: "SHΞN Zero Node"
        val flagEmoji = intent.getStringExtra(EXTRA_FLAG_EMOJI) ?: "🛡️"
        val endpoint = intent.getStringExtra(EXTRA_ENDPOINT) ?: ""

        val connectingNotification = buildNotification(nodeName, flagEmoji, "Establishing WireGuard kernel tunnel...")
        safeStartForeground(connectingNotification)

        startWireGuardTunnel(configText, nodeName, flagEmoji, endpoint)
      }
      ACTION_STOP -> {
        stopWireGuardTunnel()
      }
    }
    return START_STICKY
  }

  private fun startWireGuardTunnel(
    configText: String,
    nodeName: String,
    flagEmoji: String,
    endpoint: String
  ) {
    serviceScope.launch(Dispatchers.IO) {
      try {
        _tunnelState.value = TunnelState.CONNECTING
        if (backend == null) {
          backend = GoBackend(applicationContext)
        }

        val config = Config.parse(ByteArrayInputStream(configText.toByteArray()))
        backend?.setState(wireGuardTunnel, Tunnel.State.UP, config)

        _tunnelState.value = TunnelState.CONNECTED
        val connectedNotification = buildNotification(nodeName, flagEmoji, "Connected: $endpoint")
        safeStartForeground(connectedNotification)
        startTelemetry()
      } catch (e: Exception) {
        e.printStackTrace()
        _tunnelState.value = TunnelState.DISCONNECTED
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
      }
    }
  }

  private fun stopWireGuardTunnel() {
    serviceScope.launch(Dispatchers.IO) {
      try {
        _tunnelState.value = TunnelState.DISCONNECTING
        backend?.setState(wireGuardTunnel, Tunnel.State.DOWN, null)
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        _tunnelState.value = TunnelState.DISCONNECTED
        telemetryJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
      }
    }
  }

  private fun safeStartForeground(notification: Notification) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startForeground(
          NOTIFICATION_ID,
          notification,
          android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
        )
      } else {
        startForeground(NOTIFICATION_ID, notification)
      }
    } catch (_: Exception) {
      try {
        startForeground(NOTIFICATION_ID, notification)
      } catch (_: Exception) {}
    }
  }

  private fun startTelemetry() {
    telemetryJob?.cancel()
    telemetryJob = serviceScope.launch {
      var seconds = 0L
      while (isActive && _tunnelState.value == TunnelState.CONNECTED) {
        delay(1000)
        seconds++
        val stats = try {
          backend?.getStatistics(wireGuardTunnel)
        } catch (_: Exception) {
          null
        }

        val rx = stats?.totalRx() ?: 0L
        val tx = stats?.totalTx() ?: 0L

        val durationString = String.format(
          "%02d:%02d:%02d",
          seconds / 3600,
          (seconds % 3600) / 60,
          seconds % 60
        )

        _telemetry.value = TelemetryStats(
          uptimeSeconds = seconds,
          durationFormatted = durationString,
          downloadBytes = rx,
          uploadBytes = tx,
          activeHandshake = (rx > 0 || tx > 0)
        )
      }
    }
  }

  private fun buildNotification(name: String, flag: String, statusText: String): Notification {
    val openIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      this,
      0,
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_vpn_key)
      .setContentTitle("SHΞN™ ᴢᴇʀᴏ [$flag $name]")
      .setContentText(statusText)
      .setContentIntent(pendingIntent)
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        "SHΞN WireGuard Zero-Trust Tunnel",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Active WireGuard native protocol connection status"
        setShowBadge(false)
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    try {
      backend?.setState(wireGuardTunnel, Tunnel.State.DOWN, null)
    } catch (_: Exception) {}
    telemetryJob?.cancel()
  }
}
