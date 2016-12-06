package com.tanmay.androidservices.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tanmay.androidservices.R;
import com.tanmay.androidservices.interfaces.OnAlarmItemClickListener;
import com.tanmay.androidservices.models.AlarmItem;

import java.util.ArrayList;

/**
 * Created by TaNMay on 20/06/16.
 */
public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {

    Context context;
    ArrayList<AlarmItem> items;
    public static OnAlarmItemClickListener onClick;

    public AlarmsAdapter(Context context, ArrayList<AlarmItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.alarmName.setText(items.get(position).getName());
        holder.deleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView alarmName, deleteAlarm;

        public ViewHolder(View itemView) {
            super(itemView);
            alarmName = (TextView) itemView.findViewById(R.id.ai_title);
            deleteAlarm = (TextView) itemView.findViewById(R.id.ai_delete);
        }

    }
}
