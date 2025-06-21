package com.example.popayan_noc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnFavorites = view.findViewById(R.id.btnFavorites);
        Button btnComments = view.findViewById(R.id.btnComments);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Mostrar datos reales del usuario con animación fade-in
        org.json.JSONObject usuario = AuthUtils.getUser(requireContext());
        String nombre = null;
        String correo = null;
        if (usuario != null) {
            // Intenta primero en el nivel raíz
            nombre = usuario.optString("nombre", null);
            correo = usuario.optString("correo", null);
            // Si no están, busca dentro de un objeto 'usuario'
            if ((nombre == null || correo == null) && usuario.has("usuario")) {
                try {
                    org.json.JSONObject userObj = usuario.getJSONObject("usuario");
                    if (nombre == null) nombre = userObj.optString("nombre", userObj.optString("name", null));
                    if (correo == null) correo = userObj.optString("correo", userObj.optString("email", null));
                } catch (org.json.JSONException e) {
                    // No pasa nada, seguimos
                }
            }
            // Si sigue sin encontrarse, intenta con otras claves en el raíz
            if (nombre == null) nombre = usuario.optString("name", null);
            if (correo == null) correo = usuario.optString("email", null);

            // Si aún así no hay datos, mostrar el JSON completo
            if (nombre == null || correo == null) {
                String jsonStr = usuario.toString();
                android.util.Log.d("ProfileFragment", "Usuario JSON: " + jsonStr);
                android.widget.Toast.makeText(requireContext(), "Usuario JSON: " + jsonStr, android.widget.Toast.LENGTH_LONG).show();
            }
            tvProfileName.setAlpha(0f);
            tvProfileEmail.setAlpha(0f);
            tvProfileName.setText(nombre != null ? nombre : "-");
            tvProfileEmail.setText(correo != null ? correo : "-");
            tvProfileName.animate().alpha(1f).setDuration(600).start();
            tvProfileEmail.animate().alpha(1f).setDuration(600).start();
        } else {
            android.util.Log.d("ProfileFragment", "Usuario es null");
            android.widget.Toast.makeText(requireContext(), "No se encontró usuario logueado", android.widget.Toast.LENGTH_LONG).show();
            tvProfileName.setText("No disponible");
            tvProfileEmail.setText("");
        }

        // Transición suave para la foto de perfil
        ivProfilePhoto.setScaleX(0.8f);
        ivProfilePhoto.setScaleY(0.8f);
        ivProfilePhoto.animate().scaleX(1f).scaleY(1f).setDuration(500).setStartDelay(150).start();

        // Listener para cerrar sesión
        btnLogout.setOnClickListener(v -> {
            AuthUtils.clearSession(requireContext());
            android.content.Intent intent = new android.content.Intent(requireContext(), LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.main, new EditProfileFragment())
                .addToBackStack(null)
                .commit();
        });
        btnFavorites.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main, new FavoritesFragment())
                .addToBackStack(null)
                .commit();
        });
        btnComments.setOnClickListener(v -> {
            // Si tienes un fragmento de comentarios del usuario, reemplaza aquí:
            // requireActivity().getSupportFragmentManager()
            //     .beginTransaction()
            //     .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            //     .replace(R.id.main, new MisComentariosFragment())
            //     .addToBackStack(null)
            //     .commit();
            // Si no existe aún, muestra un Toast:
            android.widget.Toast.makeText(requireContext(), "Próximamente: Mis comentarios", android.widget.Toast.LENGTH_SHORT).show();
        });
        return view;
    }
}
