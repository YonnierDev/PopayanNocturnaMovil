package com.example.popayan_noc;

import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;

import org.osmdroid.views.MapView;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    // private MapView mapView;
    // private IMapController mapController;
    // El mapa se mostrará solo en un diálogo al pulsar el FAB

    private RecyclerView rvFeaturedPlaces;
    private RecyclerView rvEvents;
    private EventAdapter eventAdapter;
    private List<org.json.JSONArray> eventList = new ArrayList<>();
    private RequestQueue queue;
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";
    private TextView tvLugares;
    private TextView tvEventos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inicializa queue si no existe
        if (queue == null) queue = com.android.volley.toolbox.Volley.newRequestQueue(requireContext());
        rvFeaturedPlaces = view.findViewById(R.id.rvFeaturedPlaces);
        // Asegura el layout horizontal para el carrusel
        if (rvFeaturedPlaces != null) {
            rvFeaturedPlaces.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }
        rvEvents = view.findViewById(R.id.rvEvents);
        queue = Volley.newRequestQueue(requireContext());

        // Apartado de Eventos
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        cargarLugares(view);
        cargarEventos();

        // Estadísticas
        tvLugares = view.findViewById(R.id.tvLugares);
        tvEventos = view.findViewById(R.id.tvEventos);

        // Botón de Logout funcional
        Button btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
                android.content.Intent intent = new android.content.Intent(getActivity(), LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        }

        // El mapa ya no está en el layout principal. Se mostrará en un diálogo al pulsar el FAB.
        View fabMap = view.findViewById(R.id.fabMap);
        if (fabMap != null) {
            fabMap.setOnClickListener(v -> mostrarDialogoMapa());
        }
        return view;
    }

    // --- Cargar lugares reales ---
    private void cargarLugares(View view) {
        String url = BASE_URL + "/lugares";
        String token = AuthUtils.getToken(getContext());
        // rvFeaturedPlaces ya es variable de clase, no la redefinas aquí

        TextView tvNoLugares = view.findViewById(R.id.tvNoLugares);
        ImageView imgBannerLugares = view.findViewById(R.id.imgBannerLugares);
        if (token == null) {
            if (rvFeaturedPlaces != null) rvFeaturedPlaces.setVisibility(View.GONE);
            if (imgBannerLugares != null) imgBannerLugares.setVisibility(View.VISIBLE);
            if (tvNoLugares != null) tvNoLugares.setVisibility(View.VISIBLE);
            android.util.Log.e("HomeFragment", "Token de usuario no disponible. No se puede cargar lugares.");
            if (tvLugares != null) tvLugares.setText("0");
            android.widget.Toast.makeText(getContext(), "Token no disponible, no se cargan lugares", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        JsonArrayRequest request = new JsonArrayRequest(
            Request.Method.GET, url, null,
            response -> {
                android.util.Log.d("HomeFragment", "Respuesta lugares: " + response);
                if (response != null && response.length() > 0) {
                    List<Place> places = parsePlacesFromJson(response);
                    PlaceAdapter placeAdapter = new PlaceAdapter(getContext(), places);
                    if (rvFeaturedPlaces != null) {
                        rvFeaturedPlaces.setAdapter(placeAdapter);
                        rvFeaturedPlaces.setVisibility(View.VISIBLE);
                    }
                    if (imgBannerLugares != null) imgBannerLugares.setVisibility(View.GONE);
                    if (tvNoLugares != null) tvNoLugares.setVisibility(View.GONE);
                    if (tvLugares != null) tvLugares.setText(String.valueOf(places.size()));
                } else {
                    android.util.Log.w("HomeFragment", "No hay lugares disponibles. Respuesta JSON: " + response);
                    android.widget.Toast.makeText(getContext(), "No hay lugares disponibles. Respuesta: " + response, android.widget.Toast.LENGTH_LONG).show();
                    if (rvFeaturedPlaces != null) rvFeaturedPlaces.setVisibility(View.GONE);
                    if (imgBannerLugares != null) imgBannerLugares.setVisibility(View.VISIBLE);
                    if (tvNoLugares != null) tvNoLugares.setVisibility(View.GONE);
                    if (tvLugares != null) tvLugares.setText("0");
                }
            },
            error -> {
                android.util.Log.e("HomeFragment", "Error cargando lugares: " + error.toString());
                android.widget.Toast.makeText(getContext(), "Error cargando lugares: " + error.toString(), android.widget.Toast.LENGTH_LONG).show();
                if (rvFeaturedPlaces != null) rvFeaturedPlaces.setVisibility(View.GONE);
                if (imgBannerLugares != null) imgBannerLugares.setVisibility(View.VISIBLE);
                if (tvNoLugares != null) tvNoLugares.setVisibility(View.VISIBLE);
                if (tvLugares != null) tvLugares.setText("0");
            }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() throws com.android.volley.AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }

    // --- Cargar eventos reales ---
    private void cargarEventos() {
        String url = BASE_URL + "/eventos";
        String token = AuthUtils.getToken(getContext());
        if (token == null) {
            rvEvents.setVisibility(View.GONE);
            TextView tvNoEvents = getView().findViewById(R.id.tvNoEvents);
            if (tvNoEvents != null) tvNoEvents.setVisibility(View.VISIBLE);
            android.util.Log.e("HomeFragment", "Token de usuario no disponible. No se puede cargar eventos.");
            if (tvEventos != null) tvEventos.setText("0");
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET, url, null,
            response -> {
                try {
                    JSONArray eventosArray = response.optJSONArray("datos");
                    if (eventosArray != null && eventosArray.length() > 0) {
                        eventAdapter = new EventAdapter(getContext(), eventosArray);
                        rvEvents.setAdapter(eventAdapter);
                        if (tvEventos != null) tvEventos.setText(String.valueOf(eventosArray.length()));
                    } else {
                        if (tvEventos != null) tvEventos.setText("0");
                    }
                } catch (Exception e) {
                    if (tvEventos != null) tvEventos.setText("0");
                }
            },
            error -> {
                android.util.Log.e("HomeFragment", "Error cargando eventos: " + error.toString());
                rvEvents.setVisibility(View.GONE);
                TextView tvNoEvents = getView().findViewById(R.id.tvNoEvents);
                if (tvNoEvents != null) tvNoEvents.setVisibility(View.VISIBLE);
            }
        ) {
            public java.util.Map<String, String> getHeaders() throws com.android.volley.AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }

    // El método actualizarMarcadoresMapa solo se usará cuando el mapa esté visible en el diálogo.
    private void actualizarMarcadoresMapa(MapView mapView, IMapController mapController) {}

    private void mostrarDialogoMapa() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_map, null);
        org.osmdroid.views.MapView dialogMapView = dialogView.findViewById(R.id.osmMapViewDialog);
        dialogMapView.setMultiTouchControls(true);
        org.osmdroid.api.IMapController dialogMapController = dialogMapView.getController();
        dialogMapController.setZoom(15.0);
        actualizarMarcadoresMapa(dialogMapView, dialogMapController);
        builder.setView(dialogView);
        builder.setNegativeButton("Cerrar", (d, w) -> d.dismiss());
        builder.show();
    }

    // Método para convertir JSONArray a List<Place>
    private List<Place> parsePlacesFromJson(JSONArray response) {
        List<Place> places = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                Place place = new Place(
                    obj.getInt("id"),
                    obj.getInt("categoriaid"),
                    obj.getInt("usuarioid"),
                    obj.getString("nombre"),
                    obj.getString("descripcion"),
                    obj.getString("ubicacion"),
                    obj.getBoolean("estado"),
                    obj.getString("imagen"),
                    obj.getBoolean("aprobacion"),
                    obj.has("rating") && !obj.isNull("rating") ? obj.getDouble("rating") : null
                );
                if (obj.has("tipo") && !obj.isNull("tipo")) {
                    place.setTipo(obj.getString("tipo"));
                }
                places.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return places;
    }
}
