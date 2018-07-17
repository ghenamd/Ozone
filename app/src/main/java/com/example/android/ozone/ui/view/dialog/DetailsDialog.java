package com.example.android.ozone.ui.view.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.ozone.R;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.ui.view.adapter.DetailsDialogAdapter;
import com.example.android.ozone.utils.constants.OzoneConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsDialog extends AppCompatActivity {
    @BindView(R.id.recycler_details_dialog)
    RecyclerView mRecyclerView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_dialog);
        ButterKnife.bind(this);
        JsonData dt = getIntent().getParcelableExtra(OzoneConstants.DETAILS);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        DetailsDialogAdapter adapter = new DetailsDialogAdapter(dt,this);
        mRecyclerView.setAdapter(adapter);
    }
}
