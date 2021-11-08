package com.giftech.taskmastertest.core.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.giftech.taskmastertest.core.model.Task

class Datasource {
    private val taskTitles = arrayListOf<String>(
            "Buat hewan",
    )
    private val taskDates = arrayListOf<String>(
            "22/01/2001",
    )
    private val taskTimes = arrayListOf<String>(
            "14:00",
    )

    fun loadTasks():ArrayList<Task>{
        val list = arrayListOf<Task>()
        for (position in taskTitles.indices){
            val task = Task()
            task.title = taskTitles[position]
            task.date = taskDates[position]
            task.time = taskTimes[position]
            list.add(task)
        }
        for (elem in list){
            Log.v("galloadtask", elem.title.toString())
        }
        return list
    }

    fun addTask(task: Task): Unit {
        taskTitles.add(task.title.toString())
        taskDates.add(task.date.toString())
        taskTimes.add(task.time.toString())
        Log.v("galaddtask", taskTitles.toString())
    }

    fun printTask(context: Context) {
        Toast.makeText(context,taskTitles.last().toString(),Toast.LENGTH_LONG).show()
    }

}