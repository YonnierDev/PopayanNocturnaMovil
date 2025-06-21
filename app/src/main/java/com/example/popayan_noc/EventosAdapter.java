package com.example.popayan_noc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.popayan_noc.model.Evento;
import com.example.popayan_noc.model.Lugar;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.List;

public class EventosAdapter extends RecyclerView.Adapter<EventosAdapter.EventoViewHolder> {
    private Context context;
    private List<Evento> eventos;
    private HashMap<Integer, JSONArray> commentsCache = new HashMap<>();

    public EventosAdapter(Context context, List<Evento> eventos) {
        this.context = context;
        this.eventos = eventos;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_evento_detalle, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        // Animación de entrada: slide-in + fade-in
        if (holder.itemView.getAnimation() == null) {
            android.view.animation.Animation anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
            holder.itemView.startAnimation(anim);
        }
        Evento evento = eventos.get(position);
        
        // Cargar imagen de la portada
        if (!evento.getPortada().isEmpty()) {
            String urlImagen = evento.getPortada().get(0);
            Glide.with(context)
                .load(urlImagen)
                .placeholder(R.drawable.placeholder_img)
                .into(holder.ivPortada);
        }

        // Mostrar datos del evento
        holder.tvNombre.setText(evento.getNombre());
        holder.tvDescripcion.setText(evento.getDescripcion());
        holder.tvFechaHora.setText(evento.getFechaHora());
        holder.tvCapacidad.setText("Capacidad: " + evento.getCapacidad());
        holder.tvPrecio.setText("Precio: " + evento.getPrecio());

        // Mostrar datos del lugar
        Lugar lugar = evento.getLugar();
        if (lugar != null) {
            holder.tvLugarNombre.setText(lugar.getNombre());
            holder.tvLugarUbicacion.setText(lugar.getUbicacion());
        } else {
            holder.tvLugarNombre.setText("Lugar no disponible");
            holder.tvLugarUbicacion.setText("Ubicación no disponible");
        }

        // --- Cargar comentarios existentes ---
        int eventId = evento.getId();
        JSONArray cached = commentsCache.get(eventId);
        if (cached != null) {
            holder.rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
            holder.rvComments.setAdapter(new CommentAdapter(context, cached));
            holder.rvComments.setVisibility(View.VISIBLE);
        } else {
            new Thread(() -> {
                try {
                    URL url = new URL("https://popnocturna.vercel.app/api/comentario?eventoid=" + eventId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    InputStream is = conn.getInputStream();
                    Scanner s = new Scanner(is).useDelimiter("\\A");
                    String response = s.hasNext() ? s.next() : "";
                    is.close();
                    conn.disconnect();
                    JSONArray comentarios = new JSONArray(response);
                    commentsCache.put(eventId, comentarios);
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> {
                        holder.rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
                        holder.rvComments.setAdapter(new CommentAdapter(context, comentarios));
                        holder.rvComments.setVisibility(View.VISIBLE);
                    });
                } catch (Exception e) {
                    // No mostrar nada si falla
                }
            }).start();
        }

        // --- Botón Comentar ---
        holder.btnComentar.setOnClickListener(v -> {
    if (context instanceof com.example.popayan_noc.EventosProximosActivity) {
        ((com.example.popayan_noc.EventosProximosActivity) context).mostrarComentarFragment(eventId);
    } else if (context instanceof androidx.fragment.app.FragmentActivity) {
        // Fallback por si se usa en otro flujo
        androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) context;
        androidx.fragment.app.FragmentManager fm = activity.getSupportFragmentManager();
        androidx.fragment.app.Fragment fragment = new com.example.popayan_noc.WriteReviewFragment();
        android.os.Bundle args = new android.os.Bundle();
        args.putInt("eventoid", eventId);
        fragment.setArguments(args);
        fm.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    } else {
        android.widget.Toast.makeText(context, "No se pudo abrir el formulario de comentario", android.widget.Toast.LENGTH_SHORT).show();
    }
});

        // --- Botón Reservar ---
        holder.btnReservar.setOnClickListener(v -> {
            // Diálogo personalizado de reserva
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reserva, null);
            android.widget.NumberPicker npCantidad = dialogView.findViewById(R.id.npCantidadEntradas);
            TextView tvReservaTitulo = dialogView.findViewById(R.id.tvReservaTitulo);
            TextView tvReservaFecha = dialogView.findViewById(R.id.tvReservaFecha);
            TextView tvReservaCapacidad = dialogView.findViewById(R.id.tvReservaCapacidad);
            Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarReserva);
            Button btnCancelar = dialogView.findViewById(R.id.btnCancelarReserva);

