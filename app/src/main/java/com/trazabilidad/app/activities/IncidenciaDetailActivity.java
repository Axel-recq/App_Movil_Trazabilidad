package com.trazabilidad.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Incidencia;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class IncidenciaDetailActivity extends AppCompatActivity {

    private Incidencia incidencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidencia_detail);

        // Configurar la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        // Obtener la incidencia del intent
        incidencia = (Incidencia) getIntent().getSerializableExtra("INCIDENCIA");
        if (incidencia != null) {
            setupViews();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "No se pudo cargar la información de la incidencia",
                    Snackbar.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupViews() {
        // Referencias a las vistas
        ImageView ivFoto = findViewById(R.id.ivFoto);
        TextView tvDescripcion = findViewById(R.id.tvDescripcion);
        Chip chipTipo = findViewById(R.id.chipTipo);
        Chip chipEstado = findViewById(R.id.chipEstado);
        TextView tvFecha = findViewById(R.id.tvFecha);
        TextView tvPedidoId = findViewById(R.id.tvPedidoId);
        MaterialButton btnAccion = findViewById(R.id.btnAccion);

        // Configurar datos
        tvDescripcion.setText(incidencia.getDescripcion());
        chipTipo.setText("Tipo: " + incidencia.getTipo());
        chipEstado.setText("Estado: " + incidencia.getEstado());

        // Formatear fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvFecha.setText(sdf.format(incidencia.getFecha()));

        // Configurar información del pedido
        if (incidencia.getPedidoId() > 0) {
            tvPedidoId.setText("Pedido #" + incidencia.getPedidoId());
        } else {
            tvPedidoId.setText("Sin pedido asociado");
        }

        // Cargar imagen si existe
        String fotoUri = incidencia.getFotoUri();
        if (fotoUri != null && !fotoUri.isEmpty()) {
            ivFoto.setImageURI(Uri.parse(fotoUri));
        } else {
            ivFoto.setImageResource(R.drawable.camera); // Asumiendo que tienes este drawable
        }

        // Configurar botón según estado
        setupButton(btnAccion);

        // Configurar color del chip de estado según tipo de incidencia
        setupChipColor(chipEstado);
    }

    private void setupButton(MaterialButton btnAccion) {
        String estado = incidencia.getEstado().toLowerCase();
        if (estado.contains("pendiente") || estado.contains("abierta")) {
            btnAccion.setText("Marcar como resuelta");
            btnAccion.setOnClickListener(v -> resolverIncidencia());
        } else if (estado.contains("proceso")) {
            btnAccion.setText("Finalizar incidencia");
            btnAccion.setOnClickListener(v -> finalizarIncidencia());
        } else {
            btnAccion.setText("Reabrir incidencia");
            btnAccion.setOnClickListener(v -> reabrirIncidencia());
        }
    }

    private void setupChipColor(Chip chipEstado) {
        String estado = incidencia.getEstado().toLowerCase();
        if (estado.contains("pendiente") || estado.contains("abierta")) {
            chipEstado.setChipBackgroundColorResource(R.color.status_pending);
        } else if (estado.contains("proceso")) {
            chipEstado.setChipBackgroundColorResource(R.color.status_delivering);
        } else if (estado.contains("resuelta") || estado.contains("cerrada")) {
            chipEstado.setChipBackgroundColorResource(R.color.status_delivered);
        } else {
            chipEstado.setChipBackgroundColorResource(R.color.status_incident);
        }
    }

    // Métodos para manejar acciones de la incidencia
    private void resolverIncidencia() {
        // Lógica para marcar como resuelta
        Snackbar.make(findViewById(android.R.id.content),
                "Incidencia marcada como resuelta",
                Snackbar.LENGTH_SHORT).show();
    }

    private void finalizarIncidencia() {
        // Lógica para finalizar
        Snackbar.make(findViewById(android.R.id.content),
                "Incidencia finalizada",
                Snackbar.LENGTH_SHORT).show();
    }

    private void reabrirIncidencia() {
        // Lógica para reabrir
        Snackbar.make(findViewById(android.R.id.content),
                "Incidencia reabierta",
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}