package org.Telegram.digitalresistanceLite

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PsiphonManager.ConnectionListener {

  companion object {
    const val BASE_URL = "https://web.telegram.org/"

    const val FILE_CHOOSER_REQUEST_CODE = 100
  }

  private val psiphonManager by lazy { PsiphonManager(applicationContext) }

  private var uploadMessage: ValueCallback<Array<Uri>>? = null
  private var uploadMessageKitkat: ValueCallback<Uri>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setStatus("Loading Psiphon...")
    psiphonManager.connect(this)
  }

  override fun onDestroy() {
    psiphonManager.close()
    super.onDestroy()
  }

  override fun onPsiphonConnected(port: Int) = runOnUiThread {
    setStatus("Loading Telegram...")
    setupWebView(port)
  }

  override fun onPsiphonError(error: Exception) = runOnUiThread {
    setStatus(error.message ?: "Unknown error. Please contact developers for assistance.")
  }

  @SuppressLint("SetJavaScriptEnabled")
  private fun setupWebView(port: Int) {
    WebViewProxySettings.setLocalProxy(this@MainActivity, port)
    webView.apply {
      settings.javaScriptEnabled = true
      settings.domStorageEnabled = true

      webChromeClient = createWebChromeClient()
      webViewClient = createWebViewClient()

      loadUrl(BASE_URL)
    }
  }

  private fun createWebChromeClient() = object : WebChromeClient() {
    override fun onProgressChanged(view: WebView, newProgress: Int) {
      if (newProgress >= 95) showSplash(false)
    }

    //For Android 4.1+ only
    @Suppress("ProtectedInFinal")
    protected fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
      openFileBrowserKitKat(uploadMsg)
    }

    //For Android 4.1+ only
    @Suppress("ProtectedInFinal")
    protected fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
      openFileBrowserKitKat(uploadMsg)
    }

    override fun onShowFileChooser(
      webView: WebView,
      filePathCallback: ValueCallback<Array<Uri>>,
      fileChooserParams: FileChooserParams
    ): Boolean {
      uploadMessage?.let {
        it.onReceiveValue(null)
        uploadMessage = null
      }

      uploadMessage = filePathCallback

      var intent: Intent? = null
      if (SDK_INT >= LOLLIPOP) {
        intent = fileChooserParams.createIntent()
      }
      try {
        startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
      } catch (e: ActivityNotFoundException) {
        uploadMessage = null
        Toast.makeText(applicationContext, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
        return false
      }

      return true
    }
  }

  private fun createWebViewClient() = object : WebViewClient() {
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
      handler?.proceed()
    }
  }

  private fun openFileBrowserKitKat(uploadMsg: ValueCallback<Uri>) {
    uploadMessageKitkat = uploadMsg
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "*/*"
    startActivityForResult(Intent.createChooser(intent, "File Browser"), FILE_CHOOSER_REQUEST_CODE)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when {
      SDK_INT >= LOLLIPOP -> handleFileResultLollipop(requestCode, resultCode, data)
      else -> handleFileResultKitkat(requestCode, resultCode, data)
    }
  }

  private fun handleFileResultLollipop(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode != FILE_CHOOSER_REQUEST_CODE || uploadMessage == null) {
      super.onActivityResult(requestCode, resultCode, data)
      return
    }

    var results: Array<Uri>? = null

    if (resultCode == Activity.RESULT_OK) {
      data?.let {
        it.dataString?.let { results = arrayOf(Uri.parse(it)) }
      }
    }

    uploadMessage!!.onReceiveValue(results)
    uploadMessage = null
  }

  private fun handleFileResultKitkat(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode != FILE_CHOOSER_REQUEST_CODE || uploadMessageKitkat == null) {
      super.onActivityResult(requestCode, resultCode, data)
      return
    }

    var results: Uri? = null

    if (resultCode == Activity.RESULT_OK) {
      data?.let {
        it.dataString?.let { results = Uri.parse(it) }
      }
    }

    uploadMessageKitkat!!.onReceiveValue(results)
    uploadMessageKitkat = null
  }

  private fun showSplash(show: Boolean) {
    when {
      show -> splashScreen.fadeIn(250, { splashScreen.visibility = VISIBLE })
      else -> splashScreen.fadeOut(500, { splashScreen.visibility = GONE })
    }
  }

  private fun setStatus(text: String) {
    splashStatus.text = text
  }
}

