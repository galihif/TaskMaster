package com.giftech.taskmastertest.core.di

import android.app.Application
import com.giftech.taskmastertest.core.data.AuthRepository

object Injection {

    fun provideAuthRepository(application: Application) : AuthRepository? {
        return AuthRepository.getInstance(application)
    }


}