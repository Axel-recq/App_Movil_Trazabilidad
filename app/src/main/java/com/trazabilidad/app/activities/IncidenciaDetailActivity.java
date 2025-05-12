package com.trazabilidad.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Incidencia;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class IncidenciaDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidencia_detail);

        Incidencia incidencia = (Incidencia) getIntent().getSerializableExtra("INCIDENCIA");
        if (incidencia != null) {
            ImageView ivFoto = findViewById(R.id.ivFoto);
            TextView tvDescripcion = findViewById(R.id.tvDescripcion);
            TextView tvTipo = findViewById(R.id.tvTipo);
            TextView tvEstado = findViewById(R.id.tvEstado);
            TextView tvFecha = findViewById(R.id.tvFecha);
            TextView tvPedidoId = findViewById(R.id.tvPedidoId);

            tvDescripcion.setText(incidencia.getDescripcion());
            tvTipo.setText("Tipo: " + incidencia.getTipo());
            tvEstado.setText("Estado: " + incidencia.getEstado());
            tvFecha.setText("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(incidencia.getFecha()));
            tvPedidoId.setText(incidencia.getPedidoId() > 0 ? "Pedido: " + incidencia.getPedidoId() : "Sin pedido asociado");

            String fotoUri = incidencia.getFotoUri();
            if (fotoUri != null && !fotoUri.isEmpty()) {
                ivFoto.setImageURI(Uri.parse(fotoUri));
            } else {
                ivFoto.setImageResource(android.R.drawable.ic_menu_camera); // Placeholder
            }
        }
    }
}