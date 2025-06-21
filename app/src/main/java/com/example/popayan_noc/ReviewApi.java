package com.example.popayan_noc;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ReviewApi {
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";

    public static void getReviewsByEvento(Context context, String token, int eventoId, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/comentarios/evento/" + eventoId;
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }

    /**
     * Envía un comentario para un evento usando el endpoint actualizado.
     * @param context Contexto de la app
     * @param token Token JWT del usuario
     * @param eventoid ID del evento
     * @param contenido Contenido del comentario (máx 500 chars)
     * @param listener Listener para respuesta exitosa
     * @param errorListener Listener para errores
     */
    public static void postComentario(Context context, String token, int eventoid, String contenido, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        // Validación local de longitud
        if (contenido == null || contenido.trim().isEmpty()) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new VolleyError("El contenido del comentario es requerido"));
            }
            return;
        }
        if (contenido.length() > 500) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new VolleyError("El comentario no puede superar los 500 caracteres"));
            }
            return;
        }
        String url = BASE_URL + "/comentario";
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject data = new JSONObject();
        try {
            data.put("eventoid", eventoid);
            data.put("contenido", contenido);
        } catch (Exception e) {
            if (errorListener != null) errorListener.onErrorResponse(new VolleyError("Error al preparar datos: " + e.getMessage()));
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(request);
    }
}
