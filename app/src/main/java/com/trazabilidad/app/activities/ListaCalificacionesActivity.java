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
import com.trazabilidad.app.adapter.CalificacionAdapter;
import com.trazabilidad.app.controllers.CalificacionController;
import com.trazabilidad.app.models.Calificacion;

import java.util.ArrayList;
import java.util.List;

public class ListaCalificacionesActivity extends AppCompatActivity {

    private static final String TAG = "ListaCalificacionesActivity";

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View rootView;
    private Button btnVerTodas;
    private CalificacionController calificacionController;
    private CalificacionAdapter adapter;
    private int pedidoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_calificaciones);

        inicializarUI();
        configurarToolbar();
        inicializarControladores();

        pedidoId = getIntent().getIntExtra("pedidoId", -1);
        Log.d(TAG, "Received pedidoId: " + pedidoId);
        cargarCalificaciones();
    }

    private void inicializarUI() {
        rootView = findViewById(android.R.id.content);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.rvCalificaciones);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnVerTodas = findViewById(R.id.btnVerTodas);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CalificacionAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Hide btnVerTodas since we always show all calificaciones
        btnVerTodas.setVisibility(View.GONE);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.lista_calificaciones);
        }
    }

    private void inicializarControladores() {
        calificacionController = new CalificacionController(this);
    }

    private void cargarCalificaciones() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        calificacionController.obtenerTodasCalificaciones(new CalificacionController.CalificacionesCallback() {
            @Override
            public void onSuccess(List<Calificacion> calificaciones) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Loaded " + calificaciones.size() + " calificaciones");
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.updateCalificaciones(calificaciones);
                    if (pedidoId != -1) {
                        for (int i = 0; i < calificaciones.size(); i++) {
                            if (calificaciones.get(i).getPedidoId() == pedidoId) {
                                Log.d(TAG, "Scrolling to position " + i + " for pedidoId: " + pedidoId);
                                recyclerView.smoothScrollToPosition(i);
                                break;
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading calificaciones: " + message);
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No hay calificaciones registradas.");
                    recyclerView.setVisibility(View.GONE);
                });
            }
        });
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