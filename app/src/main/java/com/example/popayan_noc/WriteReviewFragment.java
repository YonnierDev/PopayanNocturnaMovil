package com.example.popayan_noc;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.TextView;

public class WriteReviewFragment extends Fragment {
    private EditText etReviewText;
    private Button btnSendReview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_review, container, false);
        etReviewText = view.findViewById(R.id.etReviewText);
        btnSendReview = view.findViewById(R.id.btnSendReview);
        TextView tvWriteReviewTitle = view.findViewById(R.id.tvWriteReviewTitle);
        if (tvWriteReviewTitle != null) {
            tvWriteReviewTitle.setText("Escribe tu comentario");
        }
        int eventoid = getArguments() != null ? getArguments().getInt("eventoid", -1) : -1;

        btnSendReview.setOnClickListener(v -> {
            String comment = etReviewText.getText().toString();
            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(getContext(), "Por favor escribe tu comentario", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventoid == -1) {
                Toast.makeText(getContext(), "Error: no se encontró el evento.", Toast.LENGTH_SHORT).show();
                return;
            }
            String token = AuthUtils.getToken(requireContext());
            btnSendReview.setEnabled(false);
            // Mostrar animación de carga
            View loadingView = LayoutInflater.from(getContext()).inflate(R.layout.view_reserva_loading, null);
            android.app.AlertDialog loadingDialog = new android.app.AlertDialog.Builder(getContext())
                    .setView(loadingView)
                    .setCancelable(false)
                    .create();
            loadingDialog.show();
            ReviewApi.postComentario(requireContext(), token, eventoid, comment,
                response -> {
                    Toast.makeText(getContext(), "¡Gracias por tu comentario!", Toast.LENGTH_SHORT).show();
                    btnSendReview.setEnabled(true);
                    loadingDialog.dismiss();
                    requireActivity().getSupportFragmentManager().popBackStack();
                },
                error -> {
                    String msg = "Error al enviar comentario";
                    if (error != null && error.getMessage() != null) {
                        msg = error.getMessage();
                    }
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    btnSendReview.setEnabled(true);
                    loadingDialog.dismiss();
                }
            );
        });
        return view;
    }
}
