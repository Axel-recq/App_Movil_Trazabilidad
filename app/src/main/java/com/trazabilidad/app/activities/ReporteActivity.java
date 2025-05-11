package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private MaterialCardView cardResultados;
    private MaterialButton btnCompartir;
    private FloatingActionButton fabHelp;

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
        cardResultados = findViewById(R.id.cardResultados);
        btnCompartir = findViewById(R.id.btnCompartir);
        fabHelp = findViewById(R.id.fabHelp);

        tvFechaReporte.setText("Fecha: " + DateUtils.formatDate(new Date()));
    }

    private void inicializarControladores() {
        reporteController = new ReporteController(this);
        sessionManager = new SessionManager(this);
    }

    private void configurarListeners() {
        btnGenerar.setOnClickListener(v -> generarReporte());
        btnCompartir.setOnClickListener(v -> compartirResultados());
        fabHelp.setOnClickListener(v -> mostrarAyudaReportes());
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
        cardResultados.setVisibility(View.GONE);

        reporteController.generarReporte(tipoReporte, usuarioId, new ReporteController.ReporteCallback() {
            @Override
            public void onSuccess(Map<String, Integer> resultados) {
                progressBar.setVisibility(View.GONE);
                mostrarResultadosInteger(resultados);
            }

            @Override
            public void onSuccessDouble(Map<String, Double> resultados) {
                progressBar.setVisibility(View.GONE);
                mostrarResultadosDouble(resultados);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ReporteActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarResultadosInteger(Map<String, Integer> resultados) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : resultados.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n\n");
        }
        tvResultados.setText(sb.toString());
        cardResultados.setVisibility(View.VISIBLE);
    }

    private void mostrarResultadosDouble(Map<String, Double> resultados) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : resultados.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(String.format("%.2f", entry.getValue())).append("\n\n");
        }
        tvResultados.setText(sb.toString());
        cardResultados.setVisibility(View.VISIBLE);
    }

    private void compartirResultados() {
        String resultados = tvResultados.getText().toString();
        if (resultados.isEmpty()) {
            Toast.makeText(this, "No hay resultados para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte generado");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Reporte del " + DateUtils.formatDate(new Date()) + "\n\n" + resultados);
        startActivity(Intent.createChooser(shareIntent, "Compartir reporte"));
    }

    private void mostrarAyudaReportes() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.help_title)
                .setMessage("La sección de reportes permite generar diferentes tipos de informes:\n\n" +
                        "• Pedidos por Estado: Muestra la cantidad de pedidos en cada estado.\n\n" +
                        "• Incidencias por Tipo: Visualiza las incidencias agrupadas por categoría.\n\n" +
                        "• Resumen de Entregas: Resume la actividad de entregas reciente.")
                .setPositiveButton(R.string.ok, null)
                .show();
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