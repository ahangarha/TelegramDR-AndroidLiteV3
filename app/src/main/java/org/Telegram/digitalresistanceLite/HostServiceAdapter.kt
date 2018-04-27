package org.Telegram.digitalresistanceLite

import android.content.Context
import ca.psiphon.PsiphonTunnel

interface HostServiceAdapter : PsiphonTunnel.HostService {
  override fun onActiveAuthorizationIDs(p0: MutableList<String>?) = Unit
  override fun getPsiphonConfig(): String
  override fun onBytesTransferred(sent: Long, received: Long) = Unit
  override fun onUntunneledAddress(p0: String?) = Unit
  override fun getVpnService(): Any = Unit
  override fun onDiagnosticMessage(p0: String?) = Unit
  override fun onListeningHttpProxyPort(port: Int) = Unit
  override fun onListeningSocksProxyPort(port: Int) = Unit
  override fun onUpstreamProxyError(p0: String?) = Unit
  override fun getAppName(): String
  override fun onClientUpgradeDownloaded(p0: String?) = Unit
  override fun onSocksProxyPortInUse(p0: Int) = Unit
  override fun onHomepage(p0: String?) = Unit
  override fun onExiting() = Unit
  override fun newVpnServiceBuilder(): Any = Unit
  override fun onHttpProxyPortInUse(p0: Int) = Unit
  override fun onClientIsLatestVersion() = Unit
  override fun getContext(): Context
  override fun onConnecting() = Unit
  override fun onSplitTunnelRegion(p0: String?) = Unit
  override fun onConnected() = Unit
  override fun onClientRegion(p0: String?) = Unit
  override fun onAvailableEgressRegions(p0: MutableList<String>?) = Unit
  override fun onClientVerificationRequired(p0: String?, p1: Int, p2: Boolean) = Unit
  override fun onStartedWaitingForNetworkConnectivity() = Unit
}