            int capacidad = evento.getCapacidad();
            String fechaHora = evento.getFechaHora();
            tvReservaFecha.setText("Fecha y hora del evento: " + fechaHora);
            tvReservaCapacidad.setText("Capacidad disponible: " + capacidad);
            npCantidad.setMinValue(1);
            npCantidad.setMaxValue(Math.max(1, capacidad));
            npCantidad.setValue(1);

            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            btnCancelar.setOnClickListener(bv -> dialog.dismiss());
            btnConfirmar.setOnClickListener(bv -> {
                int cantidad = npCantidad.getValue();
                String token = com.example.popayan_noc.AuthUtils.getToken(context);
                btnConfirmar.setEnabled(false);
                // Mostrar animación de carga
                View loadingView = LayoutInflater.from(context).inflate(R.layout.view_reserva_loading, null);
                android.app.AlertDialog loadingDialog = new android.app.AlertDialog.Builder(context)
                        .setView(loadingView)
                        .setCancelable(false)
                        .create();
                loadingDialog.show();
                // Construir JSON
                org.json.JSONObject body = new org.json.JSONObject();
                try {
                    body.put("eventoid", eventId);
                    body.put("fecha_hora", fechaHora);
                    body.put("cantidad_entradas", cantidad);
                } catch (Exception e) {
                    android.widget.Toast.makeText(context, "Error al preparar datos", android.widget.Toast.LENGTH_SHORT).show();
                    btnConfirmar.setEnabled(true);
                    loadingDialog.dismiss();
                    return;
                }
                new Thread(() -> {
                    try {
                        java.net.URL url = new java.net.URL("https://popnocturna.vercel.app/api/reserva");
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        conn.setDoOutput(true);
                        java.io.OutputStream os = conn.getOutputStream();
                        os.write(body.toString().getBytes("UTF-8"));
                        os.close();
                        int responseCode = conn.getResponseCode();
                        java.io.InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                        String response = s.hasNext() ? s.next() : "";
                        is.close();
                        conn.disconnect();
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> {
                            btnConfirmar.setEnabled(true);
                            loadingDialog.dismiss();
                            if (responseCode == 201) {
                                dialog.dismiss();
                                android.widget.Toast.makeText(context, "¡Reserva creada exitosamente!", android.widget.Toast.LENGTH_LONG).show();
                            } else if (response.contains("Ya tienes una reserva")) {
                                android.widget.Toast.makeText(context, "Ya tienes una reserva activa para este evento.", android.widget.Toast.LENGTH_LONG).show();
                            } else if (response.contains("No hay suficiente capacidad")) {
                                android.widget.Toast.makeText(context, "No hay suficiente capacidad disponible.", android.widget.Toast.LENGTH_LONG).show();
                            } else {
                                android.widget.Toast.makeText(context, "Error: " + response, android.widget.Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> android.widget.Toast.makeText(context, "Error al crear reserva", android.widget.Toast.LENGTH_LONG).show());
                    }
                }).start();
            });
            dialog.show();
        });

        // --- Botón Calificar ---
        holder.btnCalificar.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null);
            RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
            dialogView.findViewById(R.id.btnCancelRating).setOnClickListener(bv -> dialog.dismiss());
            dialogView.findViewById(R.id.btnSendRating).setOnClickListener(bv -> {
                int puntuacion = (int) ratingBar.getRating();
                if (puntuacion < 1 || puntuacion > 5) {
                    android.widget.Toast.makeText(context, "Selecciona una puntuación de 1 a 5", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                new Thread(() -> {
                    try {
                        URL url = new URL("https://popnocturna.vercel.app/api/calificacion");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        String token = com.example.popayan_noc.AuthUtils.getToken(context);
                        if (token == null || token.isEmpty()) {
                            android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                            mainHandler.post(() -> android.widget.Toast.makeText(context, "No hay sesión activa. Inicia sesión para calificar.", android.widget.Toast.LENGTH_LONG).show());
                            return;
                        }
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        conn.setDoOutput(true);
                        JSONObject body = new JSONObject();
                        body.put("eventoid", eventId);
                        body.put("puntuacion", puntuacion);
                        OutputStream os = conn.getOutputStream();
                        os.write(body.toString().getBytes("UTF-8"));
                        os.close();
                        int responseCode = conn.getResponseCode();
                        InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                        Scanner s = new Scanner(is).useDelimiter("\\A");
                        String response = s.hasNext() ? s.next() : "";
                        is.close();
                        conn.disconnect();
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> {
                            if (responseCode == 201) {
                                android.widget.Toast.makeText(context, "¡Calificación enviada!", android.widget.Toast.LENGTH_SHORT).show();
                                holder.tvRating.setVisibility(View.VISIBLE);
                                holder.tvRating.setText("Calificación: " + puntuacion + " estrellas");
                            } else if (response.contains("Ya has calificado")) {
                                android.widget.Toast.makeText(context, "Ya has calificado este evento", android.widget.Toast.LENGTH_LONG).show();
                            } else if (response.contains("propietario")) {
                                android.widget.Toast.makeText(context, "Los propietarios no pueden calificar eventos", android.widget.Toast.LENGTH_LONG).show();
                            } else {
                                android.widget.Toast.makeText(context, "Error: " + response, android.widget.Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> android.widget.Toast.makeText(context, "Error al enviar calificación", android.widget.Toast.LENGTH_LONG).show());
                    }
                }).start();
            });
            dialog.show();
        });
    }
    
    @Override
    public int getItemCount() {
        return eventos == null ? 0 : eventos.size();
    }

    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivPortada;
        public TextView tvNombre;
        public TextView tvDescripcion;
        public TextView tvFechaHora;
        public TextView tvCapacidad;
        public TextView tvPrecio;
        public TextView tvLugarNombre;
        public TextView tvLugarUbicacion;
        public TextView tvRating;
        public Button btnCalificar;
        public Button btnComentar;
        public Button btnReservar;
        public RecyclerView rvComments;
        public TextView tvReservaCapacidad;
        public TextView tvReservaFecha;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.ivPortada);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvCapacidad = itemView.findViewById(R.id.tvCapacidad);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvLugarNombre = itemView.findViewById(R.id.tvLugarNombre);
            tvLugarUbicacion = itemView.findViewById(R.id.tvLugarUbicacion);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnCalificar = itemView.findViewById(R.id.btnCalificar);
            btnComentar = itemView.findViewById(R.id.btnComentar);
            btnReservar = itemView.findViewById(R.id.btnReservar);
            rvComments = itemView.findViewById(R.id.rvComments);
        }
    }
}
