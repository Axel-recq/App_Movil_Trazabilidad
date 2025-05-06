package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.AuthController;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.SessionManager;
import com.trazabilidad.app.utils.ValidationUtils;


public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    private AuthController authController;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);

            // Inicializar vistas
            etUsername = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            progressBar = findViewById(R.id.progressBar);

            // Inicializar servicios
            authController = new AuthController(this);
            sessionManager = new SessionManager(this);

            // Si ya hay sesión activa, redirigir a MainActivity
            if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                return;
            }

            // Listener del botón de login
            btnLogin.setOnClickListener(v -> login());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validar entrada
        if (!ValidationUtils.isValidInput(username) || !ValidationUtils.isValidInput(password)) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar progreso
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        etUsername.setEnabled(false);
        etPassword.setEnabled(false);

        // Llamar al AuthController para autenticación local
        authController.login(username, password, new AuthController.AuthCallback() {
            @Override
            public void onSuccess(Usuario usuario) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                etUsername.setEnabled(true);
                etPassword.setEnabled(true);

                // Crear sesión de usuario
                sessionManager.createLoginSession(usuario);

                // Redirigir a MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {

                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                etUsername.setEnabled(true);
                etPassword.setEnabled(true);

                // Mostrar mensaje de error
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}