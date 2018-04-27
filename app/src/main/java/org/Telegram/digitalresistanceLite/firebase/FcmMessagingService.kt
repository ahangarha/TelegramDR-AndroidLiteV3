package org.Telegram.digitalresistanceLite.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmMessagingService : FirebaseMessagingService() {

  override fun onMessageReceived(message: RemoteMessage) {
    Log.i(FcmMessagingService::class.java.simpleName, "Message Received. Ignoring because app is in the foreground.")
  }

}