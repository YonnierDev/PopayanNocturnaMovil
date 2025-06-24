package com.example.popayan_noc;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class WriteCommentDialogFragment extends DialogFragment {
    public interface OnCommentSendListener {
        void onCommentSend(String comment);
    }

    private OnCommentSendListener listener;

    public void setOnCommentSendListener(OnCommentSendListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comment, null);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnSend = dialogView.findViewById(R.id.btnSend);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.setOnShowListener(d -> {
            btnSend.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentSend(etComment.getText().toString());
                }
                dismiss();
            });
            btnCancel.setOnClickListener(v -> dismiss());
        });

        // Fondo opaco
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
        return dialog;
    }
}
