package com.giftech.taskmastertest.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.giftech.taskmastertest.core.data.AuthRepository
import com.giftech.taskmastertest.core.model.User

class SplashViewModel(private val repository: AuthRepository):ViewModel() {

    fun getUser():LiveData<Boolean> = repository.getUser()

}