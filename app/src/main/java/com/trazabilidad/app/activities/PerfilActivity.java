package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.AuthController;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.SessionManager;
import com.trazabilidad.app.utils.ValidationUtils;

import java.util.Objects;

public class PerfilActivity extends AppCompatActivity {

    private TextView tvNombre, tvEmail, tvRol;
    private EditText etPasswordActual, etPasswordNuevo, etPasswordConfirmar;
    private Button btnCambiarPassword, btnCerrarSesion;
    private ProgressBar progressBar;

    private AuthController authController;
    private SessionManager sessionManager;
    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Mi Perfil");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }

        inicializarUI();
        inicializarControladores();

        usuarioActual = sessionManager.getUsuarioDetails();
        mostrarDatosUsuario();

        btnCambiarPassword.setOnClickListener(v -> cambiarPassword());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void inicializarUI() {
        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvRol = findViewById(R.id.tvRol);
        etPasswordActual = findViewById(R.id.etPasswordActual);
        etPasswordNuevo = findViewById(R.id.etPasswordNuevo);
        etPasswordConfirmar = findViewById(R.id.etPasswordConfirmar);
        btnCambiarPassword = findViewById(R.id.btnCambiarPassword);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        progressBar = findViewById(R.id.progressBar);
    }

    private void inicializarControladores() {
        authController = new AuthController(this);
        sessionManager = new SessionManager(this);
    }

    private void mostrarDatosUsuario() {
        tvNombre.setText(usuarioActual.getNombre());
        tvEmail.setText(usuarioActual.getEmail());
        tvRol.setText(usuarioActual.getRol());
    }

    private void cambiarPassword() {
        String actual = etPasswordActual.getText().toString().trim();
        String nueva = etPasswordNuevo.getText().toString().trim();
        String confirmar = etPasswordConfirmar.getText().toString().trim();

        if (!ValidationUtils.isValidInput(actual) ||
                !ValidationUtils.isValidInput(nueva) ||
                !ValidationUtils.isValidInput(confirmar)) {
            mostrarToast("Por favor complete todos los campos");
            return;
        }

        if (!nueva.equals(confirmar)) {
            mostrarToast("Las contraseñas nuevas no coinciden");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCambiarPassword.setEnabled(false);

        authController.cambiarPassword(usuarioActual.getId(), actual, nueva, new AuthController.OperacionCallback() {
            @Override
            public void onSuccess() {
                mostrarToast("Contraseña actualizada correctamente");
                limpiarCamposPassword();
                finalizarProceso();
            }

            @Override
            public void onError(String message) {
                mostrarToast(message);
                finalizarProceso();
            }
        });
    }

    private void cerrarSesion() {
        sessionManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void limpiarCamposPassword() {
        etPasswordActual.setText("");
        etPasswordNuevo.setText("");
        etPasswordConfirmar.setText("");
    }

    private void finalizarProceso() {
        progressBar.setVisibility(View.GONE);
        btnCambiarPassword.setEnabled(true);
    }

    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
