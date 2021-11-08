package com.giftech.taskmastertest

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import com.giftech.taskmastertest.ui.HomeActivity
import com.giftech.taskmastertest.ui.widget.ExampleWidgetProvider
import com.giftech.taskmastertest.ui.widget.ExampleWidgetService

class ExampleAppWidgetConfig : AppCompatActivity() {

    val SHARED_PREFS = "prefs"
    val KEY_BUTTON_TEXT = "keyButtonText"

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var editTextButton: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example_app_widget_config)
        val configIntent = intent
        val extras = configIntent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
        editTextButton = findViewById(R.id.edit_text_button)
    }

    fun confirmConfiguration(v: View?) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val buttonIntent = Intent(this, HomeActivity::class.java)
        val buttonPendingIntent = PendingIntent.getActivity(
            this,
            0, buttonIntent, 0
        )
        val buttonText = editTextButton!!.text.toString()
        val serviceIntent = Intent(this, ExampleWidgetService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
        val clickIntent = Intent(this, ExampleWidgetProvider::class.java)
        clickIntent.action = ExampleWidgetProvider().ACTION_TOAST
        val clickPendingIntent = PendingIntent.getBroadcast(
            this,
            0, clickIntent, 0
        )
        val views = RemoteViews(this.packageName, R.layout.example_widget)
        views.setRemoteAdapter(R.id.example_widget_stack_view, serviceIntent)
        views.setEmptyView(R.id.example_widget_stack_view, R.id.example_widget_empty_view)
        views.setPendingIntentTemplate(R.id.example_widget_stack_view, clickPendingIntent)
        appWidgetManager.updateAppWidget(appWidgetId, views)
        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_BUTTON_TEXT + appWidgetId, buttonText)
        editor.apply()
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

}