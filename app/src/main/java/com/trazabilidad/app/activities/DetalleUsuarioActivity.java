package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.chip.Chip;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.UsuarioController;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.SessionManager;

public class DetalleUsuarioActivity extends AppCompatActivity {

    private TextView tvNombre, tvEmail, tvTelefono, tvRol, tvEstado;
    private Chip chipEstado;
    private Button btnEditar, btnDesactivar;
    private CardView cardInfo;
    private View divider;

    private UsuarioController usuarioController;
    private SessionManager sessionManager;
    private int usuarioId;
    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_usuario);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Detalle de Usuario");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializar controladores
        usuarioController = new UsuarioController(this);
        sessionManager = new SessionManager(this);

        // Obtener ID del usuario
        if (getIntent().hasExtra("USUARIO_ID")) {
            usuarioId = getIntent().getIntExtra("USUARIO_ID", -1);
        } else {
            Toast.makeText(this, "Error: No se especificó un usuario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar componentes
        inicializarComponentes();

        // Cargar información del usuario
        cargarDatosUsuario(usuarioId);

        // Configurar listeners
        configurarListeners();
    }

    private void inicializarComponentes() {
        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvRol = findViewById(R.id.tvRol);
        tvEstado = findViewById(R.id.tvEstado);
        chipEstado = findViewById(R.id.chipEstado);
        btnEditar = findViewById(R.id.btnEditar);
        btnDesactivar = findViewById(R.id.btnDesactivar);
        cardInfo = findViewById(R.id.cardInfo);
        divider = findViewById(R.id.divider);

        // Verificar si el usuario actual es administrador para mostrar opciones de edición
        if (!sessionManager.isUserAdmin()) {
            btnEditar.setVisibility(View.GONE);
            btnDesactivar.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
    }

    private void cargarDatosUsuario(int id) {
        usuarioController.obtenerUsuarioPorId(id, new UsuarioController.UsuarioCallback() {
            @Override
            public void onSuccess(Usuario usuario) {
                usuarioActual = usuario;
                mostrarDatosUsuario(usuario);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(DetalleUsuarioActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void mostrarDatosUsuario(Usuario usuario) {
        tvNombre.setText(usuario.getNombre());
        tvEmail.setText(usuario.getEmail());
        tvTelefono.setText(usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()
                ? usuario.getTelefono() : "No especificado");
        tvRol.setText(usuario.getRol());

        // Configurar estado
        if (usuario.isActivo()) {
            chipEstado.setText("Activo");
            chipEstado.setChipBackgroundColorResource(R.color.colorActive);
            btnDesactivar.setText("Desactivar Usuario");
        } else {
            chipEstado.setText("Inactivo");
            chipEstado.setChipBackgroundColorResource(R.color.colorInactive);
            btnDesactivar.setText("Activar Usuario");
        }
    }

    private void configurarListeners() {
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearEditarUsuarioActivity.class);
            intent.putExtra("USUARIO_ID", usuarioId);
            startActivity(intent);
        });

        btnDesactivar.setOnClickListener(v -> {
            if (usuarioActual.isActivo()) {
                mostrarDialogoConfirmacionDesactivar();
            } else {
                activarUsuario();
            }
        });
    }

    private void mostrarDialogoConfirmacionDesactivar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Desactivar usuario");
        builder.setMessage("¿Estás seguro de que deseas desactivar este usuario?");
        builder.setPositiveButton("Desactivar", (dialog, which) -> {
            desactivarUsuario();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void desactivarUsuario() {
        usuarioController.eliminarUsuario(usuarioId, new UsuarioController.OperacionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(DetalleUsuarioActivity.this, "Usuario desactivado correctamente", Toast.LENGTH_SHORT).show();
                cargarDatosUsuario(usuarioId);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(DetalleUsuarioActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void activarUsuario() {
        // Crear un usuario con los datos actuales pero con estado activo
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(usuarioActual.getId());
        usuarioActualizado.setNombre(usuarioActual.getNombre());
        usuarioActualizado.setEmail(usuarioActual.getEmail());
        usuarioActualizado.setPassword(usuarioActual.getPassword());
        usuarioActualizado.setRol(usuarioActual.getRol());
        usuarioActualizado.setTelefono(usuarioActual.getTelefono());
        usuarioActualizado.setActivo(true);

        usuarioController.actualizarUsuario(usuarioActualizado, new UsuarioController.OperacionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(DetalleUsuarioActivity.this, "Usuario activado correctamente", Toast.LENGTH_SHORT).show();
                cargarDatosUsuario(usuarioId);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(DetalleUsuarioActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuario(usuarioId); // Recargar datos por si hubo cambios
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}