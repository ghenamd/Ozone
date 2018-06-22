package com.example.android.ozone.ui.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.example.android.ozone.model.JsonData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private static final String TAG = "LocationAdapter";
    private List <JsonData> mJsonData;

    public LocationAdapter() {

    }

    @NonNull
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location,parent,false);
        return new LocationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationAdapter.ViewHolder holder, int position) {
        JsonData mData = mJsonData.get(position);
        holder.aqiNumber.setText(String.valueOf(mData.getAqius()));
        holder.city.setText(mData.getCity());
        holder.country.setText(mData.getState() +", " +mData.getCountry());
        holder.humidityPercentage.setText(String.valueOf(mData.getHu()+ "%"));
        holder.windSpeed.setText(String.valueOf(mData.getWs() + "m/s"));
        holder.temperature.setText(String.valueOf(mData.getTp() + "C"));
        holder.date.setText(formatDate(mData.getTs()));
       //holder.date.setText(formatDate(mData.getTs()));
        if (mData.getAqius() <= 50){
            holder.circleView.setImageResource(R.drawable.green_circle);
            holder.airCondition.setText(R.string.good);
            holder.airDescription.setText(R.string.desc_good);
        }else if (mData.getAqius()>50&&mData.getAqius()<=100){
            holder.circleView.setImageResource(R.drawable.yellow_circle);
            holder.airCondition.setText(R.string.moderate);
            holder.airDescription.setText(R.string.desc_moderate);
        }else if (mData.getAqius()>100 && mData.getAqius()<=150){
            holder.circleView.setImageResource(R.drawable.orange_circle);
            holder.airCondition.setText(R.string.unhealthy);
            holder.airDescription.setText(R.string.desc_unhealthy);
        }else if (mData.getAqius()>150 && mData.getAqius()<=200){
            holder.circleView.setImageResource(R.drawable.red_circle);
            holder.airCondition.setText(R.string.unhealthy);
            holder.airDescription.setText(R.string.desc_unhealthy);
        }else if (mData.getAqius()>200 && mData.getAqius()<=300){
            holder.circleView.setImageResource(R.drawable.purple_circle);
            holder.airCondition.setText(R.string.very_unhealthy);
        }else if (mData.getAqius()>300 && mData.getAqius()<=500){
            holder.circleView.setImageResource(R.drawable.maroon_circle);
            holder.airCondition.setText(R.string.hazardous);
            holder.airDescription.setText(R.string.desc_hazardous);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }
    public List<JsonData> getLocations(){return mJsonData;}
    public void addData(List<JsonData> data){
        mJsonData = data;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.air_condition)
        TextView airCondition;
        @BindView(R.id.circleView)
        ImageView circleView;
        @BindView(R.id.aqi_number)
        TextView aqiNumber;
        @BindView(R.id.city)
        TextView city;
        @BindView(R.id.country)
        TextView country;
        @BindView(R.id.weather_condition)
        ImageView weatherCondition;
        @BindView(R.id.temperature)
        TextView temperature;
        @BindView(R.id.humidity)
        ImageView humidity;
        @BindView(R.id.humidity_percentage)
        TextView humidityPercentage;
        @BindView(R.id.wind)
        ImageView wind;
        @BindView(R.id.wind_speed)
        TextView windSpeed;
        @BindView(R.id.air_description)
        TextView airDescription;
        @BindView(R.id.date)
        TextView date;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
    private  String formatDate(String str)  {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy",Locale.UK);
        Date date = null;
        try {
            date = inputFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputFormat.format(date);
    }
}
