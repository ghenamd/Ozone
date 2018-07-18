package com.example.android.ozone.ui.view.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.android.ozone.R;

import com.example.android.ozone.ui.view.LocationActivity;
import com.example.android.ozone.utils.constants.OzoneConstants;

/**
 * Implementation of App Widget functionality.
 */
public class OzoneWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ozone_widget);

        Intent click = new Intent(context, LocationActivity.class);
        click.putExtra(OzoneConstants.WIDGET,1);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context,0,click,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_title,appPendingIntent);

        Intent intent = new Intent(context, OzoneRemoteViewService.class);
        views.setRemoteAdapter(R.id.widget_list_view, intent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        OzoneWidgetIntentService.startUpdateOzoneWidget(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void updateOzoneWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

}

