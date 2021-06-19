package com.giftech.taskmastertest

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftech.taskmastertest.adapter.TaskAdapter
import com.giftech.taskmastertest.model.Task
import com.giftech.taskmastertest.notification.ReminderBroadcast
import com.giftech.taskmastertest.sign.SignInActivity
import com.giftech.taskmastertest.utils.Preferences
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {


    private lateinit var database: DatabaseReference
    private lateinit var preferences: Preferences
    private val mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var user:FirebaseUser

    private lateinit var userId: String
    private lateinit var userName:String

    private var todayList: ArrayList<Task> = arrayListOf()
    private var upcomingList: ArrayList<Task> = arrayListOf()
    private var taskList: ArrayList<Task> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Initialize
        database = FirebaseDatabase.getInstance().reference
        preferences = Preferences(this)
        auth = Firebase.auth

        //User
        user = auth.currentUser
        userId = user.uid.toString()
        userName = user.displayName.toString()

        tv_hello.text = getString(R.string.hello_user)+" "+userName+"!"

        //Calendar
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val date = String.format("%02d/%02d/%04d", day, month, year)

        addDataToList(date)
        setTaskAdapter(rv_today, todayList)
        setTaskAdapter(rv_upcoming, upcomingList)
        createNotificationChannel()

        btn_logout.setOnClickListener {
            Firebase.auth.signOut()
            preferences.setValues("userName", "")
            preferences.setValues("userId", "")
            var intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

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
            var intent = Intent(this, HistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
    }

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
        val newTask = database
                .child("users")
                .child(userId)
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

    private fun setTaskAdapter(recyclerView: RecyclerView?, list: java.util.ArrayList<Task>) {
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        val taskAdapter = TaskAdapter(this, list)
        recyclerView.adapter = taskAdapter
        recyclerView.setNestedScrollingEnabled(false);
    }

    private fun addDataToList(date: String) {
        spinner_home.visibility = View.VISIBLE
        //ValueEventListener untuk mengambil data dari firebase dengan child goal user tsb
        database
                .child("users")
                .child(userId)
                .child("tasks")
                .orderByChild("timeMillis")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //Hapus data dalam arraylist agar tidak jadi penumpukan data
                        todayList.clear()
                        upcomingList.clear()

                        //Ambil semua child dalam goal dan masukan ke items
                        var items = snapshot.children
                        //Lakukan iterasi pada setiap item lalu buat class dan tambahkan ke list
                        items.forEach {
                            var task = it.getValue(Task::class.java)
                            if (task!!.completed == false) {
                                if (task!!.date.equals(date)) {
                                    todayList.add(task)
                                } else {
                                    upcomingList.add(task)
                                }
                                taskList.add(task)
                            }
                        }
                        refreshList()


                        spinner_home.visibility = View.INVISIBLE

                        if (todayList.size == 0) {
                            tv_no_task_today.visibility = View.VISIBLE
                        } else {
                            tv_no_task_today.visibility = View.GONE
                        }

                        if (upcomingList.size == 0) {
                            tv_no_task_upcoming.visibility = View.VISIBLE
                        } else {
                            tv_no_task_upcoming.visibility = View.GONE
                        }

                        taskList.forEach {
                            setNotification(it, taskList.indexOf(it))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
    }

    fun refreshList() {
        val todayAdapter = TaskAdapter(this, todayList)
        val upcomingAdapter = TaskAdapter(this, upcomingList)
        rv_today.adapter = todayAdapter
        rv_upcoming.adapter = upcomingAdapter
    }
}
