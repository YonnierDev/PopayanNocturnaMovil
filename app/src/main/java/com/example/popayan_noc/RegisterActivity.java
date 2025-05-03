package com.example.popayan_noc;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etLastName, etBirthDate, etEmail, etPassword;
    private Spinner spinnerGender;
    private Button btnRegister;
    private TextView tvGoLogin;
    private ProgressDialog progressDialog;
    private static final String REGISTER_URL = "https://popnocturna.vercel.app/api/registrar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etLastName = findViewById(R.id.etLastName);
        etBirthDate = findViewById(R.id.etBirthDate);
        spinnerGender = findViewById(R.id.spinnerGender);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando usuario...");

        // Configurar Spinner de género
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // DatePicker para fecha de nacimiento
        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnRegister.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.93f).scaleY(0.93f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });

        btnRegister.setOnClickListener(v -> attemptRegister());

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem() != null ? spinnerGender.getSelectedItem().toString() : "";
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean valid = true;
        if (name.isEmpty()) {
            etName.setError("Requerido");
            valid = false;
        } else {
            etName.setError(null);
        }
        if (lastName.isEmpty()) {
            etLastName.setError("Requerido");
            valid = false;
        } else {
            etLastName.setError(null);
        }
        if (birthDate.isEmpty()) {
            etBirthDate.setError("Requerido");
            valid = false;
        } else {
            etBirthDate.setError(null);
        }
        if (gender.isEmpty()) {
            Snackbar.make(btnRegister, "Selecciona un género", Snackbar.LENGTH_SHORT).show();
            valid = false;
        }
        if (email.isEmpty()) {
            etEmail.setError("Requerido");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo no válido");
            valid = false;
        } else {
            etEmail.setError(null);
        }
        if (password.isEmpty()) {
            etPassword.setError("Requerido");
            valid = false;
        } else if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$")) {
            etPassword.setError("8-20 caracteres, 1 mayúscula, 1 número y 1 símbolo");
            valid = false;
        } else {
            etPassword.setError(null);
        }
        if (!valid) return;

        progressDialog.show();
        JSONObject body = new JSONObject();
        try {
            body.put("nombre", name);
            body.put("apellido", lastName);
            body.put("fecha_nacimiento", birthDate);
            body.put("genero", gender);
            body.put("correo", email);
            body.put("contrasena", password);
        } catch (JSONException e) {
            progressDialog.dismiss();
            Snackbar.make(btnRegister, "Error interno", Snackbar.LENGTH_LONG).show();
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, REGISTER_URL, body,
                response -> {
                    progressDialog.dismiss();
                    Snackbar.make(btnRegister, "Registro exitoso. Revisa tu correo para validar tu cuenta.", Snackbar.LENGTH_LONG).show();
                    btnRegister.postDelayed(() -> {
                        Intent intent = new Intent(this, ValidateAccountActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }, 1200);
                },
                error -> {
                    progressDialog.dismiss();
                    String msg = "Error de conexión";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorMsg = new String(error.networkResponse.data);
                            JSONObject errorObj = new JSONObject(errorMsg);
                            msg = errorObj.optString("mensaje", msg);
                        } catch (Exception ignored) {}
                    }
                    Snackbar.make(btnRegister, msg, Snackbar.LENGTH_LONG).show();
                });
        queue.add(request);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etBirthDate.setText(formatted);
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
