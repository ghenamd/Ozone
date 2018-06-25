package com.example.android.ozone.network;

import com.example.android.ozone.model.JsonData;

public interface AsyncResponse {
    void processFinished(JsonData jsonData);
}
