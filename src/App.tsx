import React, { useState, useEffect } from 'react';
import {
  Shield,
  Power,
  RefreshCw,
  FileCode,
  ArrowDown,
  ArrowUp,
  Clock,
  HardDrive,
  Lock,
  Key,
  CheckCircle,
  Copy,
  Terminal,
  Server,
  Activity,
  Check,
  Globe
} from 'lucide-react';

interface TunnelNode {
  id: string;
  name: string;
  countryCode: string;
  countryName: string;
  flagEmoji: string;
  city: string;
  endpoint: string;
  ip: string;
  latencyMs: number;
  isHealthy: boolean;
  tag: string;
  publicKey: string;
  clientAddress: string;
}

const DEFAULT_NODES: TunnelNode[] = [
  {
    id: 'ca-01',
    name: 'SHΞN CA-East #01',
    countryCode: 'CA',
    countryName: 'Canada',
    flagEmoji: '🇨🇦',
    city: 'Montreal',
    endpoint: '198.51.100.42:51820',
    ip: '198.51.100.42',
    latencyMs: 38,
    isHealthy: true,
    tag: 'Stealth P2P',
    publicKey: 'k9H2L0qO7M+W9v1c+P2eR8tY5uI3oA4sD6fG7hJ8kL=',
    clientAddress: '10.66.66.2/32'
  },
  {
    id: 'de-02',
    name: 'SHΞN DE-Central #02',
    countryCode: 'DE',
    countryName: 'Germany',
    flagEmoji: '🇩🇪',
    city: 'Frankfurt',
    endpoint: '185.120.44.18:51820',
    ip: '185.120.44.18',
    latencyMs: 26,
    isHealthy: true,
    tag: 'Low Latency',
    publicKey: 'm8P1Q3rT5yU7iO9pA1sD3fG5hJ7kL9zX1cV3bN5mQ8=',
    clientAddress: '10.66.66.3/32'
  },
  {
    id: 'us-03',
    name: 'SHΞN US-Atlantic #03',
    countryCode: 'US',
    countryName: 'United States',
    flagEmoji: '🇺🇸',
    city: 'Miami',
    endpoint: '104.28.19.82:51820',
    ip: '104.28.19.82',
    latencyMs: 52,
    isHealthy: true,
    tag: 'Stream Fast',
    publicKey: 'r4T6yU8iO0pA2sD4fG6hJ8kL0zX2cV4bN6mQ8wE0rT=',
    clientAddress: '10.66.66.4/32'
  },
  {
    id: 'il-04',
    name: 'SHΞN IL-Coast #04',
    countryCode: 'IL',
    countryName: 'Israel',
    flagEmoji: '🇮🇱',
    city: 'Tel Aviv',
    endpoint: '185.220.101.5:51820',
    ip: '185.220.101.5',
    latencyMs: 45,
    isHealthy: true,
    tag: 'Zero-Censorship',
    publicKey: 'y7U9iO1pA3sD5fG7hJ9kL1zX3cV5bN7mQ9wE1rT3yU=',
    clientAddress: '10.66.66.5/32'
  },
  {
    id: 'nl-05',
    name: 'SHΞN NL-EuroGate #05',
    countryCode: 'NL',
    countryName: 'Netherlands',
    flagEmoji: '🇳🇱',
    city: 'Amsterdam',
    endpoint: '194.36.191.22:51820',
    ip: '194.36.191.22',
    latencyMs: 29,
    isHealthy: true,
    tag: 'Privacy Haven',
    publicKey: 'p0O2iU4yT6rE8wQ0mN2bV4cX6zL8kI0jH2gF4dD6sA=',
    clientAddress: '10.66.66.6/32'
  },
  {
    id: 'fi-06',
    name: 'SHΞN FI-Nordic #06',
    countryCode: 'FI',
    countryName: 'Finland',
    flagEmoji: '🇫🇮',
    city: 'Helsinki',
    endpoint: '95.217.34.80:51820',
    ip: '95.217.34.80',
    latencyMs: 34,
    isHealthy: true,
    tag: 'Shielded Core',
    publicKey: 't3Y5uI7oP9aS1dF3gH5jK7lZ9xC1vB3nM5qW7eR9tY=',
    clientAddress: '10.66.66.7/32'
  },
  {
    id: 'gb-07',
    name: 'SHΞN GB-Thames #07',
    countryCode: 'GB',
    countryName: 'United Kingdom',
    flagEmoji: '🇬🇧',
    city: 'London',
    endpoint: '45.83.220.10:51820',
    ip: '45.83.220.10',
    latencyMs: 31,
    isHealthy: true,
    tag: 'High Bandwidth',
    publicKey: 'u7I9oP1aS3dF5gH7jK9lZ1xC3vB5nM7qW9eR1tY3uI=',
    clientAddress: '10.66.66.8/32'
  },
  {
    id: 'jp-08',
    name: 'SHΞN JP-Apex #08',
    countryCode: 'JP',
    countryName: 'Japan',
    flagEmoji: '🇯🇵',
    city: 'Tokyo',
    endpoint: '133.242.18.99:51820',
    ip: '133.242.18.99',
    latencyMs: 82,
    isHealthy: true,
    tag: 'Asia Gateway',
    publicKey: 'a2S4dF6gH8jK0lZ2xC4vB6nM8qW0eR2tY4uI6oP8aS=',
    clientAddress: '10.66.66.9/32'
  }
];

