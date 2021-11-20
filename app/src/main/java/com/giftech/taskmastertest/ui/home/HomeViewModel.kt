package com.giftech.taskmastertest.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.giftech.taskmastertest.core.data.AuthRepository
import com.giftech.taskmastertest.core.data.TaskRepository
import com.giftech.taskmastertest.core.model.Task
import com.giftech.taskmastertest.core.model.User

class HomeViewModel(private val repository: TaskRepository):ViewModel() {

    val user = repository.user

    val loading = repository.isLoading

    fun getTodayTask(todayDate:String):LiveData<List<Task>> = repository.getTodayTask(todayDate)

    fun getUpcomingTask(todayDate:String):LiveData<List<Task>> = repository.getUpcomingTask(todayDate)

    fun addNewTask(task:Task) = repository.addNewTask(task)

    fun signOut() = repository.signOut()

}