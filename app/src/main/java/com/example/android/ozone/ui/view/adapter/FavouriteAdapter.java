package com.example.android.ozone.ui.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.utils.animator.AnimatorUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {

    private List<JsonData> mData;
    private OnLocationClicked mLocationClicked;
    private int prevPosition = 0;
    private Context mContext;
    private static final String AQI_US = "Us Aqi-";
    private static final String AQI_CN = "Cn Aqi-";

    public FavouriteAdapter(List<JsonData> dataList, OnLocationClicked clicked, Context context) {
        mData = dataList;
        mLocationClicked = clicked;
        mContext = context;
    }

    @NonNull
    @Override
    public FavouriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favourite,parent,false);
        return new FavouriteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String aqiValue = pref.getString("Aqi","Us");
        JsonData jsonData = mData.get(position);
        final String city = jsonData.getCity();

        int temp = jsonData.getTp();
        holder.mCity.setText(city);
        holder.mTemp.setText(String.valueOf(temp) + "C");
        int aqi;
        if (aqiValue.equals("Us")){
            aqi = jsonData.getAqius();
            holder.mAqi.setText(AQI_US +String.valueOf(aqi));
            setViewHolder(aqi,holder);
        }else if (aqiValue.equals("Cn")){
            aqi = jsonData.getAqicn();
            holder.mAqi.setText(AQI_CN +String.valueOf(aqi));
            setViewHolder(aqi,holder);
        }

        if (position > prevPosition){
            AnimatorUtil.animate(holder,true);
        }else{
            AnimatorUtil.animate(holder,false);
        }
        prevPosition = position;
        holder.mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,"In " +
                        holder.mCity.getText() + " "+
                        holder.mDesc.getText());
                shareIntent.setType("text/plain");
                mContext.startActivity(Intent.createChooser(shareIntent, "Send To"));
            }
        });

    }

    public interface OnLocationClicked{
        void onItemClicked(JsonData data);
    }

    private void setViewHolder(int aqi,ViewHolder holder){
        if (aqi <= 50){
            holder.mAirStatus.setText(R.string.good);
            holder.mAirStatus.setTextColor(ContextCompat.getColor(mContext,R.color.green));
            holder.mDesc.setText(R.string.desc_good);
        }else if (aqi > 50 && aqi <=100){
            holder.mAirStatus.setText(R.string.moderate);
            holder.mAirStatus.setTextColor(ContextCompat.getColor(mContext,R.color.yellow));
            holder.mDesc.setText(R.string.desc_moderate);
        }else if (aqi>100 && aqi<=150){

            holder.mAirStatus.setText(R.string.unhealthy);
            holder.mAirStatus.setTextColor(ContextCompat.getColor(mContext,R.color.orange));
            holder.mDesc.setText(R.string.desc_unhealthy);
        }else if (aqi>150 && aqi <=200){

            holder.mAirStatus.setText(R.string.unhealthy);
            holder.mAirStatus.setTextColor(ContextCompat.getColor(mContext,R.color.red));
            holder.mDesc.setText(R.string.desc_unhealthy);
        }else if (aqi >200 && aqi <=300){

            holder.mAirStatus.setText(R.string.very_unhealthy);
            holder.mAirStatus.setTextColor(ContextCompat.getColor(mContext,R.color.purple));
            holder.mDesc.setText(R.string.desc_unhealthy);
        }else if (aqi >300 && aqi <=500){

            holder.mAirStatus.setText(R.string.hazardous);
            holder.mAirStatus.setTextColor(ContextCompat.getColor(mContext,R.color.maroon));
            holder.mDesc.setText(R.string.desc_hazardous);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    public void addData(List<JsonData> data){
        mData = data;
    }
    public List<JsonData> getData(){
        return mData;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.favourite_city)
        TextView mCity;
        @BindView(R.id.favourite_air_status)
        TextView mAirStatus;
        @BindView(R.id.favourite_temp)
        TextView mTemp;
        @BindView(R.id.favourite_desc)
        TextView mDesc;
        @BindView(R.id.favourite_aqi)
        TextView mAqi;
        @BindView(R.id.share)
        ImageButton mShare;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            JsonData jd = mData.get(position);
            mLocationClicked.onItemClicked(jd);
        }
    }
}
