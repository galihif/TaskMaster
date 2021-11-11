package com.giftech.taskmastertest.ui.auth.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.giftech.taskmastertest.ui.home.HomeActivity
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.core.model.User
import com.giftech.taskmastertest.core.ui.ViewModelFactory
import com.giftech.taskmastertest.ui.auth.signup.SignUpActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AppCompatActivity() {

    private lateinit var tUser:User
    private lateinit var viewModel: SignInViewModel

    companion object{
        const val RC_SIGN_IN = 9001
        const val TAG = "SignInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val factory = ViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, factory)[SignInViewModel::class.java]

        btn_signin.setOnClickListener {
            tUser = User()
            tUser.email = et_email_in.text.toString()
            tUser.password = et_password_in.text.toString()

            if(formNotEmpty()){
                viewModel.signIn(tUser).observe(this, {isLogged ->
                    if(isLogged){
                        val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                        startActivity(intent)
                    }
                })
            }
        }

        btn_google_signin.setOnClickListener {
            setupGoogleSignIn()
        }

        btn_go_signup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        viewModel.loading.observe(this, {isLoading ->
            showLoading(isLoading)
        })

        viewModel.error.observe(this, {error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })
    }

    private fun showLoading(loading:Boolean){
        if (loading){
            spinner_signin.visibility = View.VISIBLE
        } else{
            spinner_signin.visibility = View.GONE
        }
    }

    private fun formNotEmpty(): Boolean {
        if (tUser.email == ""){
            et_email_in.requestFocus()
            et_email_in.error = "Please fill your email"
            return false
        }

        if (tUser.password == ""){
            et_password_in.requestFocus()
            et_password_in.error = "Please fill your password"
            return false
        }
        return true
    }

    private fun setupGoogleSignIn() {
        //keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.server_client_id))
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        startActivityForResult(mGoogleSignInClient?.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                viewModel.signInGoogle(account.idToken!!).observe(this,{isLogged ->
                    if(isLogged){
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    }
                })
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

}