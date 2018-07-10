package com.example.android.ozone.ui.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.example.android.ozone.model.JsonData;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {
    private List<JsonData> mData;
    private OnLocationClicked mLocationClicked;

    public FavouriteAdapter(List<JsonData> dataList, OnLocationClicked clicked) {
        mData = dataList;
        mLocationClicked = clicked;
    }

    @NonNull
    @Override
    public FavouriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favourite,parent,false);
        return new FavouriteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonData jsonData = mData.get(position);
        String city = jsonData.getCity();
        int aqi = jsonData.getAqius();
        int temp = jsonData.getTp();
        holder.mAqi.setText("Aqi " +String.valueOf(aqi));
        holder.mCity.setText(city);
        holder.mTemp.setText(String.valueOf(temp) + "C");
        if (aqi <= 50){

            holder.mAirStatus.setText(R.string.good);
            holder.mDesc.setText(R.string.desc_good);
        }else if (aqi > 50 && aqi <=100){
            holder.mAirStatus.setText(R.string.moderate);
            holder.mDesc.setText(R.string.desc_moderate);
        }else if (aqi>100 && aqi<=150){

            holder.mAirStatus.setText(R.string.unhealthy);
            holder.mDesc.setText(R.string.desc_unhealthy);
        }else if (aqi>150 && aqi <=200){

            holder.mAirStatus.setText(R.string.unhealthy);
            holder.mDesc.setText(R.string.desc_unhealthy);
        }else if (aqi >200 && aqi <=300){

            holder.mAirStatus.setText(R.string.very_unhealthy);
        }else if (aqi >300 && aqi <=500){

            holder.mAirStatus.setText(R.string.hazardous);
            holder.mDesc.setText(R.string.desc_hazardous);
        }

    }
    public interface OnLocationClicked{
        void onItemClicked(JsonData data);
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
