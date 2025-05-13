package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.IncidenciaAdapter;
import com.trazabilidad.app.database.IncidenciaDAO;
import com.trazabilidad.app.models.Incidencia;
import com.trazabilidad.app.utils.SessionManager;

import java.util.List;

public class IncidenciasListActivity extends AppCompatActivity {

    private static final String TAG = "IncidenciasListActivity";
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabNuevaIncidencia;
    private LinearLayout emptyStateContainer;
    private IncidenciaDAO incidenciaDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidencias_list);

        inicializarUI();
        configurarToolbar();
        configurarEventos();
        inicializarControladores();
        cargarIncidencias();
    }

    private void inicializarUI() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL));
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");  // Título vacío porque lo manejamos en el collapsing toolbar
        }
    }

    private void configurarEventos() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimaryVariant,
                R.color.colorSecondary
        );

        swipeRefreshLayout.setOnRefreshListener(this::cargarIncidencias);

    }

    private void inicializarControladores() {
        incidenciaDAO = new IncidenciaDAO(this);
        sessionManager = new SessionManager(this);
    }

    private void cargarIncidencias() {
        swipeRefreshLayout.setRefreshing(true);
        try {
            int usuarioId = sessionManager.getUsuarioDetails().getId();
            List<Incidencia> incidencias = incidenciaDAO.obtenerIncidenciasPorUsuario(usuarioId);

            // Actualizar UI según si hay datos o no
            if (incidencias.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateContainer.setVisibility(View.GONE);

                IncidenciaAdapter adapter = new IncidenciaAdapter(incidencias, this::actualizarEstadoIncidencia);
                recyclerView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar incidencias: " + e.getMessage());
            Snackbar.make(recyclerView, "Error al cargar incidencias", Snackbar.LENGTH_LONG)
                    .setAction("Reintentar", v -> cargarIncidencias())
                    .show();
        } finally {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void actualizarEstadoIncidencia(Incidencia incidencia, String nuevoEstado) {
        try {
            boolean resultado = incidenciaDAO.actualizarEstadoIncidencia(incidencia.getId(), nuevoEstado);
            if (resultado) {
                // Mensaje con el nuevo estado
                String mensaje = "Estado actualizado a " + nuevoEstado;

                // Cambiar el color del Snackbar según el estado
                Snackbar snackbar = Snackbar.make(recyclerView, mensaje, Snackbar.LENGTH_SHORT);

                if (nuevoEstado.equals(Incidencia.ESTADO_RESUELTO)) {
                    snackbar.setBackgroundTint(getResources().getColor(R.color.success, null));
                } else {
                    snackbar.setBackgroundTint(getResources().getColor(R.color.warning, null));
                }

                snackbar.show();

                // Recargar la lista para reflejar el cambio
                cargarIncidencias();
            } else {
                Snackbar.make(recyclerView, "Error al actualizar estado", Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar estado: " + e.getMessage());
            Snackbar.make(recyclerView, "Error al actualizar estado", Snackbar.LENGTH_SHORT).show();
        }
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
    protected void onResume() {
        super.onResume();
        // Recargar datos al volver a la actividad
        cargarIncidencias();
    }
}