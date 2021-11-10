package com.giftech.taskmastertest.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.giftech.taskmastertest.ui.HomeActivity
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.ui.auth.signup.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_intro1.*
import maes.tech.intentanim.CustomIntent

class Intro1Activity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro1)

        // Initialize
        auth = Firebase.auth

        btn_skip1.setOnClickListener {
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btn_next1.setOnClickListener {
            var intent = Intent(this, Intro2Activity::class.java)
            CustomIntent.customType(this, "fadein-to-fadeout")
            startActivity(intent)
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            var intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}