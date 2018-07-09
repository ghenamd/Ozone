package com.example.android.ozone.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.android.ozone.R;

public class OzoneWidgetIntentService extends IntentService {
    private static final String TAG = "OzoneWidgetIntentService";
    private static final String ACTION_UPDATE = "Action_Update";

    public OzoneWidgetIntentService() {
        super(TAG);
    }

    public static void startUpdateOzoneWidget(Context context) {
        Intent intent = new Intent(context, OzoneWidgetIntentService.class);
        intent.setAction(ACTION_UPDATE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            if (ACTION_UPDATE.equals(intent.getAction())) {
                handleUpdateWidget();
            }
        }
    }

    private void handleUpdateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, OzoneWidget.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        OzoneWidget.updateOzoneWidget(this, appWidgetManager, appWidgetIds);
    }
}