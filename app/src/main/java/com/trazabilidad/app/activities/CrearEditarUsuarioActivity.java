package com.trazabilidad.app.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.UsuarioController;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.SessionManager;

public class CrearEditarUsuarioActivity extends AppCompatActivity {
    private SwitchMaterial switchActivo;
    private TextInputLayout tilNombre, tilEmail, tilPassword, tilTelefono;
    private EditText etNombre, etEmail, etPassword, etTelefono;
    private Spinner spinnerRol;
    private Button btnGuardar;
    private ProgressBar progressBar;

    private UsuarioController usuarioController;
    private SessionManager sessionManager;
    private boolean modoEdicion = false;
    private int usuarioId = -1;
    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_editar_usuario);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializar controladores
        usuarioController = new UsuarioController(this);
        sessionManager = new SessionManager(this);

        // Verificar si el usuario actual es administrador
        if (!sessionManager.isUserAdmin()) {
            Toast.makeText(this, "No tienes permisos para esta acción", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar componentes
        inicializarComponentes();

        // Verificar si estamos en modo edición
        if (getIntent().hasExtra("USUARIO_ID")) {
            usuarioId = getIntent().getIntExtra("USUARIO_ID", -1);
            modoEdicion = true;
            cargarDatosUsuario(usuarioId);
            getSupportActionBar().setTitle("Editar Usuario");
        } else {
            getSupportActionBar().setTitle("Crear Usuario");
            switchActivo.setChecked(true); // Por defecto activo
            switchActivo.setVisibility(View.GONE); // Ocultar en modo creación
        }

        // Configurar listeners
        configurarListeners();
    }

    private void inicializarComponentes() {
        tilNombre = findViewById(R.id.tilNombre);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilTelefono = findViewById(R.id.tilTelefono);

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etTelefono = findViewById(R.id.etTelefono);

        spinnerRol = findViewById(R.id.spinnerRol);
        switchActivo = findViewById(R.id.switchActivo);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);

        // Configurar spinner de roles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);
    }

    private void cargarDatosUsuario(int id) {
        progressBar.setVisibility(View.VISIBLE);
        usuarioController.obtenerUsuarioPorId(id, new UsuarioController.UsuarioCallback() {
            @Override
            public void onSuccess(Usuario usuario) {
                usuarioActual = usuario;
                etNombre.setText(usuario.getNombre());
                etEmail.setText(usuario.getEmail());
                etPassword.setText(usuario.getPassword());
                etTelefono.setText(usuario.getTelefono());

                // Seleccionar el rol en el spinner
                int posicion = 0;
                String[] roles = getResources().getStringArray(R.array.roles_array);
                for (int i = 0; i < roles.length; i++) {
                    if (roles[i].equals(usuario.getRol())) {
                        posicion = i;
                        break;
                    }
                }
                spinnerRol.setSelection(posicion);

                switchActivo.setChecked(usuario.isActivo());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CrearEditarUsuarioActivity.this, message, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                finish();
            }
        });
    }

    private void configurarListeners() {
        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarUsuario();
            }
        });
    }

    private boolean validarCampos() {
        boolean esValido = true;

        if (etNombre.getText().toString().trim().isEmpty()) {
            tilNombre.setError("El nombre es requerido");
            esValido = false;
        } else {
            tilNombre.setError(null);
        }

        if (etEmail.getText().toString().trim().isEmpty()) {
            tilEmail.setError("El email es requerido");
            esValido = false;
        } else {
            tilEmail.setError(null);
        }

        if (!modoEdicion && etPassword.getText().toString().trim().isEmpty()) {
            tilPassword.setError("La contraseña es requerida");
            esValido = false;
        } else {
            tilPassword.setError(null);
        }

        return esValido;
    }

    private void guardarUsuario() {
        progressBar.setVisibility(View.VISIBLE);

        Usuario usuario = new Usuario();
        if (modoEdicion) {
            usuario.setId(usuarioId);
        }

        usuario.setNombre(etNombre.getText().toString().trim());
        usuario.setEmail(etEmail.getText().toString().trim());
        usuario.setPassword(etPassword.getText().toString().trim());
        usuario.setRol(spinnerRol.getSelectedItem().toString());
        usuario.setTelefono(etTelefono.getText().toString().trim());
        usuario.setActivo(switchActivo.isChecked());

        if (modoEdicion) {
            // Si el password está vacío y estamos en modo edición, conservar el anterior
            if (usuario.getPassword().isEmpty()) {
                usuario.setPassword(usuarioActual.getPassword());
            }

            actualizarUsuario(usuario);
        } else {
            crearUsuario(usuario);
        }
    }

    private void crearUsuario(Usuario usuario) {
        usuarioController.registrarUsuario(usuario, new UsuarioController.OperacionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CrearEditarUsuarioActivity.this, "Usuario creado correctamente", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CrearEditarUsuarioActivity.this, message, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void actualizarUsuario(Usuario usuario) {
        usuarioController.actualizarUsuario(usuario, new UsuarioController.OperacionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CrearEditarUsuarioActivity.this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CrearEditarUsuarioActivity.this, message, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
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