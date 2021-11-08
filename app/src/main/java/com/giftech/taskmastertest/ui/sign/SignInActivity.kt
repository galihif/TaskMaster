package com.giftech.taskmastertest.ui.sign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giftech.taskmastertest.ui.HomeActivity
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.core.model.User
import com.giftech.taskmastertest.core.utils.Preferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AppCompatActivity() {

    private lateinit var tUser:User
    private lateinit var preferences: Preferences
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 9001
    private val TAG = "SignInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize Firebase Auth
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
        preferences = Preferences(this)

        //keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btn_google_signin.setOnClickListener {
            googleSignIn()
            spinner_signin.visibility = View.VISIBLE
        }

        btn_signin.setOnClickListener {
            tUser = User()
            tUser.email = et_email_in.text.toString()
            tUser.password = et_password_in.text.toString()
            if (tUser.email.equals("")){
                et_email_in.requestFocus()
                et_email_in.error = "Please fill your email"
            } else if (tUser.password.equals("")){
                et_password_in.requestFocus()
                et_password_in.error = "Please fill your password"
            } else{
                pushSignIn(tUser)
                spinner_signin.visibility = View.VISIBLE
            }
        }

        btn_go_signup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val currentUser = auth.currentUser
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        spinner_signin.visibility = View.INVISIBLE
                        val user = auth.currentUser
                        var intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        spinner_signin.visibility = View.INVISIBLE
                    }
                }
    }

    private fun pushSignIn(tUser: User) {
        auth.signInWithEmailAndPassword(tUser.email, tUser.password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithEmail:success")
                        spinner_signin.visibility = View.INVISIBLE
                        val user = auth.currentUser
                        database
                                .child("users")
                                .child(user.uid.toString())
                                .child("name")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        tUser.name = snapshot.getValue(String::class.java).toString()
                                        var intent = Intent(this@SignInActivity, HomeActivity::class.java)
                                        startActivity(intent)
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        spinner_signin.visibility = View.INVISIBLE
                        Toast.makeText(
                                baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }
}