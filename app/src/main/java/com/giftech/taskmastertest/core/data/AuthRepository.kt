package com.giftech.taskmastertest.core.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giftech.taskmastertest.core.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

    fun signInGoogle(idToken: String):LiveData<Boolean> {
        val isLogged = MutableLiveData<Boolean>()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _isLoading.postValue(true)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                _isLoading.postValue(false)
                if (task.isSuccessful) {
                    isLogged.postValue(true)
                    Log.d(TAG, "signInWithCredential: success")
                    isLogged.postValue(true)
                } else {
                    isLogged.postValue(false)
                    _error.postValue(task.exception?.message)
                    Log.w(TAG, "signInWithCredential: failure", task.exception)
                }
            }
        return isLogged
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