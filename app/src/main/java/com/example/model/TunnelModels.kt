package com.example.model

enum class TunnelState {
  DISCONNECTED,
  PROBING,
  CONNECTING,
  CONNECTED,
  DISCONNECTING
}

data class TunnelNode(
  val id: String,
  val name: String,
  val countryCode: String,
  val countryName: String,
  val flagEmoji: String,
  val city: String,
  val endpointHost: String,
  val endpointPort: Int,
  val ipAddress: String,
  val publicKey: String,
  val clientPrivateKey: String,
  val clientAddress: String,
  val dns: String = "1.1.1.1, 1.0.0.1",
  val mtu: Int = 1280,
  val latencyMs: Long = -1L,
  val isHealthy: Boolean = false,
  val isProbing: Boolean = false,
  val protocol: String = "WireGuard 0-Trust",
  val cipher: String = "ChaCha20-Poly1305",
  val tag: String = "Ultra Stealth"
) {
  val formattedEndpoint: String
    get() = "$endpointHost:$endpointPort"

  val latencyDisplay: String
    get() = when {
      isProbing -> "Probing…"
      latencyMs > 0 -> "${latencyMs}ms"
      else -> "Offline"
    }

  fun toWireGuardConfig(): String {
    return """
      |[Interface]
      |PrivateKey = $clientPrivateKey
      |Address = $clientAddress
      |DNS = $dns
      |MTU = $mtu
      |
      |[Peer]
      |PublicKey = $publicKey
      |Endpoint = $formattedEndpoint
      |AllowedIPs = 0.0.0.0/0, ::/0
      |PersistentKeepalive = 25
    """.trimMargin()
  }
}

data class TelemetryStats(
  val uploadSpeedKbps: Float = 0f,
  val downloadSpeedKbps: Float = 0f,
  val totalUploadedBytes: Long = 0L,
  val totalDownloadedBytes: Long = 0L,
  val sessionDurationSeconds: Long = 0L
) {
  fun formatDuration(): String {
    val hrs = sessionDurationSeconds / 3600
    val mins = (sessionDurationSeconds % 3600) / 60
    val secs = sessionDurationSeconds % 60
    return if (hrs > 0) {
      String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
      String.format("%02d:%02d", mins, secs)
    }
  }

  fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
      gb >= 1.0 -> String.format("%.2f GB", gb)
      mb >= 1.0 -> String.format("%.1f MB", mb)
      kb >= 1.0 -> String.format("%.0f KB", kb)
      else -> "$bytes B"
    }
  }

  fun formatSpeed(kbps: Float): String {
    val mbps = kbps / 1024f
    return if (mbps >= 1.0f) {
      String.format("%.1f Mbps", mbps)
    } else {
      String.format("%.0f Kbps", kbps)
    }
  }
}
