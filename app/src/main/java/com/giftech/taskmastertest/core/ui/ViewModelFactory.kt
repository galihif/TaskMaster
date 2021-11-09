package com.giftech.taskmastertest.core.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giftech.taskmastertest.core.data.AuthRepository
import com.giftech.taskmastertest.core.di.Injection
import com.giftech.taskmastertest.ui.auth.signin.SignInViewModel


class ViewModelFactory private constructor(
    private val mAuthRepository: AuthRepository
): ViewModelProvider.NewInstanceFactory() {

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(application: Application): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    Injection.provideAuthRepository(application)!!,
                ).apply {
                    instance = this
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when{
            modelClass.isAssignableFrom(SignInViewModel::class.java)->{
                return SignInViewModel(mAuthRepository) as T
            }

            else -> throw Throwable("Unknown ViewModel class: " + modelClass.name)
        }
    }

}