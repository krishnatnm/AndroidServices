package com.tanmay.androidservices.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.tanmay.androidservices.R;
import com.tanmay.androidservices.views.interfaces.OnListItemClickListener;
import com.tanmay.androidservices.views.adapters.DashboardAdapter;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements OnListItemClickListener{

    public static String TAG = "DashboardActivity";

    Context context;

    Toolbar toolbar;
    RecyclerView mRecyclerView;

    RecyclerView.Adapter mAdapter;

    RecyclerView.LayoutManager mLayoutManager;

    ArrayList<String> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        context = this;
        initView();
        setSupportActionBar(toolbar);

        itemList = new ArrayList<>();
        itemList.add("Set/Remove Multiple Alarms");

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DashboardAdapter(context, itemList);
        mRecyclerView.setAdapter(mAdapter);
        DashboardAdapter.onClick = this;
    }

    public void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.cm_recycler_view);
    }

    @Override
    public void onItemClick(View view) {
        int pos = mRecyclerView.getChildAdapterPosition(view);
        switch (pos) {
            case 0:
                startActivity(new Intent(context, ManageAlarms.class));
                break;
            default:
                startActivity(new Intent(context, ManageAlarms.class));
                Toast.makeText(context, "To be done!", Toast.LENGTH_SHORT).show();
        }
    }
}
