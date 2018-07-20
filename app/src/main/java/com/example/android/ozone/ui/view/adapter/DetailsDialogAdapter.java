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

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsDialogAdapter extends RecyclerView.Adapter<DetailsDialogAdapter.ViewHolder> {

    private JsonData mData;
    private Context mContext;
    private DetailsDialogAdapter.ViewHolder holder;
    public DetailsDialogAdapter(JsonData data,Context context) {
        mData = data;
        mContext = context;
    }

    @NonNull
    @Override
    public DetailsDialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_details_dialog,parent,false);

        return new DetailsDialogAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsDialogAdapter.ViewHolder hold, int position) {
        holder = hold;
        hold.city.setText(mData.getCity());
        hold.country.setText(mData.getState() +", " +mData.getCountry());
        hold.humidityPercentage.setText(String.valueOf(mData.getHu()+ "%"));
        hold.windSpeed.setText(String.valueOf(mData.getWs() + "m"));
        hold.temperature.setText(String.valueOf(mData.getTp() + "C"));
        setWeatherConditionLogo(mData,hold);
        int aqi;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String aqiValue = sharedPreferences.getString("Aqi","Us");
        if (aqiValue.equals("Us")){
            aqi = mData.getAqius();
            hold.aqiNumber.setText(String.valueOf(mData.getAqius()));
            holder.dialogUsCn.setText(R.string.us_lable_aqi);
            setViewHolder(aqi);

        }else if(aqiValue.equals("Cn")){
            aqi = mData.getAqicn();
            hold.aqiNumber.setText(String.valueOf(mData.getAqicn()));
            holder.dialogUsCn.setText(R.string.cn_lable_aqi);
            setViewHolder(aqi);
        }

    }
    //Helper method to change textViews content depending on Aqi value
    private void setViewHolder(int aqi){
        if (aqi <= 50){
            holder.circleView.setImageResource(R.drawable.green_circle);
            holder.airCondition.setText(R.string.good);

        }else if (aqi>50&&aqi<=100){
            holder.circleView.setImageResource(R.drawable.yellow_circle);
            holder.airCondition.setText(R.string.moderate);

        }else if (aqi>100 && aqi<=150){
            holder.circleView.setImageResource(R.drawable.orange_circle);
            holder.airCondition.setText(R.string.unhealthy);

        }else if (aqi>150 && aqi<=200){
            holder.circleView.setImageResource(R.drawable.red_circle);
            holder.airCondition.setText(R.string.unhealthy);

        }else if (aqi>200 && aqi<=300){
            holder.circleView.setImageResource(R.drawable.purple_circle);
            holder.airCondition.setText(R.string.very_unhealthy);
        }else if (aqi>300 && aqi<=500){
            holder.circleView.setImageResource(R.drawable.maroon_circle);
            holder.airCondition.setText(R.string.hazardous);

        }
    }
    //Helper method to change weatherLogo depending on server reply
    private void setWeatherConditionLogo(JsonData jsonData, DetailsDialogAdapter.ViewHolder holder){
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

    public class ViewHolder extends RecyclerView.ViewHolder  {
        @BindView(R.id.dialog_us_cn)
        TextView dialogUsCn;
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
        @BindView(R.id.temperature)
        TextView temperature;
        @BindView(R.id.humidity_percentage)
        TextView humidityPercentage;
        @BindView(R.id.wind_speed)
        TextView windSpeed;
        @BindView(R.id.details_weather_condition)
        ImageView weatherCondition;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

    }

}
