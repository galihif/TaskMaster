package com.giftech.taskmastertest.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.ui.sign.SignUpActivity
import kotlinx.android.synthetic.main.activity_intro3.*

class Intro3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro3)

        btn_get_started.setOnClickListener {
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}