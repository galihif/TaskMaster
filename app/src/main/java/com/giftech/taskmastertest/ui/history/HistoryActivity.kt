package com.giftech.taskmastertest.ui.history

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.core.model.Task
import com.giftech.taskmastertest.core.utils.Preferences
import com.giftech.taskmastertest.ui.home.HomeActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_history.*
import java.util.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var preferences: Preferences
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var userId: String
    private lateinit var userName:String

    private var completedList:ArrayList<Task> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        //Initialize
        database = FirebaseDatabase.getInstance().reference
        preferences = Preferences(this)
        auth = Firebase.auth
        user = auth.currentUser

        userId = user.uid
        userName = user.displayName

        tv_completed.text = userName+" "+getString(R.string.user_completed_task)
        addDataToList()
//        setTaskAdapter(rv_completed, completedList)


        //Action saat btn_add diklik
        btn_add.setOnClickListener {
            //Inisiasi view material dialog
            var dialog = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
            var view: View = layoutInflater.inflate(R.layout.add_task_popup, null)
            dialog.setView(view)
            val alertDialog = dialog.create()

            //Inisiasi view etAddDate dan etAddTime
            val etAddTitle = view.findViewById<EditText>(R.id.et_add_title)
            val etAddDate = view.findViewById<EditText>(R.id.et_add_date)
            val etAddTime = view.findViewById<EditText>(R.id.et_add_time)
            val tvAdd = view.findViewById<TextView>(R.id.tv_add)
            val tvCancel = view.findViewById<TextView>(R.id.tv_cancel)

            tvAdd.setOnClickListener {
                val task = Task()
                task.title = etAddTitle.text.toString()
                task.date = etAddDate.text.toString()
                task.time = etAddTime.text.toString()
                if(task.title.equals("")){
                    etAddTitle.requestFocus()
                    etAddTitle.error = "Fill title"
                } else if(task.date.equals("")){
                    etAddDate.requestFocus()
                    etAddDate.error = "Fill date"
                } else if (task.time.equals("")){
                    etAddTime.requestFocus()
                    etAddTime.error = "Fill time"
                } else{
                    addTaskToFirebase(task)
                    alertDialog.dismiss()
                }
            }

            tvCancel.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()

            //Inisiasi value calendar
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            //Action saat etAddDate diklik
            etAddDate.setOnClickListener {
                val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    // Display Selected date in TextView
                    etAddDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year))
                }, year, month, day)
                datePickerDialog.show()
            }

            //Action saat etAddTime diklik
            etAddTime.setOnClickListener {
                val timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                    etAddTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
                    val mHour = etAddTime.text
                    val hourList = mHour.split(':').map { it.trim() }
                }, hour, minute, true)
                timePickerDialog.show()
            }
        }

        btn_go_home.setOnClickListener {
            var intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

    }

    private fun addTaskToFirebase(task: Task) {
        val newTask = database
                .child("users")
                .child(userId)
                .child("tasks")
                .push()
        task.id = newTask.key.toString()
        newTask.setValue(task)
    }

//    private fun setTaskAdapter(rvCompleted: RecyclerView?, completedList: ArrayList<Task>) {
//        rvCompleted!!.layoutManager = LinearLayoutManager(this)
//        val completedAdapter = TaskAdapter(this, completedList)
//        rvCompleted.adapter = completedAdapter
//    }

    private fun addDataToList() {
        database
                .child("users")
                .child(userId)
                .child("tasks")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        completedList.clear()
                        val items = snapshot.children
                        items.forEach{
                            var task = it.getValue(Task::class.java)
                            if (task!!.completed == true){
                                completedList.add(task)
                            }
                        }
//                        refreshList()
                        if(completedList.isEmpty()){
                            tv_no_task_upcoming.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
    }

//    private fun refreshList() {
//        val completedAdapter = TaskAdapter(this, completedList)
//        rv_completed.adapter = completedAdapter
//    }

}