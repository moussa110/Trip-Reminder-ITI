package com.example.tripreminderapp.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripreminderapp.R;
import com.example.tripreminderapp.database.trip.Trip;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{

    private List<Trip> data = null;
    public OnDeletTripListener setOnDeletTripListener = null;
    public OnShowNoteListener setOnShowNoteListener = null;

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
        holder.startPointTv.setText(trip.getStartPoint());
        holder.endPointTv.setText(trip.getEndPoint());
        holder.dateTv.setText(trip.getDate());
        holder.timeTv.setText(trip.getTime());
        int status=0;
        if (trip.isDone())
            status = R.string.Done;

        if (trip.isCanceled())
            status= R.string.cancel;

        holder.statusTv.setText(status);

        if(setOnDeletTripListener != null){
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setOnDeletTripListener.onClick(trip);
                }
            });
        }
        if (setOnShowNoteListener != null){
            holder.showNotesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setOnShowNoteListener.onClick(trip);
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







    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv;
        TextView startPointTv;
        TextView endPointTv;
        TextView timeTv;
        TextView dateTv;
        TextView statusTv;
        ImageButton showNotesBtn;
        Button deleteBtn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.item_tv_name);
            startPointTv = itemView.findViewById(R.id.item_tv_start);
            endPointTv = itemView.findViewById(R.id.item_tv_end);
            dateTv = itemView.findViewById(R.id.item_tv_date);
            timeTv = itemView.findViewById(R.id.item_tv_time);
            statusTv = itemView.findViewById(R.id.item_tv_status);
            showNotesBtn = itemView.findViewById(R.id.item_btn_add_notes);
            deleteBtn = itemView.findViewById(R.id.delete_trip);
        }
    }
    public interface OnDeletTripListener {
        void onClick(Trip trip);
    }

    public interface OnShowNoteListener{
        void onClick(Trip trip);
    }
}
