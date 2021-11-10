package com.giftech.taskmastertest.ui.auth.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.giftech.taskmastertest.core.data.AuthRepository
import com.giftech.taskmastertest.core.model.User

class SignInViewModel(private val repository: AuthRepository):ViewModel() {

    fun signIn(user:User):LiveData<Boolean> = repository.signIn(user)

    fun signInGoogle(idToken:String):LiveData<Boolean> = repository.signWithGoogle(idToken)

    val error = repository.error

    val loading = repository.isLoading

}