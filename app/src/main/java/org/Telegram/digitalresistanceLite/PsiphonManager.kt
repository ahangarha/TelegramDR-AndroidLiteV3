package org.Telegram.digitalresistanceLite

import android.content.Context
import android.util.Log
import ca.psiphon.PsiphonTunnel
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicInteger

class PsiphonManager(private val appContext: Context) : HostServiceAdapter {

  companion object {
    private const val TAG = "PsiphonManager"
  }

  interface ConnectionListener {
    fun onPsiphonConnected(port: Int)
    fun onPsiphonError(error: Exception)
  }

  private lateinit var connectionListener: ConnectionListener
  private val httpLocalPort = AtomicInteger()
  private val psiphonTunnel by lazy { PsiphonTunnel.newPsiphonTunnel(this) }

  fun connect(listener: ConnectionListener) {
    connectionListener = listener
    try {
      psiphonTunnel.startTunneling("")
    } catch (e: Exception) {
      Log.e(TAG, e.message)
      connectionListener.onPsiphonError(e)
    }
  }

  fun close() = psiphonTunnel.stop()

  override fun onConnected() {
    connectionListener.onPsiphonConnected(httpLocalPort.get())
  }

  override fun getAppName(): String = appContext.getString(R.string.app_name)

  override fun getContext() = appContext

  override fun getPsiphonConfig(): String {
    Log.d(TAG, "getPsiphonConfig")
    return try {
      //Psiphon config file should only be stored locally. Should be added to /res/raw dir if it's missing.
      JSONObject(readInputStreamToString(appContext.resources.openRawResource(R.raw.psiphon_config))).toString()
    } catch (e: IOException) {
      Log.e(TAG, e.message)
      ""
    } catch (e: JSONException) {
      Log.e(TAG, e.message)
      ""
    }
  }

  override fun onBytesTransferred(sent: Long, received: Long) {
    Log.d(TAG, "Bytes sent: $sent")
    Log.d(TAG, "Bytes received: $received")
  }

  override fun onListeningHttpProxyPort(port: Int) {
    Log.d(TAG, "onListeningHttpProxyPort: $port")
    httpLocalPort.set(port)
  }

  override fun onUpstreamProxyError(p0: String?) {
    connectionListener.onPsiphonError(Exception(p0))
  }

  @Throws(IOException::class)
  private fun readInputStreamToString(inputStream: InputStream) = String(readInputStreamToBytes(inputStream), Charset.forName("UTF-8"))

  @Throws(IOException::class)
  private fun readInputStreamToBytes(inputStream: InputStream): ByteArray {
    val outputStream = ByteArrayOutputStream()

    outputStream.write(inputStream.readBytes())

    outputStream.flush()
    inputStream.close()
    return outputStream.toByteArray()
  }

}