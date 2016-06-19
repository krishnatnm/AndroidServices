package com.tanmay.androidservices.views.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.tanmay.androidservices.R;
import com.tanmay.androidservices.utils.LocalStorage;
import com.tanmay.androidservices.views.interfaces.OnDialogButtonClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by TaNMay on 20/06/16.
 */
public class CustomDialogs {

    Context context;
    OnDialogButtonClickListener listener;

    public CustomDialogs(Context context, OnDialogButtonClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void reminderDialog() {
        final LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.dialog_set_alarm, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(true);
        final AlertDialog alertDialog = builder.create();

        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.dsa_time_picker);
        Button okay = (Button) view.findViewById(R.id.dsa_submit);

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int alarmId = LocalStorage.getInstance(context).getAlarmId();
                alarmId ++;

                int hour = timePicker.getCurrentHour();
                int min = timePicker.getCurrentMinute();
                String time = hour + ":" + min;

                alertDialog.dismiss();
                listener.onReminderOkayClick(time, alarmId);
            }
        });
        alertDialog.show();
    }
}
