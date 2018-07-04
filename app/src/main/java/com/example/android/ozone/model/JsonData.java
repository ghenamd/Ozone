package com.example.android.ozone.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "location")
public class JsonData implements Parcelable {

    @PrimaryKey
    @NonNull
    @SerializedName("city")
    @Expose
    private String city;

    @SerializedName("state")
    @Expose
    private String state;

    @SerializedName("country")
    @Expose
    private String country;

    @SerializedName("ts")
    @Expose
    private String ts;

    @SerializedName("hu")
    @Expose
    private int hu;

    @SerializedName("ic")
    @Expose
    private String ic;

    @SerializedName("pr")
    @Expose
    private int pr;

    @SerializedName("tp")
    @Expose
    private int tp;

    @SerializedName("wd")
    @Expose
    private int wd;

    @SerializedName("ws")
    @Expose
    private double ws;

    @SerializedName("aqius")
    @Expose
    private int aqius;

    @SerializedName("aqicn")
    @Expose
    private int aqicn;

    @Ignore
    public JsonData() {
    }


    public JsonData(String city, String state, String country, String ts, int hu,
                    String ic, int pr, int tp, int wd, double ws, int aqius, int aqicn) {
        this.city = city;
        this.state = state;
        this.country = country;
        this.ts = ts;
        this.hu = hu;
        this.ic = ic;
        this.pr = pr;
        this.tp = tp;
        this.wd = wd;
        this.ws = ws;
        this.aqius = aqius;
        this.aqicn = aqicn;
    }


    protected JsonData(Parcel in) {
        city = in.readString();
        state = in.readString();
        country = in.readString();
        ts = in.readString();
        hu = in.readInt();
        ic = in.readString();
        pr = in.readInt();
        tp = in.readInt();
        wd = in.readInt();
        ws = in.readDouble();
        aqius = in.readInt();
        aqicn = in.readInt();
    }

    public static final Creator<JsonData> CREATOR = new Creator<JsonData>() {
        @Override
        public JsonData createFromParcel(Parcel in) {
            return new JsonData(in);
        }

        @Override
        public JsonData[] newArray(int size) {
            return new JsonData[size];
        }
    };

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public int getHu() {
        return hu;
    }

    public void setHu(int hu) {
        this.hu = hu;
    }

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public int getTp() {
        return tp;
    }

    public void setTp(int tp) {
        this.tp = tp;
    }

    public int getWd() {
        return wd;
    }

    public void setWd(int wd) {
        this.wd = wd;
    }

    public double getWs() {
        return ws;
    }

    public void setWs(double ws) {
        this.ws = ws;
    }

    public int getAqius() {
        return aqius;
    }

    public void setAqius(int aqius) {
        this.aqius = aqius;
    }

    public int getAqicn() {
        return aqicn;
    }

    public void setAqicn(int aqicn) {
        this.aqicn = aqicn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(city);
        parcel.writeString(state);
        parcel.writeString(country);
        parcel.writeString(ts);
        parcel.writeInt(hu);
        parcel.writeString(ic);
        parcel.writeInt(pr);
        parcel.writeInt(tp);
        parcel.writeInt(wd);
        parcel.writeDouble(ws);
        parcel.writeInt(aqius);
        parcel.writeInt(aqicn);
    }
}
