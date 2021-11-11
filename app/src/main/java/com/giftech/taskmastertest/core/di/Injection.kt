package com.giftech.taskmastertest.core.di

import android.app.Application
import com.giftech.taskmastertest.core.data.AuthRepository
import com.giftech.taskmastertest.core.data.TaskRepository

object Injection {

    fun provideAuthRepository(application: Application) : AuthRepository? {
        return AuthRepository.getInstance(application)
    }

    fun provideTaskRepository():TaskRepository{
        return TaskRepository.getInstance()!!
    }


}