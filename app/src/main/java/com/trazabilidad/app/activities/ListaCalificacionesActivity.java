package com.trazabilidad.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.CalificacionAdapter;
import com.trazabilidad.app.controllers.CalificacionController;
import com.trazabilidad.app.models.Calificacion;

import java.util.ArrayList;
import java.util.List;

public class ListaCalificacionesActivity extends AppCompatActivity implements CalificacionAdapter.OnCalificacionClickListener {

    private static final String TAG = "ListaCalificacionesActivity";

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View rootView;
    private Button btnVerTodas;
    private SwipeRefreshLayout swipeRefreshLayout;
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
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Configurar RecyclerView con el nuevo adaptador
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CalificacionAdapter(this);
        adapter.setOnCalificacionClickListener(this);
        recyclerView.setAdapter(adapter);

        // Hide btnVerTodas since we always show all calificaciones
        btnVerTodas.setVisibility(View.GONE);

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::cargarCalificaciones);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark
        );
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
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        calificacionController.obtenerTodasCalificaciones(new CalificacionController.CalificacionesCallback() {
            @Override
            public void onSuccess(List<Calificacion> calificaciones) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Loaded " + calificaciones.size() + " calificaciones");
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (calificaciones.isEmpty()) {
                        mostrarVista(tvEmpty);
                    } else {
                        mostrarVista(recyclerView);
                        adapter.submitList(calificaciones);

                        if (pedidoId != -1) {
                            scrollToPedido(calificaciones);
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading calificaciones: " + message);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    tvEmpty.setText(R.string.error_cargar_calificaciones);
                    mostrarVista(tvEmpty);
                    mostrarError(message);
                });
            }
        });
    }

    private void scrollToPedido(List<Calificacion> calificaciones) {
        for (int i = 0; i < calificaciones.size(); i++) {
            if (calificaciones.get(i).getPedidoId() == pedidoId) {
                Log.d(TAG, "Scrolling to position " + i + " for pedidoId: " + pedidoId);
                recyclerView.smoothScrollToPosition(i);
                break;
            }
        }
    }

    private void mostrarVista(View viewToShow) {
        recyclerView.setVisibility(viewToShow == recyclerView ? View.VISIBLE : View.GONE);
        tvEmpty.setVisibility(viewToShow == tvEmpty ? View.VISIBLE : View.GONE);
    }

    private void mostrarError(String mensaje) {
        Snackbar.make(rootView, mensaje, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCalificacionClick(Calificacion calificacion) {
        // Aquí puedes manejar el clic en una calificación
        // Por ejemplo, mostrar detalles completos de la calificación
        Toast.makeText(this, "Calificación del pedido #" + calificacion.getPedidoId(), Toast.LENGTH_SHORT).show();

        // También podrías iniciar una nueva actividad para mostrar detalles
        /*
        Intent intent = new Intent(this, DetalleCalificacionActivity.class);
        intent.putExtra("pedidoId", calificacion.getPedidoId());
        startActivity(intent);
        */
    }
}