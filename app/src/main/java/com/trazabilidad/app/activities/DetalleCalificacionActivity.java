package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.CalificacionController;
import com.trazabilidad.app.models.Calificacion;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetalleCalificacionActivity extends AppCompatActivity {

    private static final String TAG = "DetalleCalificacionActivity";

    private MaterialToolbar toolbar;
    private TextView tvPedidoNumero;
    private TextView tvCliente;
    private RatingBar ratingBar;
    private TextView tvCategoria;
    private TextView tvComentario;
    private TextView tvFecha;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Button btnVerTodas;
    private CalificacionController calificacionController;
    private int pedidoId = -1;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_calificacion);

        inicializarUI();
        configurarToolbar();
        inicializarControladores();

        pedidoId = getIntent().getIntExtra("pedidoId", -1);
        Log.d(TAG, "Received pedidoId: " + pedidoId);
        if (pedidoId == -1) {
            Toast.makeText(this, "ID de pedido inválido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        cargarCalificacion(pedidoId);
    }

    private void inicializarUI() {
        toolbar = findViewById(R.id.toolbar);
        tvPedidoNumero = findViewById(R.id.tvPedidoNumero);
        tvCliente = findViewById(R.id.tvCliente);
        ratingBar = findViewById(R.id.ratingBar);
        tvCategoria = findViewById(R.id.tvCategoria);
        tvComentario = findViewById(R.id.tvComentario);
        tvFecha = findViewById(R.id.tvFecha);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnVerTodas = findViewById(R.id.btnVerTodas);

        btnVerTodas.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to ListaCalificacionesActivity");
            Intent intent = new Intent(DetalleCalificacionActivity.this, ListaCalificacionesActivity.class);
            startActivity(intent);
        });
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.detalle_calificacion);
        }
    }

    private void inicializarControladores() {
        calificacionController = new CalificacionController(this);
    }

    private void cargarCalificacion(int pedidoId) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        findViewById(R.id.layoutCalificacion).setVisibility(View.GONE);
        btnVerTodas.setVisibility(View.GONE);

        calificacionController.obtenerCalificacionPorPedido(pedidoId, new CalificacionController.CalificacionCallback() {
            @Override
            public void onSuccess(Calificacion calificacion) {
                Log.d(TAG, "Calificación encontrada: " + calificacion.toString());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    findViewById(R.id.layoutCalificacion).setVisibility(View.VISIBLE);
                    btnVerTodas.setVisibility(View.VISIBLE);
                    mostrarCalificacion(calificacion);
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error al obtener calificación: " + message);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No hay calificación para este pedido.");
                    findViewById(R.id.layoutCalificacion).setVisibility(View.GONE);
                    btnVerTodas.setVisibility(View.VISIBLE);
                    Toast.makeText(DetalleCalificacionActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void mostrarCalificacion(Calificacion calificacion) {
        tvPedidoNumero.setText(getString(R.string.pedido_numero, String.valueOf(calificacion.getPedidoId())));
        tvCliente.setText(getString(R.string.cliente, calificacion.getNombreCliente() != null ? calificacion.getNombreCliente() : "Usuario ID: " + calificacion.getUsuarioId()));
        ratingBar.setRating(calificacion.getValor());
        tvCategoria.setText(getString(R.string.categoria, calificacion.getCategoria()));
        tvComentario.setText(calificacion.getComentario() != null ? calificacion.getComentario() : "Sin comentario");
        tvFecha.setText(dateFormat.format(calificacion.getFecha()));

        switch (calificacion.getCategoria()) {
            case "Muy bueno":
                tvCategoria.setTextColor(getResources().getColor(R.color.green));
                break;
            case "Bueno":
                tvCategoria.setTextColor(getResources().getColor(R.color.yellow));
                break;
            case "Malo":
                tvCategoria.setTextColor(getResources().getColor(R.color.red));
                break;
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