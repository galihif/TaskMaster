package com.giftech.taskmastertest.core.data

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giftech.taskmastertest.core.model.User
import com.giftech.taskmastertest.ui.HomeActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class AuthRepository private constructor(private val application: Application) {

    private val TAG = javaClass.simpleName

    private val auth: FirebaseAuth = Firebase.auth
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    fun getUser():LiveData<Boolean>{
        val isLogged = MutableLiveData<Boolean>()
        if(auth.currentUser != null){
            isLogged.postValue(true)
        }
        return isLogged
    }


    fun signUp(mUser: User):LiveData<Boolean>{
        val isLogged = MutableLiveData<Boolean>()
        _isLoading.postValue(true)
        auth.createUserWithEmailAndPassword(mUser.email, mUser.password)
            .addOnCompleteListener { task ->
                _isLoading.postValue(false)
                if(task.isSuccessful){
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(mUser.name)
                        .build()

                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                mUser.password = ""
                                saveUser(mUser, user.uid)
                                isLogged.postValue(true)
                                Log.w(TAG, "signUpWithEmail: Success")
                            }
                        }
                } else{
                    isLogged.postValue(false)
                    Log.w(TAG, "signUpWithEmail:failure", task.exception)
                }
            }
        return isLogged
    }

    fun signIn(mUser:User):LiveData<Boolean> {
        val isLogged = MutableLiveData<Boolean>()
        _isLoading.postValue(true)
        auth.signInWithEmailAndPassword(mUser.email,mUser.password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                _isLoading.postValue(false)
                if (task.isSuccessful){
                    isLogged.postValue(true)
                } else{
                    isLogged.postValue(false)
                    _error.postValue(task.exception?.message)
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                }
            }
        return isLogged
    }

    fun signWithGoogle(idToken: String):LiveData<Boolean> {
        val isLogged = MutableLiveData<Boolean>()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _isLoading.postValue(true)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                _isLoading.postValue(false)
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    var isNewAccount = true
                    if(task.result != null && task.result!!.additionalUserInfo != null){
                        isNewAccount = task.result!!.additionalUserInfo.isNewUser
                    }
                    if(isNewAccount){
                        val newUser = User()
                        newUser.name = user.displayName
                        newUser.email = user.email
                        saveUser(newUser, user.uid)
                        isLogged.postValue(true)
                        Log.w(TAG, "signIn new google")
                    } else{
                        isLogged.postValue(true)
                        Log.w(TAG, "signIn old google")
                    }

                } else {
                    isLogged.postValue(false)
                    _error.postValue(task.exception?.message)
                    Log.w(TAG, "signInWithCredential: failure", task.exception)
                }
            }
        return isLogged
    }

    fun saveUser(newUser: User, id:String){
        databaseReference
            .child("users")
            .child(id)
            .setValue(newUser)
            .addOnSuccessListener {
                Log.w(TAG, "success save user")
            }
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null
        fun getInstance(application: Application): AuthRepository? {
            if (INSTANCE == null) {
                synchronized(AuthRepository::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = AuthRepository(application)
                    }
                }
            }
            return INSTANCE
        }
    }

}