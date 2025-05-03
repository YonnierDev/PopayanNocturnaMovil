package com.example.popayan_noc;

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
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

// NUEVO MODELO PARA CATEGORÍA CON LUGARES
class CategoriaConLugares {
    String tipo;
    List<Place> lugares;
    CategoriaConLugares(String tipo, List<Place> lugares) {
        this.tipo = tipo;
        this.lugares = lugares;
    }
}

public class HomeFragment extends Fragment {
    // private MapView mapView;
    // private IMapController mapController;
    // El mapa se mostrará solo en un diálogo al pulsar el FAB

    private List<Place> currentPlaces = new ArrayList<>(); // Lugares actualmente visibles en la lista y el mapa

    private RecyclerView rvCategories, rvPlaces;
    private CategoryAdapter categoryAdapter;
    private PlaceAdapter placeAdapter;
    private List<CategoriaConLugares> categoriaList = new ArrayList<>();
    private List<Place> placeList = new ArrayList<>();
    private int selectedCategoryIndex = 0;
    private RequestQueue queue;
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.util.Log.d("HomeFragment", "onCreateView START");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvPlaces = view.findViewById(R.id.rvPlaces);
        queue = Volley.newRequestQueue(requireContext());

        // El mapa ya no está en el layout principal. Se mostrará en un diálogo al pulsar el FAB.
        View fabMap = view.findViewById(R.id.fabMap);
        if (fabMap != null) {
            fabMap.setOnClickListener(v -> mostrarDialogoMapa());
        }

        categoryAdapter = new CategoryAdapter(getContext(), new ArrayList<>(), category -> {
            int idx = findCategoryIndexByTipo(category.tipo);
            if (idx >= 0) {
                selectedCategoryIndex = idx;
                updatePlacesForSelectedCategory();
            }
        });

        // Siempre carga categorías locales si la lista está vacía
        categoryAdapter.setCategories(null); // fuerza categorías locales por defecto
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        placeAdapter = new PlaceAdapter(getContext(), placeList);
        rvPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlaces.setAdapter(placeAdapter);

        cargarCategoriasYlLugares();
        return view;
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

    private void cargarCategoriasYlLugares() {
        android.util.Log.d("HomeFragment", "cargarCategoriasYlLugares START");
        String url = BASE_URL + "/usuarios/lista-categorias";
        android.util.Log.d("HomeFragment", "URL categorías: " + url);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
            response -> {
                categoriaList.clear();
                List<String> tipos = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject obj = response.getJSONObject(i);
                        String tipo = obj.optString("tipo", "");
                        JSONArray lugaresArr = obj.optJSONArray("lugares");
                        List<Place> lugares = new ArrayList<>();
                        if (lugaresArr != null) {
                            for (int j = 0; j < lugaresArr.length(); j++) {
                                JSONObject l = lugaresArr.getJSONObject(j);
                                String nombre = l.optString("nombre", "");
                                String descripcion = l.optString("descripcion", "");
                                String ubicacion = l.optString("ubicacion", "");
                                String imagen = l.optString("imagen", "");
                                lugares.add(new Place(0, 0, 0, nombre, descripcion, ubicacion, true, imagen, true));
                            }
                        }
                        categoriaList.add(new CategoriaConLugares(tipo, lugares));
                        tipos.add(tipo);
                    } catch (JSONException e) { e.printStackTrace(); }
                }
                // Actualizar adapter de categorías usando el método público
                List<Category> categorias = new ArrayList<>();
                for (String t : tipos) categorias.add(new Category(0, t, "", "", true));
                android.util.Log.d("HomeFragment", "CATEGORIAS CARGADAS: " + categorias.size());
                if (categorias.size() > 0) {
                    categoryAdapter.setCategories(categorias);
                    // Mostrar lugares de la primera categoría
                    selectedCategoryIndex = 0;
                    updatePlacesForSelectedCategory();
                } else {
                    android.util.Log.d("HomeFragment", "Respuesta vacía, se mantienen categorías locales.");
                    // No actualices el adaptador, así se mantienen las locales
                }
            },
            error -> {
                android.util.Log.e("HomeFragment", "Error cargando categorías: " + error.toString());
                android.widget.Toast.makeText(getContext(), "Error de conexión al cargar categorías", android.widget.Toast.LENGTH_LONG).show();
                // Si falla la API, fuerza categorías locales
                categoryAdapter.setCategories(null);
            }
        );
        queue.add(request);
    }

    private void updatePlacesForSelectedCategory() {
        placeList.clear();
        if (!categoriaList.isEmpty() && selectedCategoryIndex < categoriaList.size()) {
            placeList.addAll(categoriaList.get(selectedCategoryIndex).lugares);
        }
        placeAdapter.notifyDataSetChanged();
        // El mapa solo se actualiza cuando se abre el diálogo
    }

    private int findCategoryIndexByTipo(String tipo) {
        for (int i = 0; i < categoriaList.size(); i++) {
            if (categoriaList.get(i).tipo.equals(tipo)) return i;
        }
        return -1;
    }
}
