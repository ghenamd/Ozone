package com.example.android.ozone.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.ozone.R;
import com.example.android.ozone.model.JsonData;

import java.util.List;

import static com.example.android.ozone.ui.view.fragment.FavouriteFragment.favData;

public class OzoneRemoteViewService extends RemoteViewsService {
public static final String TEMP= "Temp ";
public static final String AQI= "Aqi ";
public static final String CELSIUS= "C";

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
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget);

            JsonData data = mDataList.get(position);
            views.setTextViewText(R.id.widget_place, data.getCity());
            views.setTextViewText(R.id.widget_aqi, AQI+ String.valueOf(data.getAqius()));
            views.setTextViewText(R.id.widget_temperature, TEMP + String.valueOf(data.getTp()) + CELSIUS);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

    }


}
