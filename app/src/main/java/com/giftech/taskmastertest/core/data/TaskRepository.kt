package com.giftech.taskmastertest.core.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giftech.taskmastertest.core.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class TaskRepository() {

    private val TAG = javaClass.simpleName

    private val auth: FirebaseAuth = Firebase.auth
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val _user = MutableLiveData<FirebaseUser>()
    val user: LiveData<FirebaseUser>
        get() = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error


    fun getTodayTask(todayDate:String):LiveData<List<Task>>{
        val listTask = MutableLiveData<List<Task>>()
        _isLoading.postValue(true)
        databaseReference
            .child("users")
            .child(_user.value?.uid!!)
            .child("tasks")
            .orderByChild("timeMillis")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    _isLoading.postValue(false)
                    val items = snapshot.children
                    val listTaskRes = arrayListOf<Task>()
                    items.forEach {
                        val task = it.getValue(Task::class.java)
                        if (task!!.completed == false) {
                            if (task!!.date.equals(todayDate)) {
                                listTaskRes.add(task)
                            }
                        }
                    }
                    listTask.postValue(listTaskRes)
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.postValue(false)
                    Log.d(TAG, error.message)
                }
            })
        return listTask
    }

    fun getUpcomingTask(todayDate:String):LiveData<List<Task>>{
        val listTask = MutableLiveData<List<Task>>()
        _isLoading.postValue(true)
        databaseReference
            .child("users")
            .child(_user.value?.uid!!)
            .child("tasks")
            .orderByChild("timeMillis")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    _isLoading.postValue(false)
                    val items = snapshot.children
                    val listTaskRes = arrayListOf<Task>()
                    items.forEach {
                        val task = it.getValue(Task::class.java)
                        if (task!!.completed == false) {
                            if (!task!!.date.equals(todayDate)) {
                                listTaskRes.add(task)
                            }
                        }
                    }
                    listTask.postValue(listTaskRes)
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.postValue(false)
                    Log.d(TAG, error.message)
                }
            })
        return listTask
    }

    fun addNewTask(task: Task){
        val newTask = databaseReference
            .child("users")
            .child(_user.value?.uid!!)
            .child("tasks")
            .push()
        task.id = newTask.key.toString()

        val givenDateString = task.time+" "+task.date
        val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy")
        val mDate: Date = sdf.parse(givenDateString)
        val timeInMilliseconds = mDate.time
        task.timeMillis = timeInMilliseconds.toString()

        newTask.setValue(task)
    }

    fun signOut(){
        auth.signOut()
    }

    init {
        _user.postValue(auth.currentUser)
    }

    companion object {
        @Volatile
        private var INSTANCE: TaskRepository? = null
        fun getInstance(): TaskRepository? {
            if (INSTANCE == null) {
                synchronized(TaskRepository::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = TaskRepository()
                    }
                }
            }
            return INSTANCE
        }
    }


}