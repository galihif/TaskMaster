package com.giftech.taskmastertest.ui.home

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.core.adapter.TaskAdapter
import com.giftech.taskmastertest.core.model.Task
import com.giftech.taskmastertest.core.services.notification.ReminderBroadcast
import com.giftech.taskmastertest.core.ui.ViewModelFactory
import com.giftech.taskmastertest.ui.auth.signin.SignInActivity
import com.giftech.taskmastertest.ui.history.HistoryActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.add_task_popup.*
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {


    private lateinit var database: DatabaseReference
    private val mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var user:FirebaseUser

    private lateinit var userId: String
    private lateinit var userName:String

    private var todayList: ArrayList<Task> = arrayListOf()
    private var upcomingList: ArrayList<Task> = arrayListOf()
    private var taskList: ArrayList<Task> = arrayListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val factory = ViewModelFactory.getInstance(this.application)
        val viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        val todayAdapter = TaskAdapter()
        val upcomingAdapter = TaskAdapter()

        viewModel.user.observe(this, {
            user = it
            userId = user.uid
            userName = user.displayName.toString()

            tv_hello.text = getString(R.string.hello_user)+" "+userName+"!"
        })

        viewModel.getTodayTask(setTodayDate()).observe(this, { listTask ->
            todayAdapter.setList(listTask)
        })

        with(rv_today){
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = todayAdapter
            isNestedScrollingEnabled = false
        }

        viewModel.getUpcomingTask(setTodayDate()).observe(this, {
            upcomingAdapter.setList(it)
        })

        with(rv_upcoming){
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = upcomingAdapter
            isNestedScrollingEnabled = false
        }

        viewModel.loading.observe(this, {
            showLoading(it)
        })

        //Initialize
        database = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth

//        createNotificationChannel()

        btn_logout.setOnClickListener {
            viewModel.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        //Action saat btn_add diklik
        btn_add.setOnClickListener {
            //Inisiasi view material dialog
            val dialog = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
            val view: View = layoutInflater.inflate(R.layout.add_task_popup, null)
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
                    viewModel.addNewTask(task)
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
                val datePickerDialog = DatePickerDialog(
                    this,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        // Display Selected date in TextView
                        etAddDate.setText(
                            String.format(
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                monthOfYear + 1,
                                year
                            )
                        )
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }

            //Action saat etAddTime diklik
            etAddTime.setOnClickListener {
                val timePickerDialog = TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                        etAddTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
                        val mHour = etAddTime.text
                        val hourList = mHour.split(':').map { it.trim() }
                    },
                    hour,
                    minute,
                    true
                )
                timePickerDialog.show()
            }
        }

        //Action saat btn_history diklik
        btn_history.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
    }

    private fun setTodayDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val date = String.format("%02d/%02d/%04d", day, month, year)
        return date
    }

    private fun showLoading(isLoading:Boolean){
        if(isLoading){
            spinner_home.visibility = View.VISIBLE
        } else{
            spinner_home.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        var name:CharSequence = "Task Reminder"
        var description = "Reminder for task"
        var importance = NotificationManager.IMPORTANCE_DEFAULT

        var channel = NotificationChannel("notifyTaskmaster", name, importance)
        channel.description = description

        var notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun setNotification(task: Task, code:Int) {

        var intent = Intent(this, ReminderBroadcast::class.java)
        intent.putExtra("title", task.title)
        var pendingIntent = PendingIntent.getBroadcast(this, code, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        if(task.timeMillis.toLong() >= System.currentTimeMillis()){
            alarmManager.set(AlarmManager.RTC_WAKEUP, task.timeMillis.toLong(), pendingIntent)
        }
    }

    private fun addTaskToFirebase(task: Task) {

    }


}
