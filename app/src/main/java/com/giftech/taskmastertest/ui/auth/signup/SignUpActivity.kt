package com.giftech.taskmastertest.ui.auth.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.giftech.taskmastertest.ui.HomeActivity
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.core.model.User
import com.giftech.taskmastertest.core.ui.ViewModelFactory
import com.giftech.taskmastertest.core.utils.Preferences
import com.giftech.taskmastertest.ui.auth.signin.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var tUser:User
    private lateinit var preferences:Preferences
    private val RC_SIGN_IN = 9001
    private val TAG = "SignInActivity"

    private lateinit var viewModel: SignUpViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
        preferences = Preferences(this)
        tUser = User()

        val factory = ViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, factory)[SignUpViewModel::class.java]

        btn_google_signup.setOnClickListener {
            setupGoogleSignUp()
        }

        btn_signup.setOnClickListener {
            tUser.name = et_name_up.text.toString()
            tUser.email = et_email_up.text.toString()
            tUser.password = et_password_up.text.toString()

            if(formNotEmpty()){
                viewModel.signUp(tUser).observe(this, {isLogged ->
                    if(isLogged){
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    }
                })
            }
        }

        btn_go_signin.setOnClickListener {
            var intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        viewModel.loading.observe(this, {isLoading ->
            showLoading(isLoading)
        })

        viewModel.error.observe(this, {error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })

    }

    private fun formNotEmpty():Boolean{
        if (tUser.name.equals("")){
            et_name_up.requestFocus()
            et_name_up.error = "Please fill your name"
            return false
        }
        if (tUser.email.equals("")){
            et_email_up.requestFocus()
            et_email_up.error = "Please fill your email"
            return false
        }
        if (tUser.password.equals("")){
            et_password_up.requestFocus()
            et_password_up.error = "Please fill your password"
            return false
        }
        return true
    }

    private fun showLoading(loading:Boolean){
        if (loading){
            spinner_signup.visibility = View.VISIBLE
        } else{
            spinner_signup.visibility = View.GONE
        }
    }

    private fun setupGoogleSignUp() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.server_client_id))
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onStart() {
        super.onStart()
        viewModel.getUser().observe(this, {isLogged ->
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                viewModel.signUpGoogle(account.idToken!!).observe(this,{ isLogged ->
                    if(isLogged){
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    }
                })
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

}