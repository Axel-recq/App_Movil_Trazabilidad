package com.trazabilidad.app.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.ReporteController;
import com.trazabilidad.app.utils.DateUtils;
import com.trazabilidad.app.utils.SessionManager;

import java.util.Date;
import java.util.Map;

public class ReporteActivity extends AppCompatActivity {

    private Spinner spinnerTipoReporte;
    private Button btnGenerar;
    private ProgressBar progressBar;
    private TextView tvResultados;
    private TextView tvFechaReporte;
    // Nuevo elemento de la UI
    private MaterialCardView cardResultados;

    private ReporteController reporteController;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reportes");
        }

        inicializarUI();
        inicializarControladores();
        configurarListeners();
        configurarSpinner();
    }

    private void inicializarUI() {
        spinnerTipoReporte = findViewById(R.id.spinnerTipoReporte);
        btnGenerar = findViewById(R.id.btnGenerar);
        progressBar = findViewById(R.id.progressBar);
        tvResultados = findViewById(R.id.tvResultados);
        tvFechaReporte = findViewById(R.id.tvFechaReporte);
        // Inicializar el nuevo componente
        cardResultados = findViewById(R.id.cardResultados);

        tvFechaReporte.setText("Fecha: " + DateUtils.formatDate(new Date()));
    }

    private void inicializarControladores() {
        reporteController = new ReporteController(this);
        sessionManager = new SessionManager(this);
    }

    private void configurarListeners() {
        btnGenerar.setOnClickListener(v -> generarReporte());
    }

    private void configurarSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_reportes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoReporte.setAdapter(adapter);
        spinnerTipoReporte.setSelection(0, false);
    }

    private void generarReporte() {
        String tipoReporte = spinnerTipoReporte.getSelectedItem().toString();
        if ("Seleccione un tipo de reporte".equals(tipoReporte)) {
            Toast.makeText(this, "Por favor, seleccione un tipo válido de reporte", Toast.LENGTH_SHORT).show();
            return;
        }

        int usuarioId = sessionManager.getUsuarioDetails().getId();

        progressBar.setVisibility(View.VISIBLE);
        cardResultados.setVisibility(View.GONE); // Ocultar resultados mientras carga

        reporteController.generarReporte(tipoReporte, usuarioId, new ReporteController.ReporteCallback() {
            @Override
            public void onSuccess(Map<String, Integer> resultados) {
                progressBar.setVisibility(View.GONE);
                mostrarResultados(resultados);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ReporteActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarResultados(Map<String, Integer> resultados) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Integer> entry : resultados.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n\n");
        }

        tvResultados.setText(sb.toString());
        cardResultados.setVisibility(View.VISIBLE); // Hacer visible la tarjeta de resultados
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