package com.example.popayan_noc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.view.MotionEvent;
import android.widget.RatingBar;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {
    private Context context;
    private List<Place> placeList;

    public PlaceAdapter(Context context, List<Place> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.tvPlaceName.setText(place.nombre);
        holder.tvPlaceLocation.setText(place.ubicacion);
        holder.tvPlaceDistance.setText(""); // Puedes calcular distancia si tienes el dato
        holder.tvPlaceDates.setText(""); // Puedes mostrar fechas si tienes el dato
        holder.tvPlacePrice.setText(""); // Puedes mostrar precio si tienes el dato
        // Mostrar rating visual si hay dato
        if (place.rating != null && place.rating > 0) {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.tvPlaceRating.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(place.rating != null ? place.rating.floatValue() : 0f);
            holder.tvPlaceRating.setText(String.format("%.1f", place.rating));
        } else {
            holder.ratingBar.setVisibility(View.GONE);
            holder.tvPlaceRating.setVisibility(View.VISIBLE);
            holder.tvPlaceRating.setText("Sin calificaciones");
        }
        holder.tvTag.setVisibility(View.GONE); // Muestra la etiqueta si aplica
        // Cargar imagen con Glide
        if (place.imagen != null && !place.imagen.isEmpty()) {
            Glide.with(context).load(place.imagen).placeholder(R.drawable.placeholder_img).into(holder.imgPlace);
        } else {
            holder.imgPlace.setImageResource(R.drawable.placeholder_img);
        }
        // Listener para abrir ReviewsFragment con argumentos
        holder.cardView.setOnClickListener(v -> {
            androidx.fragment.app.Fragment fragment = new com.example.popayan_noc.PlaceDetailFragment();
            android.os.Bundle args = new android.os.Bundle();
            args.putInt("lugarId", place.id);
            args.putString("lugarNombre", place.nombre);
            args.putString("lugarDescripcion", place.descripcion != null ? place.descripcion : "");
            args.putString("lugarUbicacion", place.ubicacion != null ? place.ubicacion : "");
            args.putString("lugarImagen", place.imagen != null ? place.imagen : "");
            // Si tienes eventos asociados, pásalos aquí. Por ahora dummy:
            args.putString("lugarEventos", "No hay eventos asociados");
            fragment.setArguments(args);
            if (context instanceof androidx.fragment.app.FragmentActivity) {
                ((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace;
        TextView tvPlaceName, tvPlaceLocation, tvPlaceDistance, tvPlaceDates, tvPlacePrice, tvPlaceRating, tvTag;
        CardView cardView;
        RatingBar ratingBar;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            cardView = (CardView) itemView;
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvPlaceRating = itemView.findViewById(R.id.tvPlaceRating);
            // Animación de escala al presionar
            cardView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cardView.animate().scaleX(0.97f).scaleY(0.97f).setDuration(120).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cardView.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        break;
                }
                return false;
            });
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceLocation = itemView.findViewById(R.id.tvPlaceLocation);
            tvPlaceDistance = itemView.findViewById(R.id.tvPlaceDistance);
            tvPlaceDates = itemView.findViewById(R.id.tvPlaceDates);
            tvPlacePrice = itemView.findViewById(R.id.tvPlacePrice);
            tvPlaceRating = itemView.findViewById(R.id.tvPlaceRating);
            tvTag = itemView.findViewById(R.id.tvTag);
            cardView = (CardView) itemView;
        }
    }
}
