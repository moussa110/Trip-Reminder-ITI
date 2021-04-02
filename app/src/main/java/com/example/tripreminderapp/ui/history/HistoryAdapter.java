package com.example.tripreminderapp.ui.history;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripreminderapp.R;
import com.example.tripreminderapp.database.trip.Trip;
import com.example.tripreminderapp.ui.upcoming_trips.UpcomingTripAdapter;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{

    private List<Trip> data = null;

    public DeletTrip setDeletTrip = null;

    public void changeData(List<Trip> tripsData) {
        this.data = tripsData;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dash_trip, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Trip trip = data.get(position);
        holder.nameTv.setText(trip.getName());
        Log.e("mido","the item is "+ trip.getName());
        holder.startPointTv.setText(trip.getStartPoint());
        holder.endPointTv.setText(trip.getEndPoint());
        holder.dateTv.setText(trip.getDate());
        holder.timeTv.setText(trip.getTime());

        if(setDeletTrip != null){
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDeletTrip.onClick(trip);
                }
            });
        }


    }



    @Override
    public int getItemCount() {
        if (data == null)
            return 0;
        else
            return data.size();
    }

    public interface DeletTrip{
        void onClick(Trip trip);
    }





    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv;
        TextView startPointTv;
        TextView endPointTv;
        TextView timeTv;
        TextView dateTv;
        TextView statusTv;
        Button startBtn;
        Button deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.item_tv_name);
            startPointTv = itemView.findViewById(R.id.item_tv_start);
            endPointTv = itemView.findViewById(R.id.item_tv_end);
            dateTv = itemView.findViewById(R.id.item_tv_date);
            timeTv = itemView.findViewById(R.id.item_tv_time);
            statusTv = itemView.findViewById(R.id.item_tv_status);
            startBtn = itemView.findViewById(R.id.item_btn_start);
            deleteBtn = itemView.findViewById(R.id.item_btn_cancel);
        }
    }
}