export default function App() {
  const [nodes, setNodes] = useState<TunnelNode[]>(DEFAULT_NODES);
  const [selectedNode, setSelectedNode] = useState<TunnelNode>(DEFAULT_NODES[0]);
  const [tunnelState, setTunnelState] = useState<'DISCONNECTED' | 'CONNECTING' | 'CONNECTED'>('DISCONNECTED');
  const [isScanning, setIsScanning] = useState(false);
  const [showConfigModal, setShowConfigModal] = useState(false);
  const [showGithubDoc, setShowGithubDoc] = useState(false);
  const [uptimeSeconds, setUptimeSeconds] = useState(0);
  const [upSpeed, setUpSpeed] = useState(0);
  const [downSpeed, setDownSpeed] = useState(0);
  const [totalUp, setTotalUp] = useState(128000);
  const [totalDown, setTotalDown] = useState(940000);
  const [copied, setCopied] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Live telemetry timer when connected
  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (tunnelState === 'CONNECTED') {
      timer = setInterval(() => {
        setUptimeSeconds(prev => prev + 1);
        const newUp = Math.floor(Math.random() * 1200 + 400);
        const newDown = Math.floor(Math.random() * 5800 + 1500);
        setUpSpeed(newUp);
        setDownSpeed(newDown);
        setTotalUp(prev => prev + newUp * 128);
        setTotalDown(prev => prev + newDown * 128);
      }, 1000);
    } else {
      setUpSpeed(0);
      setDownSpeed(0);
    }
    return () => clearInterval(timer);
  }, [tunnelState]);

  const handleToggleTunnel = () => {
    if (tunnelState === 'CONNECTED') {
      setTunnelState('DISCONNECTED');
      setUptimeSeconds(0);
    } else {
      setTunnelState('CONNECTING');
      setTimeout(() => {
        setTunnelState('CONNECTED');
      }, 1200);
    }
  };

  const handleScanNodes = () => {
    setIsScanning(true);
    setTimeout(() => {
      setNodes(prev =>
        prev.map(node => ({
          ...node,
          latencyMs: Math.max(18, node.latencyMs + Math.floor(Math.random() * 8 - 4)),
          isHealthy: true
        }))
      );
      setIsScanning(false);
    }, 900);
  };

  const formatTime = (secs: number) => {
    const h = Math.floor(secs / 3600);
    const m = Math.floor((secs % 3600) / 60);
    const s = secs % 60;
    return `${h > 0 ? `${h.toString().padStart(2, '0')}:` : ''}${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const formatBytes = (bytes: number) => {
    const mb = bytes / (1024 * 1024);
    if (mb > 1024) {
      return `${(mb / 1024).toFixed(2)} GB`;
    }
    return `${mb.toFixed(1)} MB`;
  };

  const getWireGuardConfigText = (node: TunnelNode) => {
    return `[Interface]
PrivateKey = eE3rT5yU7iO9pA1sD3fG5hJ7kL9zX1cV3bN5mQ7wE9=
Address = ${node.clientAddress}
DNS = 1.1.1.1, 1.0.0.1
MTU = 1280

[Peer]
PublicKey = ${node.publicKey}
Endpoint = ${node.endpoint}
AllowedIPs = 0.0.0.0/0, ::/0
PersistentKeepalive = 25`;
  };

  const filteredNodes = nodes.filter(n =>
    n.countryName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    n.city.toLowerCase().includes(searchQuery.toLowerCase()) ||
    n.ip.includes(searchQuery)
  );

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#07090e', color: '#f1f5f9', display: 'flex', flexDirection: 'column' }}>
      {/* Top Navbar */}
      <header style={{ borderBottom: '1px solid #161e2e', backgroundColor: '#0a0e17', padding: '14px 20px' }}>
        <div style={{ maxWidth: '720px', margin: '0 auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '12px',
              backgroundColor: '#101623',
              border: `1px solid ${tunnelState === 'CONNECTED' ? '#00f5a0' : '#1e293b'}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <Shield size={22} color={tunnelState === 'CONNECTED' ? '#00f5a0' : '#00d9f5'} />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ color: '#00f5a0', fontWeight: 900, fontSize: '18px', letterSpacing: '1px' }}>SHΞN™</span>
                <span style={{ color: '#f1f5f9', fontWeight: 700, fontSize: '18px' }}>tunnel</span>
                <span style={{ color: '#00d9f5', fontWeight: 700, fontSize: '15px', letterSpacing: '2px' }}>ᴢᴇʀᴏ</span>
              </div>
              <div style={{ color: '#64748b', fontSize: '11px', fontFamily: 'monospace' }}>
                Zero-Trust WireGuard Stealth Engine
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <button
              onClick={handleScanNodes}
              disabled={isScanning}
              style={{
                backgroundColor: '#101623',
                border: '1px solid #1e293b',
                color: isScanning ? '#fbbf24' : '#94a3b8',
                padding: '8px 12px',
                borderRadius: '8px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                fontSize: '12px',
                fontWeight: 600
              }}
            >
              <RefreshCw size={15} className={isScanning ? 'spin' : ''} />
              <span>{isScanning ? 'Probing…' : 'Ping Nodes'}</span>
            </button>

            <button
              onClick={() => setShowConfigModal(true)}
              style={{
                backgroundColor: '#101623',
                border: '1px solid #1e293b',
                color: '#38bdf8',
                padding: '8px 12px',
                borderRadius: '8px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                fontSize: '12px',
                fontWeight: 600
              }}
            >
              <FileCode size={15} />
              <span>Config</span>
            </button>

            <button
              onClick={() => setShowGithubDoc(!showGithubDoc)}
              style={{
                backgroundColor: '#101623',
                border: '1px solid #1e293b',
                color: '#e2e8f0',
                padding: '8px 12px',
                borderRadius: '8px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                fontSize: '12px',
                fontWeight: 600
              }}
            >
              <Terminal size={15} />
              <span>CI / Keys</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main style={{ maxWidth: '720px', width: '100%', margin: '0 auto', padding: '24px 20px', flex: 1 }}>
        {/* GitHub & Keystore Banner */}
        {showGithubDoc && (
          <div style={{
            backgroundColor: '#0d131f',
            border: '1px solid #1e293b',
            borderRadius: '16px',
            padding: '18px',
            marginBottom: '24px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Terminal size={18} color="#00f5a0" />
                <span style={{ fontWeight: 800, fontSize: '14px', letterSpacing: '0.5px' }}>
                  GOOGLE-COMPLIANT PERMANENT SIGNATURE SETUP
                </span>
              </div>
              <button
                onClick={() => setShowGithubDoc(false)}
                style={{ background: 'transparent', border: 'none', color: '#64748b', cursor: 'pointer', fontSize: '13px' }}
              >
                ✕ Close
              </button>
            </div>
            <p style={{ color: '#94a3b8', fontSize: '12px', lineHeight: '1.6', marginBottom: '12px' }}>
              To ensure all future GitHub Actions builds retain an identical permanent signature without certificate collision or uninstall requirements, run the generator script once:
            </p>
            <div style={{
              backgroundColor: '#07090e',
              border: '1px solid #171f2e',
              padding: '10px 14px',
              borderRadius: '8px',
              fontFamily: 'monospace',
              fontSize: '12px',
              color: '#38bdf8',
              marginBottom: '10px'
            }}>
              bash ./scripts/generate_permanent_keystore.sh
            </div>
            <p style={{ color: '#64748b', fontSize: '11px' }}>
              Workflow configured at: <code>.github/workflows/build-apk.yml</code>. Automatically verifies v1, v2, v3, v4 signature schemes before releasing the signed APK.
            </p>
          </div>
        )}

        {/* Central Power Switch */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', margin: '30px 0' }}>
          <div
            onClick={handleToggleTunnel}
            style={{
              width: '180px',
              height: '180px',
              borderRadius: '50%',
              backgroundColor: '#0c111c',
              border: `3px solid ${
                tunnelState === 'CONNECTED'
                  ? '#00f5a0'
                  : tunnelState === 'CONNECTING'
                  ? '#fbbf24'
                  : '#1e293b'
              }`,
              boxShadow: tunnelState === 'CONNECTED'
                ? '0 0 50px rgba(0, 245, 160, 0.35), inset 0 0 25px rgba(0, 245, 160, 0.2)'
                : '0 8px 30px rgba(0, 0, 0, 0.7)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              position: 'relative'
            }}
          >
            <Power
              size={56}
              color={
                tunnelState === 'CONNECTED'
                  ? '#00f5a0'
                  : tunnelState === 'CONNECTING'
                  ? '#fbbf24'
                  : '#475569'
              }
            />
            <span style={{
              marginTop: '8px',
              fontSize: '13px',
              fontWeight: 900,
              letterSpacing: '2px',
              fontFamily: 'monospace',
              color: tunnelState === 'CONNECTED' ? '#00f5a0' : '#94a3b8'
            }}>
              {tunnelState === 'CONNECTED' ? 'ACTIVE' : tunnelState === 'CONNECTING' ? 'LINKING…' : 'CONNECT'}
            </span>
          </div>

          <div style={{ marginTop: '18px', textAlign: 'center' }}>
            <div style={{
              fontSize: '14px',
              fontWeight: 700,
              color: tunnelState === 'CONNECTED' ? '#00f5a0' : '#f1f5f9'
            }}>
              {tunnelState === 'CONNECTED'
                ? 'ZERO-TRUST TUNNEL SHIELDED'
                : tunnelState === 'CONNECTING'
                ? 'ESTABLISHING WIRELESS TUNNEL…'
                : 'TAP TO INITIATE ENCRYPTED TUNNEL'}
            </div>
            <div style={{ color: '#64748b', fontSize: '11px', fontFamily: 'monospace', marginTop: '4px' }}>
              ChaCha20-Poly1305 · DNS Leak Guarded (1.1.1.1)
            </div>
          </div>
        </div>

        {/* Selected Relay Node Card */}
        <div style={{
          backgroundColor: '#101623',
          border: '1px solid #1e293b',
          borderRadius: '16px',
          padding: '16px',
          marginBottom: '20px'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
              <div style={{
                width: '46px',
                height: '46px',
                borderRadius: '12px',
                backgroundColor: '#171f2f',
                border: '1px solid #243048',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '24px'
              }}>
                {selectedNode.flagEmoji}
              </div>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span style={{ fontWeight: 800, fontSize: '16px', color: '#f1f5f9' }}>
                    {selectedNode.countryName}
                  </span>
                  <span style={{ color: '#94a3b8', fontSize: '13px' }}>· {selectedNode.city}</span>
                </div>
                <div style={{ color: '#64748b', fontSize: '11px', fontFamily: 'monospace', marginTop: '3px' }}>
                  IP: {selectedNode.ip} · {selectedNode.tag}
                </div>
              </div>
            </div>

            <div style={{
              padding: '6px 12px',
              borderRadius: '8px',
              backgroundColor: 'rgba(0, 245, 160, 0.1)',
              border: '1px solid rgba(0, 245, 160, 0.3)',
              color: '#00f5a0',
              fontWeight: 800,
              fontSize: '12px',
              fontFamily: 'monospace'
            }}>
              ● {selectedNode.latencyMs}ms
            </div>
          </div>

          {/* Quick Country Switcher */}
          <div style={{ marginTop: '16px', borderTop: '1px solid #182234', paddingTop: '14px' }}>
            <div style={{ color: '#64748b', fontSize: '10px', fontWeight: 800, letterSpacing: '1px', fontFamily: 'monospace', marginBottom: '8px' }}>
              QUICK RELAY NODES (INTERNAL REPOSITORY)
            </div>
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              {nodes.map(node => {
                const isCurrent = selectedNode.id === node.id;
                return (
                  <button
                    key={node.id}
                    onClick={() => setSelectedNode(node)}
                    style={{
                      backgroundColor: isCurrent ? '#09271c' : '#141c2c',
                      border: `1px solid ${isCurrent ? '#00f5a0' : '#1e293b'}`,
                      color: isCurrent ? '#00f5a0' : '#cbd5e1',
                      padding: '7px 12px',
                      borderRadius: '10px',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      fontSize: '12px',
                      fontWeight: isCurrent ? 700 : 500,
                      transition: 'all 0.15s ease'
                    }}
                  >
                    <span>{node.flagEmoji}</span>
                    <span>{node.countryName}</span>
                    <span style={{ fontSize: '10px', color: isCurrent ? '#00f5a0' : '#64748b', fontFamily: 'monospace' }}>
                      {node.latencyMs}ms
                    </span>
                  </button>
                );
              })}
            </div>
          </div>
        </div>

        {/* Telemetry HUD */}
        <div style={{
          backgroundColor: '#101623',
          border: '1px solid #1e293b',
          borderRadius: '16px',
          padding: '16px',
          marginBottom: '24px'
        }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '12px' }}>
            <div style={{ backgroundColor: '#090e18', border: '1px solid #172033', padding: '12px', borderRadius: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b', fontSize: '11px', fontFamily: 'monospace', fontWeight: 700 }}>
                <ArrowDown size={14} color="#00d9f5" />
                <span>DOWNLOAD</span>
              </div>
              <div style={{ fontSize: '18px', fontWeight: 900, color: '#f1f5f9', fontFamily: 'monospace', marginTop: '6px' }}>
                {tunnelState === 'CONNECTED' ? `${(downSpeed / 1024).toFixed(1)} Mbps` : '0.0 Kbps'}
              </div>
            </div>

            <div style={{ backgroundColor: '#090e18', border: '1px solid #172033', padding: '12px', borderRadius: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b', fontSize: '11px', fontFamily: 'monospace', fontWeight: 700 }}>
                <ArrowUp size={14} color="#00f5a0" />
                <span>UPLOAD</span>
              </div>
              <div style={{ fontSize: '18px', fontWeight: 900, color: '#f1f5f9', fontFamily: 'monospace', marginTop: '6px' }}>
                {tunnelState === 'CONNECTED' ? `${(upSpeed / 1024).toFixed(1)} Mbps` : '0.0 Kbps'}
              </div>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div style={{ backgroundColor: '#090e18', border: '1px solid #172033', padding: '12px', borderRadius: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b', fontSize: '11px', fontFamily: 'monospace', fontWeight: 700 }}>
                <Clock size={14} color="#38bdf8" />
                <span>SESSION TIME</span>
              </div>
              <div style={{ fontSize: '18px', fontWeight: 900, color: '#f1f5f9', fontFamily: 'monospace', marginTop: '6px' }}>
                {formatTime(uptimeSeconds)}
              </div>
            </div>

            <div style={{ backgroundColor: '#090e18', border: '1px solid #172033', padding: '12px', borderRadius: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b', fontSize: '11px', fontFamily: 'monospace', fontWeight: 700 }}>
                <HardDrive size={14} color="#a855f7" />
                <span>TRAFFIC ROUTED</span>
              </div>
              <div style={{ fontSize: '18px', fontWeight: 900, color: '#f1f5f9', fontFamily: 'monospace', marginTop: '6px' }}>
                {tunnelState === 'CONNECTED' ? formatBytes(totalUp + totalDown) : '0 B'}
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div style={{ textAlign: 'center', padding: '12px 0 24px 0', color: '#475569', fontSize: '12px', fontFamily: 'monospace' }}>
          ☬ Exclusive SHΞN™ made
        </div>
      </main>

      {/* WireGuard Backup Modal */}
      {showConfigModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '20px',
          zIndex: 100
        }}>
          <div style={{
            backgroundColor: '#0d131f',
            border: '1px solid #1e293b',
            borderRadius: '16px',
            maxWidth: '560px',
            width: '100%',
            padding: '22px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <div>
                <div style={{ fontWeight: 800, fontSize: '15px', color: '#00d9f5', fontFamily: 'monospace' }}>
                  WIREGUARD INTERNAL BACKUP CONFIG
                </div>
                <div style={{ color: '#94a3b8', fontSize: '11px' }}>
                  Embedded active profile for {selectedNode.countryName}
                </div>
              </div>
              <button
                onClick={() => setShowConfigModal(false)}
                style={{ background: 'transparent', border: 'none', color: '#64748b', cursor: 'pointer', fontSize: '16px' }}
              >
                ✕
              </button>
            </div>

            <pre style={{
              backgroundColor: '#07090e',
              border: '1px solid #161e2e',
              borderRadius: '10px',
              padding: '14px',
              color: '#38bdf8',
              fontSize: '11px',
              lineHeight: '1.6',
              overflowX: 'auto',
              marginBottom: '16px'
            }}>
              {getWireGuardConfigText(selectedNode)}
            </pre>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                onClick={() => {
                  navigator.clipboard.writeText(getWireGuardConfigText(selectedNode));
                  setCopied(true);
                  setTimeout(() => setCopied(false), 2000);
                }}
                style={{
                  flex: 1,
                  backgroundColor: '#00f5a0',
                  color: '#07090e',
                  border: 'none',
                  padding: '10px',
                  borderRadius: '8px',
                  fontWeight: 800,
                  fontSize: '12px',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px'
                }}
              >
                {copied ? <Check size={16} /> : <Copy size={16} />}
                <span>{copied ? 'COPIED TO CLIPBOARD' : 'COPY CONFIG'}</span>
              </button>

              <button
                onClick={() => setShowConfigModal(false)}
                style={{
                  backgroundColor: '#172033',
                  color: '#cbd5e1',
                  border: '1px solid #23314d',
                  padding: '10px 16px',
                  borderRadius: '8px',
                  fontWeight: 600,
                  fontSize: '12px',
                  cursor: 'pointer'
                }}
              >
                CLOSE
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
