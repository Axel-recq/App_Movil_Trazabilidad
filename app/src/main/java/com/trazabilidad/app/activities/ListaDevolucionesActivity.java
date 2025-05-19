package com.trazabilidad.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.DevolucionAdapter;
import com.trazabilidad.app.database.DevolucionDAO;
import com.trazabilidad.app.models.Devolucion;
import com.trazabilidad.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ListaDevolucionesActivity extends AppCompatActivity {

    private static final String TAG = "ListaDevolucionesActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View rootView;
    private Button btnVerTodas;

    // Data controllers
    private SessionManager sessionManager;
    private DevolucionDAO devolucionDAO;
    private DevolucionAdapter adapter;
    private int pedidoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_devoluciones);

        inicializarUI();
        configurarToolbar();
        inicializarControladores();

        // Validar que el usuario sea administrador o repartidor
        if (!sessionManager.isLoggedIn()) {
            mostrarError("Debe iniciar sesión para acceder.");
            finish();
            return;
        }
        String rol = sessionManager.getUsuarioDetails().getRol();
        if (!rol.equals("ADMINISTRADOR") && !rol.equals("REPARTIDOR")) {
            mostrarError("Acceso restringido. Solo para administradores y repartidores.");
            finish();
            return;
        }

        // Verificar si se pasó un pedidoId
        pedidoId = getIntent().getIntExtra("pedidoId", -1);

        // Cargar devoluciones
        cargarDevoluciones(pedidoId);
    }

    private void inicializarUI() {
        rootView = findViewById(android.R.id.content);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.rvDevoluciones);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnVerTodas = findViewById(R.id.btnVerTodas);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DevolucionAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Configurar botón Ver Todas
        btnVerTodas.setOnClickListener(v -> {
            pedidoId = -1;
            cargarDevoluciones(-1);
        });
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.lista_devoluciones);
        }
    }

    private void inicializarControladores() {
        sessionManager = new SessionManager(this);
        devolucionDAO = new DevolucionDAO(this);
    }

    private void cargarDevoluciones(int pedidoId) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        btnVerTodas.setVisibility(pedidoId != -1 ? View.VISIBLE : View.GONE);

        new Thread(() -> {
            try {
                List<Devolucion> devoluciones;
                if (pedidoId != -1) {
                    devoluciones = devolucionDAO.obtenerDevolucionesPorPedidoConDetalles(pedidoId);
                } else {
                    devoluciones = devolucionDAO.obtenerTodasDevolucionesConDetalles();
                }
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (devoluciones.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tvEmpty.setText(pedidoId != -1 ? "No hay devoluciones para este pedido." : "No hay devoluciones registradas.");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.actualizarDevoluciones(devoluciones);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error al cargar devoluciones: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    mostrarError("Error al cargar devoluciones: " + e.getMessage());
                });
            }
        }).start();
    }

    private void mostrarError(String mensaje) {
        Snackbar.make(rootView, mensaje, Snackbar.LENGTH_LONG).show();
        rootView.postDelayed(this::finish, 2000);
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