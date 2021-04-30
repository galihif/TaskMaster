package com.giftech.taskmastertest.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.giftech.taskmastertest.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ReminderBroadcast: BroadcastReceiver() {
    private val mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var userId: String
    private lateinit var userName:String
    override fun onReceive(context: Context?, intent: Intent?) {
        //Initialize
        auth = Firebase.auth

        //User
        user = auth.currentUser
        userName = user.displayName.toString()

        var title = intent!!.getStringExtra("title")

        var builder = NotificationCompat.Builder(context!!, "notifyLemubit")
            .setContentTitle("Hello "+userName)
            .setContentText("Don't forget to "+title)
            .setSmallIcon(R.drawable.ic_logo_white)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        var notificationManager = NotificationManagerCompat.from(context)

        notificationManager.notify(200, builder.build())
    }
}