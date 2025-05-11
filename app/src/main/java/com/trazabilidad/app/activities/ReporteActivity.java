package com.trazabilidad.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.ReporteController;
import com.trazabilidad.app.utils.DateUtils;
import com.trazabilidad.app.utils.SessionManager;

import java.util.Date;
import java.util.Map;

public class ReporteActivity extends AppCompatActivity {

    private Spinner spinnerTipoReporte;
    private Button btnGenerar;
    private Button btnCompartir;
    private ProgressBar progressBar;
    private TextView tvResultados;
    private TextView tvFechaReporte;
    private MaterialCardView cardResultados;
    private ConstraintLayout loadingContainer;
    private FloatingActionButton fabHelp;

    private ReporteController reporteController;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);

        inicializarUI();
        inicializarControladores();
        configurarListeners();
        configurarSpinner();
        configureToolbar();
    }

    private void configureToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.menu_reports);
        }
    }

    private void inicializarUI() {
        spinnerTipoReporte = findViewById(R.id.spinnerTipoReporte);
        btnGenerar = findViewById(R.id.btnGenerar);
        btnCompartir = findViewById(R.id.btnCompartir);
        progressBar = findViewById(R.id.progressBar);
        tvResultados = findViewById(R.id.tvResultados);
        tvFechaReporte = findViewById(R.id.tvFechaReporte);
        cardResultados = findViewById(R.id.cardResultados);
        loadingContainer = findViewById(R.id.loadingContainer);
        fabHelp = findViewById(R.id.fabHelp);

        // Establecer la fecha actual con formato adecuado
        tvFechaReporte.setText("Fecha: " + DateUtils.formatDate(new Date()));
    }

    private void inicializarControladores() {
        reporteController = new ReporteController(this);
        sessionManager = new SessionManager(this);
    }

    private void configurarListeners() {
        btnGenerar.setOnClickListener(v -> validarYGenerarReporte());

        btnCompartir.setOnClickListener(v -> compartirResultados());

        fabHelp.setOnClickListener(v -> mostrarAyudaReportes());
    }

    private void configurarSpinner() {
        // Usar un adaptador personalizado para el spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_reportes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoReporte.setAdapter(adapter);
        spinnerTipoReporte.setSelection(0, false);

        // Añadir listener al spinner para validación dinámica
        spinnerTipoReporte.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Habilitar o deshabilitar el botón según la selección
                btnGenerar.setEnabled(position != 0);

                if (position == 0) {
                    btnGenerar.setAlpha(0.5f);
                } else {
                    btnGenerar.setAlpha(1.0f);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                btnGenerar.setEnabled(false);
                btnGenerar.setAlpha(0.5f);
            }
        });
    }

    private void validarYGenerarReporte() {
        String tipoReporte = spinnerTipoReporte.getSelectedItem().toString();
        if ("Seleccione un tipo de reporte".equals(tipoReporte)) {
            Snackbar.make(btnGenerar, "Por favor, seleccione un tipo válido de reporte",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        generarReporte();
    }

    private void generarReporte() {
        String tipoReporte = spinnerTipoReporte.getSelectedItem().toString();
        int usuarioId = sessionManager.getUsuarioDetails().getId();

        // Mostrar el contenedor de carga en lugar de solo el progressBar
        loadingContainer.setVisibility(View.VISIBLE);
        cardResultados.setVisibility(View.GONE);

        // Simular un pequeño retraso para mejor UX
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            reporteController.generarReporte(tipoReporte, usuarioId, new ReporteController.ReporteCallback() {
                @Override
                public void onSuccess(Map<String, Integer> resultados) {
                    loadingContainer.setVisibility(View.GONE);
                    mostrarResultados(resultados);
                    mostrarNotificacionExito();
                }

                @Override
                public void onError(String message) {
                    loadingContainer.setVisibility(View.GONE);
                    Snackbar.make(btnGenerar, message, Snackbar.LENGTH_LONG)
                            .setAction("Reintentar", v -> generarReporte())
                            .show();
                }
            });
        }, 800); // Retraso para mostrar la animación de carga
    }

    private void mostrarResultados(Map<String, Integer> resultados) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Integer> entry : resultados.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n\n");
        }

        tvResultados.setText(sb.toString());
        cardResultados.setVisibility(View.VISIBLE);

        // Animación simple para mostrar resultados
        cardResultados.setAlpha(0f);
        cardResultados.animate().alpha(1f).setDuration(500).start();
    }

    private void mostrarNotificacionExito() {
        Snackbar.make(cardResultados, "Reporte generado exitosamente", Snackbar.LENGTH_SHORT).show();
    }

    private void compartirResultados() {
        if (tvResultados.getText().toString().isEmpty()) {
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de " +
                spinnerTipoReporte.getSelectedItem().toString());
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Reporte generado el " +
                DateUtils.formatDate(new Date()) + ":\n\n" + tvResultados.getText().toString());

        startActivity(Intent.createChooser(shareIntent, "Compartir reporte mediante"));
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