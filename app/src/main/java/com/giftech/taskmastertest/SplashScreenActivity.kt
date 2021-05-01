package com.giftech.taskmastertest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.giftech.taskmastertest.intro.Intro1Activity

class SplashScreenActivity : AppCompatActivity() {

    private var TIME_OUT:Long = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_splash_screen)

        loadSplashScreen()
    }

    private fun loadSplashScreen() {
        Handler().postDelayed({
            // You can declare your desire activity here to open after finishing splash screen. Like MainActivity
            val intent = Intent(this,Intro1Activity::class.java)
            startActivity(intent)
            finish()
        },TIME_OUT)
    }
}