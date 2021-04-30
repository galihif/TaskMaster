package com.giftech.taskmastertest.sign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giftech.taskmastertest.HomeActivity
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.model.User
import com.giftech.taskmastertest.utils.Preferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var tUser:User
    private lateinit var preferences:Preferences
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 9001
    private val TAG = "SignInActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
        preferences = Preferences(this)
        tUser = User()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btn_google_signup.setOnClickListener {
            googleSignUp()
            spinner_signup.visibility = View.VISIBLE
        }

        btn_signup.setOnClickListener {
            tUser.name = et_name_up.text.toString()
            tUser.email = et_email_up.text.toString()
            tUser.password = et_password_up.text.toString()
            if (tUser.name.equals("")){
                et_name_up.requestFocus()
                et_name_up.error = "Please fill your name"
            } else if (tUser.email.equals("")){
                et_email_up.requestFocus()
                et_email_up.error = "Please fill your email"
            } else if (tUser.password.equals("")){
                et_password_up.requestFocus()
                et_password_up.error = "Please fill your password"
            } else{
                pushSignUp(tUser)
                spinner_signup.visibility = View.VISIBLE
            }
        }

        btn_go_signin.setOnClickListener {
            var intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

    }

    private fun googleSignUp() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        spinner_signup.visibility = View.INVISIBLE
                        val user = auth.currentUser

                        //Get user profile from auth
                        tUser.name = user.displayName
                        tUser.email = user.email
                        tUser.password = ""

                        //Push user to RD
                        database.child("users")
                                .child(user.uid)
                                .setValue(tUser)
                                .addOnSuccessListener {
                                    var intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        spinner_signup.visibility = View.INVISIBLE
                    }
                }
    }

    private fun pushSignUp(tUser: User) {
        auth.createUserWithEmailAndPassword(tUser.email, tUser.password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "createUserWithEmail:success")
                    spinner_signup.visibility = View.INVISIBLE
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(tUser.name)
                            .build()

                    user!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "User profile updated.")
                                    tUser.password = ""
                                    database.child("users")
                                            .child(user.uid)
                                            .setValue(tUser)
                                            .addOnSuccessListener {
                                                var intent = Intent(this, HomeActivity::class.java)
                                                startActivity(intent)
                                            }
                                }
                            }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    spinner_signup.visibility = View.INVISIBLE
                    Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

}