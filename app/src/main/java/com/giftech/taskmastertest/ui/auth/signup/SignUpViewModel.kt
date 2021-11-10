package com.giftech.taskmastertest.ui.auth.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.giftech.taskmastertest.core.data.AuthRepository
import com.giftech.taskmastertest.core.model.User

class SignUpViewModel(private val repository: AuthRepository):ViewModel() {

    fun signUp(user:User):LiveData<Boolean> = repository.signUp(user)

    fun signUpGoogle(idToken:String):LiveData<Boolean> = repository.signWithGoogle(idToken)

    fun getUser():LiveData<Boolean> = repository.getUser()

    val error = repository.error

    val loading = repository.isLoading

}