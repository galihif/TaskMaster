package com.giftech.taskmastertest.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.adapter.TaskAdapter
import com.giftech.taskmastertest.model.Task
import com.giftech.taskmastertest.utils.Preferences
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment : Fragment() {

    private var completedList:ArrayList<Task> = arrayListOf()
    private lateinit var database: DatabaseReference
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        preferences = Preferences(requireContext())

        val userId = preferences.getValues("userId")
        val userName = preferences.getValues("userName")

        tv_completed.text = userName+"'s Completed Tasks"
        addDataToList(userId)
        setTaskAdapter(rv_completed, completedList)
    }

    private fun setTaskAdapter(rvCompleted: RecyclerView?, completedList: ArrayList<Task>) {
        rvCompleted!!.layoutManager = LinearLayoutManager(requireContext())
        val completedAdapter = TaskAdapter(requireContext(), completedList)
        rvCompleted.adapter = completedAdapter
    }

    private fun addDataToList(userId: String?) {
        database
                .child("users")
                .child(userId.toString())
                .child("tasks")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        completedList.clear()
                        val items = snapshot.children
                        items.forEach{
                            var task = it.getValue(Task::class.java)
                            if (task!!.completed == true){
                                completedList.add(task)
                            }
                        }
                        refreshList(completedList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
    }

    private fun refreshList(completedList: ArrayList<Task>) {
        val completedAdapter = TaskAdapter(requireContext(), completedList)
        rv_completed.adapter = completedAdapter
    }


}