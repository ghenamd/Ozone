package com.example.android.ozone.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.ui.view.adapter.LocationAdapter;

import java.util.List;
import java.util.Objects;

public class Helper {
    private static final String TAG = "Helper";
    public static JsonData getLastListItem(List<JsonData> data) {
        int n = data.size();
        if (n != 0) {
            int i = n - 1;
            return data.get(i);
        } else {
            return null;
        }
    }
    //Helper method to check if there is Internet connection
    public static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = Objects.requireNonNull(manager).getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }
    //Helper method to populate the UI
    public static void populateUi(JsonData jsonData, Context context,
                            LocationAdapter adapter, RecyclerView recyclerView, ProgressBar bar) {
        LinearLayoutManager manager = new LinearLayoutManager(context);
        adapter.addData(jsonData);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setAdapter(adapter);
        bar.setVisibility(View.INVISIBLE);
    }

    public static void showToastInserted(Context context, String city){
        Toast.makeText(context, city + " added", Toast.LENGTH_SHORT).show();
    }
    public static void showToastDeleted(Context context, String city){
        Toast.makeText(context, city+ " deleted", Toast.LENGTH_SHORT).show();
    }

}
