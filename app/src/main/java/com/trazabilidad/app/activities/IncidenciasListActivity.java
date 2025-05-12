package com.trazabilidad.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private IncidenciaDAO incidenciaDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidencias_list);

        inicializarUI();
        configurarToolbar();
        inicializarControladores();
        cargarIncidencias();
    }

    private void inicializarUI() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lista de Incidencias");
        }
    }

    private void inicializarControladores() {
        incidenciaDAO = new IncidenciaDAO(this);
        sessionManager = new SessionManager(this);
    }

    private void cargarIncidencias() {
        try {
            int usuarioId = sessionManager.getUsuarioDetails().getId();
            List<Incidencia> incidencias = incidenciaDAO.obtenerIncidenciasPorUsuario(usuarioId);
            if (incidencias.isEmpty()) {
                Toast.makeText(this, "No hay incidencias registradas", Toast.LENGTH_SHORT).show();
            }
            IncidenciaAdapter adapter = new IncidenciaAdapter(incidencias, this::actualizarEstadoIncidencia);
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar incidencias: " + e.getMessage());
            Toast.makeText(this, "Error al cargar incidencias", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarEstadoIncidencia(Incidencia incidencia, String nuevoEstado) {
        try {
            boolean resultado = incidenciaDAO.actualizarEstadoIncidencia(incidencia.getId(), nuevoEstado);
            if (resultado) {
                Toast.makeText(this, "Estado actualizado a " + nuevoEstado, Toast.LENGTH_SHORT).show();
                cargarIncidencias(); // Recargar la lista para reflejar el cambio
            } else {
                Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar estado: " + e.getMessage());
            Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show();
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
}