package com.giftech.taskmastertest.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.ui.auth.SignUpActivity
import kotlinx.android.synthetic.main.activity_intro2.*

class Intro2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro2)

        btn_skip2.setOnClickListener {
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btn_next2.setOnClickListener {
            var intent = Intent(this, Intro3Activity::class.java)
            startActivity(intent)
        }
    }
}