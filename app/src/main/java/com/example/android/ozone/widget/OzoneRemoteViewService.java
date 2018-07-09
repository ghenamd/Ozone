package com.example.android.ozone.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.ozone.R;
import com.example.android.ozone.model.JsonData;

import java.util.List;

import static com.example.android.ozone.ui.view.fragment.FavouriteFragment.favData;

public class OzoneRemoteViewService extends RemoteViewsService {
    private static final String TAG = "OzoneRemoteViewService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new OzoneRemoteViewsFactory(this.getApplicationContext());
    }


    //Inner class remoteViewsFactory
    public class OzoneRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private List<JsonData> mDataList;

        public OzoneRemoteViewsFactory(Context applicationContext) {
            mContext = applicationContext;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            mDataList = favData;
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            if (mDataList == null) {
                return 0;
            } else {
                return mDataList.size();
            }
        }

        @Override
        public RemoteViews getViewAt(int i) {
            return null;
        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget);
            for (JsonData data: mDataList) {
                views.setTextViewText(R.id.widget_place, data.getCity());
                Log.d(TAG, "getLoadingView: " + data.getCity());
                views.setTextViewText(R.id.widget_aqi, String.valueOf(data.getAqius()));
                views.setTextViewText(R.id.widget_temperature, String.valueOf(data.getTp()));

            }
            return views;

        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

    }

}
