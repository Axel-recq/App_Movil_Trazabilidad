package com.trazabilidad.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.UsuarioAdapter;
import com.trazabilidad.app.controllers.UsuarioController;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ListaUsuariosActivity extends AppCompatActivity implements UsuarioAdapter.OnUsuarioClickListener {

    private RecyclerView recyclerView;
    private UsuarioAdapter adapter;
    private UsuarioController usuarioController;
    private List<Usuario> listaUsuariosCompleta;
    private List<Usuario> listaUsuariosFiltrada;
    private EditText etBuscar;
    private ChipGroup chipGroupFiltros;
    private Chip chipTodos, chipAdmins, chipRepartidores, chipSupervisores, chipDesactivados;
    private FloatingActionButton fabAgregarUsuario;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_usuarios);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Usuarios");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializar componentes
        inicializarComponentes();

        // Verificar si el usuario actual es administrador
        if (!sessionManager.isUserAdmin()) {
            fabAgregarUsuario.setVisibility(View.GONE); // Ocultar botón si no es admin
        }
        if (!sessionManager.isUserAdmin()) {
            Toast.makeText(this, "Acceso restringido a administradores", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Configurar el RecyclerView
        configurarRecyclerView();

        // Cargar datos
        cargarUsuarios();

        // Configurar listeners
        configurarListeners();
    }

    private void inicializarComponentes() {
        recyclerView = findViewById(R.id.recyclerViewUsuarios);
        etBuscar = findViewById(R.id.etBuscarUsuario);
        chipGroupFiltros = findViewById(R.id.chipGroupFiltros);
        chipTodos = findViewById(R.id.chipTodos);
        chipAdmins = findViewById(R.id.chipAdmins);
        chipRepartidores = findViewById(R.id.chipRepartidores);
        chipSupervisores = findViewById(R.id.chipSupervisores);
        chipDesactivados = findViewById(R.id.chipDesactivados);
        fabAgregarUsuario = findViewById(R.id.fabAgregarUsuario);

        usuarioController = new UsuarioController(this);
        sessionManager = new SessionManager(this);
        listaUsuariosCompleta = new ArrayList<>();
        listaUsuariosFiltrada = new ArrayList<>();
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsuarioAdapter(listaUsuariosFiltrada, this);
        recyclerView.setAdapter(adapter);
    }

    private void cargarUsuarios() {
        usuarioController.obtenerTodosLosUsuarios(new UsuarioController.UsuariosCallback() {
            @Override
            public void onSuccess(List<Usuario> usuarios) {
                listaUsuariosCompleta = usuarios;
                listaUsuariosFiltrada.clear();
                listaUsuariosFiltrada.addAll(usuarios);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ListaUsuariosActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarListeners() {
        // Listener para el botón de agregar usuario
        fabAgregarUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(ListaUsuariosActivity.this, CrearEditarUsuarioActivity.class);
            startActivity(intent);
        });

        // Listener para búsqueda
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                aplicarFiltros();
            }
        });

        // Listener para chips de filtrado
        chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            aplicarFiltros();
        });
    }

    private void aplicarFiltros() {
        String textoBusqueda = etBuscar.getText().toString().toLowerCase().trim();

        // Aplicar filtro de texto y chip seleccionado
        List<Usuario> usuariosFiltrados = new ArrayList<>();
        for (Usuario usuario : listaUsuariosCompleta) {
            boolean pasaFiltroBusqueda = usuario.getNombre().toLowerCase().contains(textoBusqueda) ||
                    usuario.getEmail().toLowerCase().contains(textoBusqueda);

            boolean pasaFiltroChip = true;
            int chipId = chipGroupFiltros.getCheckedChipId();

            if (chipId == R.id.chipAdmins) {
                pasaFiltroChip = "ADMINISTRADOR".equals(usuario.getRol());
            } else if (chipId == R.id.chipRepartidores) {
                pasaFiltroChip = "REPARTIDOR".equals(usuario.getRol());
            } else if (chipId == R.id.chipSupervisores) {
                pasaFiltroChip = "SUPERVISOR".equals(usuario.getRol());
            } else if (chipId == R.id.chipDesactivados) {
                pasaFiltroChip = !usuario.isActivo();
            }

            if (pasaFiltroBusqueda && pasaFiltroChip) {
                usuariosFiltrados.add(usuario);
            }
        }

        listaUsuariosFiltrada.clear();
        listaUsuariosFiltrada.addAll(usuariosFiltrados);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onUsuarioClick(Usuario usuario) {
        Intent intent = new Intent(this, DetalleUsuarioActivity.class);
        intent.putExtra("USUARIO_ID", usuario.getId());
        startActivity(intent);
    }

    @Override
    public void onEditarClick(Usuario usuario) {
        Intent intent = new Intent(this, CrearEditarUsuarioActivity.class);
        intent.putExtra("USUARIO_ID", usuario.getId());
        startActivity(intent);
    }

    @Override
    public void onEliminarClick(Usuario usuario) {
        mostrarDialogoConfirmacion(usuario);
    }

    private void mostrarDialogoConfirmacion(Usuario usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Desactivar usuario");
        builder.setMessage("¿Estás seguro de que deseas desactivar el usuario " + usuario.getNombre() + "?");
        builder.setPositiveButton("Desactivar", (dialog, which) -> {
            desactivarUsuario(usuario.getId());
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void desactivarUsuario(int id) {
        usuarioController.eliminarUsuario(id, new UsuarioController.OperacionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ListaUsuariosActivity.this, "Usuario desactivado correctamente", Toast.LENGTH_SHORT).show();
                cargarUsuarios(); // Recargar la lista
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ListaUsuariosActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuarios(); // Recargar usuarios al volver a la activity
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