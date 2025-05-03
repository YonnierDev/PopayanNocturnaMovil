package com.example.popayan_noc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.animation.ValueAnimator;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import com.facebook.shimmer.ShimmerFrameLayout; // Shimmer loader

import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExploreFragment extends Fragment {
    private LinearLayout searchBarContainer;
    private EditText etSearch;
    private CardView cardSearchBar;
    private boolean isSearchExpanded = false;
    private RecyclerView rvSuggestions;
    private RecyclerView rvCategories;
    private SuggestionAdapter suggestionAdapter;
    private CategoryAdapter categoryAdapter;
    private RecyclerView rvPlaces;
    private PlaceAdapter placeAdapter;
    private List<CategoriaConLugares> categoriaList = new ArrayList<>();
    private List<Place> allPlaces = new ArrayList<>();
    private List<Place> filteredPlaces = new ArrayList<>();
    private int selectedCategoryIndex = 0;
    private com.android.volley.RequestQueue queue;
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";
    private List<String> allSuggestions = Arrays.asList(
            "Restaurantes", "Naturaleza", "Eventos hoy", "Cultura", "Bares", "Museos", "Miradores", "Cafés", "Fiesta", "Parques"
    );

    private ShimmerFrameLayout shimmerFrameLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        searchBarContainer = view.findViewById(R.id.search_bar_container);
        etSearch = view.findViewById(R.id.etSearch);
        cardSearchBar = view.findViewById(R.id.card_search_bar);
        rvSuggestions = view.findViewById(R.id.rvSuggestions);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvPlaces = view.findViewById(R.id.rvPlaces);
        shimmerFrameLayout = view.findViewById(R.id.shimmerLayout);

        // Setup suggestions list
        suggestionAdapter = new SuggestionAdapter(new ArrayList<>(allSuggestions), suggestion -> {
            etSearch.setText(suggestion);
            etSearch.setSelection(suggestion.length());
            hideSuggestions();
            hideKeyboard();
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(suggestionAdapter);

        // Setup horizontal categories carousel
        categoryAdapter = new CategoryAdapter(getContext(), new ArrayList<>(), category -> {
            animateCategoryClick(rvCategories.findViewHolderForAdapterPosition(findCategoryIndexByTipo(category.tipo)));
            android.widget.Toast.makeText(getContext(), "Categoría: " + category.tipo, android.widget.Toast.LENGTH_SHORT).show();
            filterPlacesByCategory(category.tipo);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
        // Siempre carga categorías locales si la lista está vacía
        categoryAdapter.setCategories(null); // fuerza categorías locales por defecto

        // Inicializa Volley
        queue = com.android.volley.toolbox.Volley.newRequestQueue(requireContext());
        // Adapter de lugares
        placeAdapter = new PlaceAdapter(getContext(), filteredPlaces);
        rvPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlaces.setAdapter(placeAdapter);
        // Carga categorías y lugares desde el backend
        showShimmer(true);
        cargarCategoriasYlLugares();
        

        // Touch on search bar container shows search
        searchBarContainer.setOnClickListener(v -> {
            showSearch();
            animateSearchBarExpand(true);
        });
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showSuggestions();
                animateSearchBarExpand(true);
            } else {
                hideSuggestions();
                animateSearchBarExpand(false);
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSuggestions(s.toString());
                if (s.length() == 0) hideSuggestions();
                else showSuggestions();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Hide search on outside touch
        view.setOnTouchListener((v, event) -> {
            if (etSearch.hasFocus()) {
                etSearch.clearFocus();
                hideKeyboard();
            }
            return false;
        });

        return view;
    }

    private void filterPlacesByCategory(String tipoCategoria) {
        filteredPlaces.clear();
        int idx = findCategoryIndexByTipo(tipoCategoria);
        if (idx >= 0 && idx < categoriaList.size()) {
            filteredPlaces.addAll(categoriaList.get(idx).lugares);
        }
        placeAdapter.notifyDataSetChanged();
        
    }



    private int findCategoryIndexByTipo(String tipo) {
        for (int i = 0; i < categoriaList.size(); i++) {
            if (categoriaList.get(i).tipo.equalsIgnoreCase(tipo)) return i;
        }
        return -1;
    }

    private void cargarCategoriasYlLugares() {
        String url = BASE_URL + "/usuarios/lista-categorias";
        com.android.volley.toolbox.JsonArrayRequest request = new com.android.volley.toolbox.JsonArrayRequest(
                com.android.volley.Request.Method.GET, url, null,
                response -> {
                    categoriaList.clear();
                    List<String> tipos = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            org.json.JSONObject obj = response.getJSONObject(i);
                            String tipo = obj.optString("tipo", "");
                            org.json.JSONArray lugaresArr = obj.optJSONArray("lugares");
                            List<Place> lugares = new ArrayList<>();
                            if (lugaresArr != null) {
                                for (int j = 0; j < lugaresArr.length(); j++) {
                                    org.json.JSONObject l = lugaresArr.getJSONObject(j);
                                    String nombre = l.optString("nombre", "");
                                    String descripcion = l.optString("descripcion", "");
                                    String ubicacion = l.optString("ubicacion", "");
                                    String imagen = l.optString("imagen", "");
                                    lugares.add(new Place(0, 0, 0, nombre, descripcion, ubicacion, true, imagen, true));
                                }
                            }
                            categoriaList.add(new CategoriaConLugares(tipo, lugares));
                            tipos.add(tipo);
                        } catch (org.json.JSONException e) { e.printStackTrace(); }
                    }
                    // Actualizar adapter de categorías usando el método público
                    List<Category> categorias = new ArrayList<>();
                    for (String t : tipos) categorias.add(new Category(0, t, "", "", true));
                    categoryAdapter.setCategories(categorias);
                    // Mostrar lugares de la primera categoría por defecto
                    if (!categoriaList.isEmpty()) {
                        filteredPlaces.clear();
                        filteredPlaces.addAll(categoriaList.get(0).lugares);
                        placeAdapter.notifyDataSetChanged();
                        animatePlacesFadeIn();
                    }
                    showShimmer(false);
                },
                error -> {
                    android.util.Log.e("ExploreFragment", "Error cargando categorías: " + error.toString());
                    android.widget.Toast.makeText(getContext(), "Error de conexión al cargar categorías", android.widget.Toast.LENGTH_LONG).show();
                    // Si falla la API, muestra empty state visual
                    categoriaList.clear();
                    filteredPlaces.clear();
                    placeAdapter.notifyDataSetChanged();
                    showShimmer(false);
                }
        );
        queue.add(request);
    }

    private void showShimmer(boolean show) {
        if (shimmerFrameLayout == null) return;
        if (show) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            rvPlaces.setVisibility(View.GONE);
        } else {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            rvPlaces.setVisibility(View.VISIBLE);
        }
    }

    private void animatePlacesFadeIn() {
        if (rvPlaces != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(400);
            rvPlaces.startAnimation(fadeIn);
        }
    }

    private void animateCategoryClick(RecyclerView.ViewHolder holder) {
        if (holder == null || holder.itemView == null) return;
        ScaleAnimation scale = new ScaleAnimation(
                1f, 1.08f, 1f, 1.08f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(120);
        scale.setRepeatCount(1);
        scale.setRepeatMode(ScaleAnimation.REVERSE);
        holder.itemView.startAnimation(scale);
    }

    // Lugares dummy para fallback visual
    private List<Place> getDummyPlaces() {
        List<Place> places = new ArrayList<>();
        places.add(new Place(1, 1, 1, "Restaurante El Sabor", "Comida típica de Popayán", "Centro", true, "", true));
        places.add(new Place(2, 2, 1, "Bar La Noche", "Tragos y música en vivo", "Zona Rosa", true, "", true));
        places.add(new Place(3, 3, 1, "Cine Popayán", "Estrenos y clásicos", "Centro", true, "", true));
        places.add(new Place(4, 4, 1, "Museo de Historia", "Cultura y arte local", "Centro", true, "", true));
        places.add(new Place(5, 6, 1, "Parque Natural", "Naturaleza y senderismo", "Sur", true, "", true));
        return places;
    }

    private void showSearch() {
        etSearch.setVisibility(View.VISIBLE);
        etSearch.requestFocus();
        showKeyboard();
        animateSearchBarExpand(true);
    }

    private void animateSearchBarExpand(boolean expand) {
        if (cardSearchBar == null) return;
        float start = cardSearchBar.getCardElevation();
        float end = expand ? 20f : 8f;
        int startHeight = cardSearchBar.getLayoutParams().height;
        int endHeight = expand ? dpToPx(62) : dpToPx(50);
        ValueAnimator elevationAnim = ValueAnimator.ofFloat(start, end);
        elevationAnim.setDuration(250);
        elevationAnim.addUpdateListener(animation ->
                cardSearchBar.setCardElevation((float) animation.getAnimatedValue()));
        elevationAnim.start();
        ValueAnimator heightAnim = ValueAnimator.ofInt(startHeight, endHeight);
        heightAnim.setDuration(250);
        heightAnim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = cardSearchBar.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            cardSearchBar.setLayoutParams(params);
        });
        heightAnim.start();
        // Color highlight
        int colorFrom = cardSearchBar.getCardBackgroundColor().getDefaultColor();
        int colorTo = expand ? 0xFFFFFFFF : 0xFFF5F5F5;
        ValueAnimator colorAnim = ValueAnimator.ofArgb(colorFrom, colorTo);
        colorAnim.setDuration(250);
        colorAnim.addUpdateListener(anim -> cardSearchBar.setCardBackgroundColor((int) anim.getAnimatedValue()));
        colorAnim.start();
        isSearchExpanded = expand;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void showSuggestions() {
        rvSuggestions.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rvSuggestions.getLayoutParams();
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        rvSuggestions.setLayoutParams(params);
    }
    private void hideSuggestions() {
        rvSuggestions.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rvSuggestions.getLayoutParams();
        params.height = 0;
        rvSuggestions.setLayoutParams(params);
    }
    private void filterSuggestions(String query) {
        List<String> filtered = new ArrayList<>();
        for (String s : allSuggestions) if (s.toLowerCase().contains(query.toLowerCase())) filtered.add(s);
        suggestionAdapter.updateSuggestions(filtered);
    }
    private void showKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
    }
    private void hideKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }
}

