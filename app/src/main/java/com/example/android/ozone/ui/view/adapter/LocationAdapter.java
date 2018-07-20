package com.example.android.ozone.ui.view.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder>  {
    private static final String TAG = "LocationAdapter";
    private JsonData mData;
    private Context mContext;
    private LocationAdapter.ViewHolder holder;

    public LocationAdapter(JsonData data, Context context) {
        mData = data;
        mContext = context;


    }

    @NonNull
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new LocationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationAdapter.ViewHolder hold, int position) {
        holder = hold;
        holder.city.setText(mData.getCity());
        holder.country.setText(mData.getState() + ", " + mData.getCountry());
        holder.humidityPercentage.setText(String.valueOf(mData.getHu() + "%"));
        holder.windSpeed.setText(String.valueOf(mData.getWs() + "m/s"));
        holder.temperature.setText(String.valueOf(mData.getTp() + "C"));
        holder.date.setText(formatDate(mData.getTs()));
        setAqiValue(mData, hold);
        setWeatherConditionLogo(mData,hold);


    }
    //Helper method to change textViews content depending on Aqi value
    private void setAqiValue(JsonData jsonData, LocationAdapter.ViewHolder holder) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String aqiValue = sharedPreferences.getString("Aqi", "Us");
        if (aqiValue.equals("Us")) {
            holder.mAqiUsCn.setText(R.string.us_lable_aqi);
            holder.aqiNumber.setText(String.valueOf(mData.getAqius()));
            if (jsonData.getAqius() <= 50) {
                holder.circleView.setImageResource(R.drawable.green_circle);
                holder.airCondition.setText(R.string.good);
                holder.airDescription.setText(R.string.desc_good);
            } else if (jsonData.getAqius() > 50 && jsonData.getAqius() <= 100) {
                holder.circleView.setImageResource(R.drawable.yellow_circle);
                holder.airCondition.setText(R.string.moderate);
                holder.airDescription.setText(R.string.desc_moderate);
            } else if (jsonData.getAqius() > 100 && jsonData.getAqius() <= 150) {
                holder.circleView.setImageResource(R.drawable.orange_circle);
                holder.airCondition.setText(R.string.unhealthy);
                holder.airDescription.setText(R.string.desc_unhealthy);
            } else if (jsonData.getAqius() > 150 && jsonData.getAqius() <= 200) {
                holder.circleView.setImageResource(R.drawable.red_circle);
                holder.airCondition.setText(R.string.unhealthy);
                holder.airDescription.setText(R.string.desc_unhealthy);
            } else if (jsonData.getAqius() > 200 && jsonData.getAqius() <= 300) {
                holder.circleView.setImageResource(R.drawable.purple_circle);
                holder.airCondition.setText(R.string.very_unhealthy);
            } else if (jsonData.getAqius() > 300 && jsonData.getAqius() <= 500) {
                holder.circleView.setImageResource(R.drawable.maroon_circle);
                holder.airCondition.setText(R.string.hazardous);
                holder.airDescription.setText(R.string.desc_hazardous);
            }
        } else if (aqiValue.equals("Cn")) {
            holder.mAqiUsCn.setText(R.string.cn_lable_aqi);
            holder.aqiNumber.setText(String.valueOf(mData.getAqicn()));
            if (jsonData.getAqicn() <= 50) {
                holder.circleView.setImageResource(R.drawable.green_circle);
                holder.airCondition.setText(R.string.good);
                holder.airDescription.setText(R.string.desc_good);
            } else if (jsonData.getAqicn() > 50 && jsonData.getAqicn() <= 100) {
                holder.circleView.setImageResource(R.drawable.yellow_circle);
                holder.airCondition.setText(R.string.moderate);
                holder.airDescription.setText(R.string.desc_moderate);
            } else if (jsonData.getAqicn() > 100 && jsonData.getAqicn() <= 150) {
                holder.circleView.setImageResource(R.drawable.orange_circle);
                holder.airCondition.setText(R.string.unhealthy);
                holder.airDescription.setText(R.string.desc_unhealthy);
            } else if (jsonData.getAqicn() > 150 && jsonData.getAqicn() <= 200) {
                holder.circleView.setImageResource(R.drawable.red_circle);
                holder.airCondition.setText(R.string.unhealthy);
                holder.airDescription.setText(R.string.desc_unhealthy);
            } else if (jsonData.getAqicn() > 200 && jsonData.getAqicn() <= 300) {
                holder.circleView.setImageResource(R.drawable.purple_circle);
                holder.airCondition.setText(R.string.very_unhealthy);
            } else if (jsonData.getAqicn() > 300 && jsonData.getAqicn() <= 500) {
                holder.circleView.setImageResource(R.drawable.maroon_circle);
                holder.airCondition.setText(R.string.hazardous);
                holder.airDescription.setText(R.string.desc_hazardous);
            }
        }
    }
    //Helper method to change weatherLogo depending on server reply
    private void setWeatherConditionLogo(JsonData jsonData, LocationAdapter.ViewHolder holder){
        String weatherCondLogo = jsonData.getIc();
        switch (weatherCondLogo) {
            case "01d":
                holder.weatherCondition.setImageResource(R.drawable.d01);
                break;
            case "01n":
                holder.weatherCondition.setImageResource(R.drawable.n01);
                break;
            case "02d":
                holder.weatherCondition.setImageResource(R.drawable.d02);
                break;
            case "02n":
                holder.weatherCondition.setImageResource(R.drawable.n02);
                break;
            case "03d":
                holder.weatherCondition.setImageResource(R.drawable.d03);
                break;
            case "04d":
                holder.weatherCondition.setImageResource(R.drawable.d04);
                break;
            case "09d":
                holder.weatherCondition.setImageResource(R.drawable.d09);
                break;
            case "10d":
                holder.weatherCondition.setImageResource(R.drawable.d10);
                break;
            case "10n":
                holder.weatherCondition.setImageResource(R.drawable.n10);
                break;
            case "11d":
                holder.weatherCondition.setImageResource(R.drawable.d11);
                break;
            case "13d":
                holder.weatherCondition.setImageResource(R.drawable.d13);
                break;
            case "50d":
                holder.weatherCondition.setImageResource(R.drawable.d50);
                break;
            default:holder.weatherCondition.setImageResource(R.drawable.d01);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public JsonData getCurrentLocation() {
        return mData;
    }

    public void addData(JsonData data) {
        mData = data;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.aqi_us_cn)
        TextView mAqiUsCn;
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
            ButterKnife.bind(this, itemView);
        }
    }

    private String formatDate(String str) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
        Date date = null;
        try {
            date = inputFormat.parse(str);
        } catch (ParseException e) {

            e.printStackTrace();
        }
        return outputFormat.format(date);
    }

}
