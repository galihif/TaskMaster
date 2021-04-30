package com.giftech.taskmastertest.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.giftech.taskmastertest.R
import com.giftech.taskmastertest.model.Task
import com.giftech.taskmastertest.utils.Preferences
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*

class ExampleWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return ExampleWidgetItemFactory(applicationContext, intent!!)
    }

    internal class ExampleWidgetItemFactory(private val context: Context, intent: Intent) :
        RemoteViewsFactory {
        private lateinit var database: DatabaseReference
        private lateinit var preferences: Preferences
        private val mGoogleSignInClient: GoogleSignInClient? = null
        private lateinit var auth: FirebaseAuth
        private lateinit var user: FirebaseUser

        private lateinit var userId: String
        private lateinit var userName:String

        private var todayList: ArrayList<Task> = arrayListOf()
        private val appWidgetId: Int
        private val exampleData = arrayListOf(
                "one", "two", "three", "four",
                "five", "six", "seven", "eight", "nine", "ten"
        )

        override fun onCreate() {
            //connect to data source
            //Initialize
            database = FirebaseDatabase.getInstance().reference
            auth = Firebase.auth

            //User
            user = auth.currentUser
            userId = user.uid.toString()

            //Calendar
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val date = String.format("%02d/%02d/%04d", day, month, year)

            addDataToList(object : FirebaseCallback {
                override fun onResponse(list: ArrayList<Task>) {
                    todayList = list
                }
            }, date)
            Log.v("GAL", "after add data"+todayList.size.toString())

            SystemClock.sleep(3000)
        }

        private fun addDataToList(callback: FirebaseCallback, date:String) {
            Log.v("GAL", "before"+todayList.size.toString())
            database
                    .child("users")
                    .child(userId)
                    .child("tasks")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            //Hapus data dalam arraylist agar tidak jadi penumpukan data
                            todayList.clear()

                            //Ambil semua child dalam goal dan masukan ke items
                            var items = snapshot.children
                            //Lakukan iterasi pada setiap item lalu buat class dan tambahkan ke list
                            items.forEach {
                                var task = it.getValue(Task::class.java)
                                if (task!!.completed.equals(false) && task.date.equals(date))
                                todayList.add(task!!)
                            }
                            callback.onResponse(todayList)

                            Log.v("GAL", "inside"+todayList.size.toString())


                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
            Log.v("GAL", "after"+todayList.size.toString())
        }


        override fun onDataSetChanged() {
            //Calendar
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val date = String.format("%02d/%02d/%04d", day, month, year)

            addDataToList(object : FirebaseCallback {
                override fun onResponse(list: ArrayList<Task>) {
                    todayList = list
                }
            },date)
        }
        override fun onDestroy() {
            //close data source
        }

        override fun getCount(): Int {
            return exampleData.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.example_widget_item)
            if (todayList.isNotEmpty() && position <= todayList.size){
                views.setTextViewText(R.id.example_widget_item_text, todayList[position].title)
            }
            val fillIntent = Intent()
            fillIntent.putExtra(ExampleWidgetProvider().EXTRA_ITEM_POSITION, position)
            views.setOnClickFillInIntent(R.id.example_widget_item_text, fillIntent)
            SystemClock.sleep(500)
            return views
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        init {
            appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
    }

}

interface FirebaseCallback {
    fun onResponse(list: ArrayList<Task>)
}