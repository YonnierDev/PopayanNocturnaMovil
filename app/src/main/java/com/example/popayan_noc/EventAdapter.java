package com.example.popayan_noc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private JSONArray eventList;
    private Context context;

    public EventAdapter(Context context, JSONArray eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        JSONObject evento = eventList.optJSONObject(position);
        if (evento != null) {
            holder.tvEventName.setText(evento.optString("nombre", "Evento"));
            holder.tvEventDate.setText(evento.optString("fecha", ""));
            holder.tvEventDescription.setText(evento.optString("descripcion", ""));
        }
    }

    @Override
    public int getItemCount() {
        return eventList.length();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventDescription;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
        }
    }
}
