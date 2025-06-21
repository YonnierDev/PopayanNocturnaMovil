package com.example.popayan_noc;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EventosProximosActivity extends AppCompatActivity {
    private java.util.List<com.example.popayan_noc.model.Evento> eventosListOriginal = new java.util.ArrayList<>();
    private EventosAdapter eventosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos_proximos);

        RecyclerView rv = findViewById(R.id.rvEventosProximos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        // Animación de entrada (removida porque causaba crash)

        // Cargar eventos reales desde la API y mostrarlos con EventosAdapter
        com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(this);
        String BASE_URL = "https://popnocturna.vercel.app/api";
        String token = AuthUtils.getToken(this);
        if (token == null) {
            android.widget.Toast.makeText(this, "Token no disponible. No se pueden cargar eventos.", android.widget.Toast.LENGTH_LONG).show();
            rv.setVisibility(android.view.View.GONE);
            return;
        }
        String url = BASE_URL + "/eventos";
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null,
                response -> {
                    try {
                        org.json.JSONArray eventosArray = response.optJSONArray("datos");
                        if (eventosArray != null && eventosArray.length() > 0) {
                            eventosListOriginal.clear();
                            for (int i = 0; i < eventosArray.length(); i++) {
                                org.json.JSONObject obj = eventosArray.getJSONObject(i);
                                com.example.popayan_noc.model.Evento evento = parseEventoFromJson(obj);
                                eventosListOriginal.add(evento);
                            }
                            eventosAdapter = new EventosAdapter(this, new java.util.ArrayList<>(eventosListOriginal));
                            rv.setAdapter(eventosAdapter);
                            // Animación de entrada fade-in
                            rv.setAlpha(0f);
                            rv.animate().alpha(1f).setDuration(500).start();
                        } else {
                            android.widget.Toast.makeText(this, "No hay eventos próximos disponibles.", android.widget.Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        android.widget.Toast.makeText(this, "Error procesando eventos: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    android.widget.Toast.makeText(this, "Error al cargar eventos: " + error.toString(), android.widget.Toast.LENGTH_LONG).show();
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


        // Filtro de búsqueda
        android.widget.EditText etBuscar = findViewById(R.id.etBuscarEvento);
        etBuscar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarEventos(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Botón volver
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
    }

    private void filtrarEventos(String texto) {
        java.util.List<com.example.popayan_noc.model.Evento> filtrados = new java.util.ArrayList<>();
        for (com.example.popayan_noc.model.Evento evento : eventosListOriginal) {
            if (evento.getNombre().toLowerCase().contains(texto.toLowerCase()) ||
                (evento.getDescripcion() != null && evento.getDescripcion().toLowerCase().contains(texto.toLowerCase())) ||
                (evento.getLugar() != null && evento.getLugar().getNombre() != null && evento.getLugar().getNombre().toLowerCase().contains(texto.toLowerCase()))
            ) {
                filtrados.add(evento);
            }
        }
        eventosAdapter = new EventosAdapter(this, filtrados);
        RecyclerView rv = findViewById(R.id.rvEventosProximos);
        rv.setAdapter(eventosAdapter);
        // Animación fade-in al actualizar filtro
        rv.setAlpha(0f);
        rv.animate().alpha(1f).setDuration(300).start();
    }

    // Método para convertir JSONObject en Evento (modelo Java)
    private com.example.popayan_noc.model.Evento parseEventoFromJson(org.json.JSONObject obj) {
        com.example.popayan_noc.model.Evento evento = new com.example.popayan_noc.model.Evento();
        try {
            evento.setId(obj.optInt("id"));
            evento.setNombre(obj.optString("nombre"));
            evento.setDescripcion(obj.optString("descripcion"));
            evento.setFechaHora(obj.optString("fecha_hora"));
            evento.setCapacidad(obj.optInt("capacidad"));
            evento.setPrecio(obj.optString("precio"));
            // Portada
            java.util.List<String> portadas = new java.util.ArrayList<>();
            org.json.JSONArray portadaArr = obj.optJSONArray("portada");
            if (portadaArr != null) {
                for (int j = 0; j < portadaArr.length(); j++) {
                    portadas.add(portadaArr.optString(j));
                }
            }
            evento.setPortada(portadas);
            // Lugar (si está presente)
            org.json.JSONObject lugarObj = obj.optJSONObject("lugar");
            if (lugarObj != null) {
                com.example.popayan_noc.model.Lugar lugar = new com.example.popayan_noc.model.Lugar();
                lugar.setId(lugarObj.optInt("id"));
                lugar.setNombre(lugarObj.optString("nombre"));
                // Puedes mapear más campos de Lugar si es necesario
                evento.setLugar(lugar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evento;
    }

    // --- Métodos para el flujo de comentar ---
    public void mostrarComentarFragment(int eventId) {
        findViewById(R.id.rvEventosProximos).setVisibility(android.view.View.GONE);
        findViewById(R.id.fragment_container).setVisibility(android.view.View.VISIBLE);
        com.example.popayan_noc.WriteReviewFragment fragment = new com.example.popayan_noc.WriteReviewFragment();
        android.os.Bundle args = new android.os.Bundle();
        args.putInt("eventoid", eventId);
        fragment.setArguments(args);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    }

    @Override
    public void onBackPressed() {
        android.widget.FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer.getVisibility() == android.view.View.VISIBLE) {
            fragmentContainer.setVisibility(android.view.View.GONE);
            findViewById(R.id.rvEventosProximos).setVisibility(android.view.View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
