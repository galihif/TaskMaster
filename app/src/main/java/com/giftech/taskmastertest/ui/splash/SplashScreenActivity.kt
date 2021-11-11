package com.giftech.taskmastertest.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.core.ui.ViewModelFactory
import com.giftech.taskmastertest.ui.HomeActivity
import com.giftech.taskmastertest.ui.auth.signup.SignUpViewModel
import com.giftech.taskmastertest.ui.intro.Intro1Activity

class SplashScreenActivity : AppCompatActivity() {

    private var TIME_OUT:Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_splash_screen)

        val factory = ViewModelFactory.getInstance(application)
        val viewModel = ViewModelProvider(this, factory)[SplashViewModel::class.java]

        Handler().postDelayed({
            viewModel.getUser().observe(this, {isLogged ->
                if(isLogged){
                    val intent = Intent(this,HomeActivity::class.java)
                    startActivity(intent)
                    Log.d(javaClass.simpleName, "logged")
                } else{
                    val intent = Intent(this,Intro1Activity::class.java)
                    startActivity(intent)
                    Log.d(javaClass.simpleName, "not logged")
                }
            })
            finish()
        },TIME_OUT)
    }
}