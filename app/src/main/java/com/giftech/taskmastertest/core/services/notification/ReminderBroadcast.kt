package com.giftech.taskmastertest.core.services.notification

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

        val title = intent!!.getStringExtra("title")

        val builder = NotificationCompat.Builder(context!!, "notifyTaskmaster")
            .setContentTitle(context.getString(R.string.notif_hello)+" "+userName)
            .setContentText(context.getString(R.string.notif_dont_forget)+" "+title)
            .setSmallIcon(R.drawable.ic_logo_white)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup("notifyTaskmaster")
            .setGroupSummary(true)

        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.notify(200, builder.build())
    }
}