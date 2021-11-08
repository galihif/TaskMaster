package com.giftech.taskmastertest.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast
import com.giftech.taskmastertest.ExampleAppWidgetConfig
import com.giftech.taskmastertest.ui.HomeActivity
import com.giftech.taskmastertest.R


class ExampleWidgetProvider : AppWidgetProvider() {
    val ACTION_TOAST = "actionToast"
    val EXTRA_ITEM_POSITION = "extraItemPosition"

    override fun onUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            val buttonIntent = Intent(context, HomeActivity::class.java)
            val buttonPendingIntent = PendingIntent.getActivity(
                    context,
                    0, buttonIntent, 0
            )
            val prefs = context.getSharedPreferences(
                    ExampleAppWidgetConfig().SHARED_PREFS,
                    Context.MODE_PRIVATE
            )
            val buttonText = prefs.getString(
                    ExampleAppWidgetConfig().KEY_BUTTON_TEXT + appWidgetId,
                    "Press me"
            )
            val serviceIntent = Intent(context, ExampleWidgetService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

            val clickIntent =
                Intent(
                        context, ExampleWidgetProvider::class.java
                )
            clickIntent.action = ACTION_TOAST

            val clickPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0, clickIntent, 0
            )

            val refresh = Intent(context, ExampleWidgetService::class.java)
            val refreshIntent = PendingIntent.getBroadcast(context, 0, refresh, 0)


            val views = RemoteViews(context.packageName, R.layout.example_widget)
            views.setRemoteAdapter(R.id.example_widget_stack_view, serviceIntent)
            views.setOnClickPendingIntent(R.id.btn_refresh, refreshIntent)
            views.setEmptyView(R.id.example_widget_stack_view, R.id.example_widget_empty_view)
            views.setPendingIntentTemplate(R.id.example_widget_stack_view, clickPendingIntent)
            
            val appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId)
            resizeWidget(appWidgetOptions, views)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.example_widget_stack_view)
        }
    }

    override fun onAppWidgetOptionsChanged(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            newOptions: Bundle
    ) {
        val views = RemoteViews(context.packageName, R.layout.example_widget)
        resizeWidget(newOptions, views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun resizeWidget(appWidgetOptions: Bundle, views: RemoteViews) {
        val minWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val maxWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val minHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        Toast.makeText(context, "onDeleted", Toast.LENGTH_SHORT).show()
    }

    override fun onEnabled(context: Context?) {
        Toast.makeText(context, "onEnabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context?) {
        Toast.makeText(context, "onDisabled", Toast.LENGTH_SHORT).show()
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (ACTION_TOAST == intent.action) {
            val clickedPosition = intent.getIntExtra(EXTRA_ITEM_POSITION, 0)
            Toast.makeText(context, "Clicked position: $clickedPosition", Toast.LENGTH_SHORT).show()
        }
        super.onReceive(context, intent)
    }

}