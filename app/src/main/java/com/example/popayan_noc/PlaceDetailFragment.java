package com.example.popayan_noc;

import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;

public class PlaceDetailFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_detail, container, false);
        ImageView ivPlaceImage = view.findViewById(R.id.ivPlaceImage);
        TextView tvPlaceName = view.findViewById(R.id.tvPlaceName);
        TextView tvPlaceDescription = view.findViewById(R.id.tvPlaceDescription);
        TextView tvPlaceLocation = view.findViewById(R.id.tvPlaceLocation);
        RecyclerView rvEvents = view.findViewById(R.id.rvEvents);
        rvEvents.setVisibility(View.GONE); // Oculto por defecto hasta cargar
        Button btnSeeReviews = view.findViewById(R.id.btnSeeReviews);

        Bundle args = getArguments();
        if (args != null) {
            tvPlaceName.setText(args.getString("lugarNombre", "-"));
            tvPlaceDescription.setText(args.getString("lugarDescripcion", "Sin descripción"));
            tvPlaceLocation.setText(args.getString("lugarUbicacion", "Sin ubicación"));
            String imagen = args.getString("lugarImagen", "");
            if (!imagen.isEmpty()) {
                Glide.with(this).load(imagen).placeholder(R.drawable.placeholder_img).into(ivPlaceImage);
            } else {
                ivPlaceImage.setImageResource(R.drawable.placeholder_img);
            }
            // Cargar eventos reales del backend
            int lugarId = args.getInt("lugarId", 1);
            String token = AuthUtils.getToken(requireContext());
            EventApi.getEventosByLugar(requireContext(), token, lugarId, response -> {
                if (response.length() == 0) {
                    rvEvents.setVisibility(View.GONE);
                } else {
                    rvEvents.setVisibility(View.VISIBLE);
                    rvEvents.setAdapter(new EventAdapter(requireContext(), response));
                    rvEvents.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
                }
            }, error -> {
                rvEvents.setVisibility(View.GONE);
            });
        }

        btnSeeReviews.setOnClickListener(v -> {
            Fragment fragment = new ReviewsFragment();
            Bundle reviewsArgs = new Bundle();
            reviewsArgs.putInt("lugarId", args != null ? args.getInt("lugarId", 1) : 1);
            reviewsArgs.putString("lugarNombre", args != null ? args.getString("lugarNombre", "") : "");
            fragment.setArguments(reviewsArgs);
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit();
        });

        return view;
    }
}
