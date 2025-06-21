package com.example.popayan_noc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FeaturedPlaceAdapter extends RecyclerView.Adapter<FeaturedPlaceAdapter.PlaceViewHolder> {
    private List<Place> places;
    private Context context;

    public FeaturedPlaceAdapter(List<Place> places, Context context) {
        this.places = places;
        this.context = context;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        // Animación de entrada: slide-in + fade-in
        if (holder.itemView.getAnimation() == null) {
            android.view.animation.Animation anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
            holder.itemView.startAnimation(anim);
        }
        Place place = places.get(position);
        holder.tvPlaceName.setText(place.nombre);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void setPlaces(List<Place> newPlaces) {
        this.places = newPlaces;
        notifyDataSetChanged();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaceName;
        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
        }
    }
}
