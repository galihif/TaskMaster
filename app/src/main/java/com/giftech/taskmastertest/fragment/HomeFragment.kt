package com.giftech.taskmastertest.fragment

import android.content.Context
import android.content.Intent
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
import com.giftech.taskmastertest.sign.SignInActivity
import com.giftech.taskmastertest.utils.Preferences
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*


class HomeFragment : Fragment(){

    private var list:ArrayList<Task> = arrayListOf()
    private var todayList:ArrayList<Task> = arrayListOf()
    private var upcomingList:ArrayList<Task> = arrayListOf()
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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        var date = LocalDateTime.now()
//        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
//        val timeFormat = SimpleDateFormat("hh:mm")
//        val currentDate = dateFormat.format(Date())
//        val currentTime = timeFormat.format(Date())
//        Log.v("GALIH", currentDate.toString()+" "+currentTime.toString());

        //Initialize
        database = FirebaseDatabase.getInstance().reference
        preferences = Preferences(requireContext())

        //User
        val userId = preferences.getValues("userId").toString()
        val userName = preferences.getValues("userName").toString()

        //Calendar
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) +1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val date = String.format("%02d/%02d/%04d", day, month,year)


        btn_logout.setOnClickListener {
            Firebase.auth.signOut()
            preferences.setValues("userName","")
            preferences.setValues("userId","")
            var intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
        }

        tv_hello.text = "Hello, "+userName.toString()+"!"


        addDataToList(date,userId)
        setTaskAdapter(rv_today, todayList)
        setTaskAdapter(rv_upcoming,upcomingList)
    }





    private fun setTaskAdapter(recyclerView: RecyclerView?, list: java.util.ArrayList<Task>) {
        recyclerView!!.layoutManager = LinearLayoutManager(requireContext())
        val taskAdapter = TaskAdapter(requireContext(), list)
        recyclerView.adapter = taskAdapter
        recyclerView.setNestedScrollingEnabled(false);
    }

    private fun addDataToList(date:String, userId:String) {
        //ValueEventListener untuk mengambil data dari firebase dengan child goal user tsb
        database
            .child("users")
            .child(userId)
            .child("tasks")
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Hapus data dalam arraylist agar tidak jadi penumpukan data
                todayList.clear()
                upcomingList.clear()

                //Ambil semua child dalam goal dan masukan ke items
                var items = snapshot.children
                //Lakukan iterasi pada setiap item lalu buat class dan tambahkan ke list
                items.forEach{
                    var task = it.getValue(Task::class.java)
                    if(task!!.completed == false){
                        if (task!!.date.equals(date)){
                            todayList.add(task)
                        } else{
                            upcomingList.add(task)
                        }
                    }

                }
                refreshList(requireContext())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun refreshList(context: Context) {
        val todayAdapter = TaskAdapter(requireContext(), todayList)
        val upcomingAdapter = TaskAdapter(requireContext(), upcomingList)
        rv_today.adapter = todayAdapter
        rv_upcoming.adapter = upcomingAdapter

        if (todayList.isEmpty()){
            tv_today.visibility = View.GONE
            rv_today.visibility = View.GONE
        }
        if (upcomingList.isEmpty()){
            tv_upcoming.visibility = View.GONE
            rv_upcoming.visibility = View.GONE
        }

        if(todayList.isEmpty() && upcomingList.isEmpty()){
            tv_upcoming.visibility = View.GONE
            rv_upcoming.visibility = View.GONE
            tv_today.visibility = View.GONE
            rv_today.visibility = View.GONE
//            tv_no_task.visibility = View.VISIBLE
        }
    }

}