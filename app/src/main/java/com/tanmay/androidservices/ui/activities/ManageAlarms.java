package com.tanmay.androidservices.ui.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tanmay.androidservices.R;
import com.tanmay.androidservices.services.NotificationService;
import com.tanmay.androidservices.utils.LocalStorage;
import com.tanmay.androidservices.adapters.AlarmsAdapter;
import com.tanmay.androidservices.ui.dialogs.CustomDialogs;
import com.tanmay.androidservices.interfaces.OnAlarmItemClickListener;
import com.tanmay.androidservices.interfaces.OnDialogButtonClickListener;
import com.tanmay.androidservices.models.AlarmItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ManageAlarms extends AppCompatActivity
        implements OnAlarmItemClickListener, OnDialogButtonClickListener {

    public static String TAG = "ManageAlarms ==>";

    Context context;

    Toolbar toolbar;
    RecyclerView mRecyclerView;
    TextView noAlarmMsg;

    RecyclerView.Adapter mAdapter;
    CustomDialogs customDialogs;

    RecyclerView.LayoutManager mLayoutManager;

    ArrayList<AlarmItem> alarmItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_alarms);

        context = this;
        initView();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        alarmItems = new ArrayList<AlarmItem>();
        customDialogs = new CustomDialogs(context, this);

        String alarmsStr = LocalStorage.getInstance(context).getAlarms();
        if (alarmsStr != null) {
            try {
                JSONArray alarmsArray = new JSONArray(alarmsStr);
                for (int i = 0; i < alarmsArray.length(); i++) {
                    AlarmItem alarmItem = new AlarmItem();
                    alarmItem.setName(alarmsArray.optJSONObject(i).optString("name"));
                    alarmItem.setTime(alarmsArray.optJSONObject(i).optString("time"));
                    alarmItem.setAlarmId(alarmsArray.optJSONObject(i).optInt("id"));
                    alarmItems.add(alarmItem);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (alarmItems.size() != 0) noAlarmMsg.setVisibility(View.GONE);

        AlarmsAdapter.onClick = this;
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new AlarmsAdapter(context, alarmItems);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.cma_recycler_view);
        noAlarmMsg = (TextView) findViewById(R.id.cma_no_alarm_msg);
    }

    @Override
    public void onDeleteClick(int position) {
        int alarmId = alarmItems.get(position).getAlarmId();
        deleteNotification(alarmId);

        alarmItems.remove(position);
        mAdapter.notifyDataSetChanged();

        deleteAlarmFromStorage(position);

        if (alarmItems.size() == 0) noAlarmMsg.setVisibility(View.VISIBLE);

    }

    public void onAddAlarm(View view) {
        customDialogs.reminderDialog();
    }

    @Override
    public void onReminderOkayClick(String reminderTime, int alarmId) {
        AlarmItem alarmItem = new AlarmItem();
        alarmItem.setName(reminderTime);
        alarmItem.setTime(reminderTime);
        alarmItem.setAlarmId(alarmId);
        alarmItems.add(alarmItem);

        addAlarmToStorage(reminderTime, alarmId);
        setNotification(reminderTime, alarmId);

        mAdapter.notifyDataSetChanged();
        noAlarmMsg.setVisibility(View.GONE);
    }

    public void addAlarmToStorage(String reminderTime, int alarmId) {
        LocalStorage.getInstance(context).setAlarmId(alarmId);
        String alarmsStr = LocalStorage.getInstance(context).getAlarms();
        if (alarmsStr != null) {
            try {
                JSONArray alarmsArray = new JSONArray(alarmsStr);
                JSONObject alarmObj = new JSONObject();
                try {
                    alarmObj.put("name", reminderTime);
                    alarmObj.put("time", reminderTime);
                    alarmObj.put("id", alarmId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                alarmsArray.put(alarmObj);
                LocalStorage.getInstance(context).setAlarms(alarmsArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            JSONArray alarmsArray = new JSONArray();
            JSONObject alarmObj = new JSONObject();
            try {
                alarmObj.put("name", reminderTime);
                alarmObj.put("time", reminderTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            alarmsArray.put(alarmObj);
            LocalStorage.getInstance(context).setAlarms(alarmsArray.toString());
        }
    }

    public void deleteAlarmFromStorage(int position) {
        String alarmsStr = LocalStorage.getInstance(context).getAlarms();
        if (alarmsStr != null) {
            try {
                JSONArray alarmsArray = new JSONArray(alarmsStr);
                alarmsArray.remove(position);
                LocalStorage.getInstance(context).setAlarms(alarmsArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Some problem");
        }
    }

    public void setNotification(String alarmTime, int requestCode) {
        long frequency = 24 * 60 * 60 * 1000;                                                       // 1 day
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cal.setTime(sdf.parse(year + "-" + month + "-" + date.getDate() + " " + alarmTime + ":" + "00"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Time: " + cal.getTime());

        Intent mServiceIntent = new Intent(context, NotificationService.class);
        mServiceIntent.putExtra("CONTENT", alarmTime);
        PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, mServiceIntent, 0);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), frequency, pendingIntent);
    }

    public void deleteNotification(int requestCode) {
        Intent mServiceIntent = new Intent(context, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, mServiceIntent, 0);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }
}
