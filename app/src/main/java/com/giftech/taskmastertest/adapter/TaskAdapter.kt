package com.giftech.taskmastertest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.model.Task
import com.giftech.taskmastertest.utils.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class TaskAdapter(mContext: Context, private var taskList: ArrayList<Task>)
    : RecyclerView.Adapter<TaskAdapter.ItemViewHolder>() {

    private var mContext = mContext


    class ItemViewHolder(private val view: View, context: Context): RecyclerView.ViewHolder(view) {
        private lateinit var database: DatabaseReference
        private lateinit var preferences: Preferences
        private lateinit var auth:FirebaseAuth
        private lateinit var user: FirebaseUser

        val tvTaskTitle: TextView = view.findViewById(R.id.tv_task_title)
        val tvTaskDate: TextView = view.findViewById(R.id.tv_task_date)
        val tvTaskTime: TextView = view.findViewById(R.id.tv_task_time)
        val btnDelete: ImageView = view.findViewById(R.id.btn_delete)
        val btnCheck: ImageView = view.findViewById(R.id.btn_check)
        val context = context

        fun bindItem(task: Task){
            tvTaskTitle.setText(task.title)
            tvTaskDate.setText(task.date)
            tvTaskTime.setText(task.time)

            database = FirebaseDatabase.getInstance().reference
            preferences = Preferences(context)
            auth = Firebase.auth
            user = auth.currentUser

            if(task.completed == false){
                btnCheck.setImageResource(R.drawable.ic_uncompleted)
            } else{
                btnCheck.setImageResource(R.drawable.ic_completed)
            }

            btnDelete.setOnClickListener {
                val item = database
                        .child("users")
                        .child(user.uid)
                        .child("tasks")
                        .child(task.id.toString())
                item.removeValue()
                Toast.makeText(context,"Task ${task.title} deleted", Toast.LENGTH_LONG).show()
            }

            btnCheck.setOnClickListener {
                task.completed = !task.completed
                database.child("users")
                        .child(user.uid)
                        .child("tasks")
                        .child(task.id.toString())
                        .child("completed")
                        .setValue(task.completed)

                Toast.makeText(context,"Task ${task.title} Completed", Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_task, parent, false)
        return ItemViewHolder(adapterLayout, mContext)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val task = taskList[position]
        holder.bindItem(task)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }


}