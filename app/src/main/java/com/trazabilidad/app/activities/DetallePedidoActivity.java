package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
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

import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.ProductoAdapter;
import com.trazabilidad.app.controllers.PedidoController;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Producto;
import com.trazabilidad.app.utils.DateUtils;

import java.util.List;

public class DetallePedidoActivity extends AppCompatActivity {

    private TextView tvNumeroPedido, tvCliente, tvDireccion, tvFecha, tvEstado;
    private RecyclerView recyclerProductos;
    private Button btnVerMapa, btnEntregado, btnIncidencia;
    private ProgressBar progressBar;

    private PedidoController pedidoController;
    private int pedidoId;
    private Pedido pedidoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pedido);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.detalle_pedido);
        }

        pedidoId = getIntent().getIntExtra("PEDIDO_ID", -1);
        if (pedidoId == -1) {
            Toast.makeText(this, R.string.error_pedido_no_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarUI();
        configurarEventos();
        pedidoController = new PedidoController(this);

        cargarDetallePedido();
    }

    private void inicializarUI() {
        tvNumeroPedido = findViewById(R.id.tvNumeroPedido);
        tvCliente = findViewById(R.id.tvCliente);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvFecha = findViewById(R.id.tvFecha);
        tvEstado = findViewById(R.id.tvEstado);
        recyclerProductos = findViewById(R.id.recyclerProductos);
        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));
        btnVerMapa = findViewById(R.id.btnVerMapa);
        btnEntregado = findViewById(R.id.btnEntregado);
        btnIncidencia = findViewById(R.id.btnIncidencia);
        progressBar = findViewById(R.id.progressBar);
    }

    private void configurarEventos() {
        btnVerMapa.setOnClickListener(v -> {
            if (pedidoActual != null) {
                Intent intent = new Intent(this, MapaActivity.class);
                intent.putExtra("LATITUD", pedidoActual.getLatitud());
                intent.putExtra("LONGITUD", pedidoActual.getLongitud());
                intent.putExtra("DIRECCION", pedidoActual.getDireccion());
                startActivity(intent);
            } else {
                Toast.makeText(this, "No hay datos de ubicación disponibles", Toast.LENGTH_SHORT).show();
            }
        });

        btnEntregado.setOnClickListener(v -> {
            if (pedidoActual != null) {
                marcarComoEntregado();
            }
        });

        btnIncidencia.setOnClickListener(v -> {
            if (pedidoActual != null) {
                Intent intent = new Intent(this, IncidenciaActivity.class);
                intent.putExtra("PEDIDO_ID", pedidoId);
                startActivity(intent);
            }
        });
    }

    private void cargarDetallePedido() {
        progressBar.setVisibility(View.VISIBLE);

        pedidoController.obtenerDetallePedido(pedidoId, new PedidoController.PedidoDetalleCallback() {
            @Override
            public void onSuccess(Pedido pedido, List<Producto> productos) {
                progressBar.setVisibility(View.GONE);
                pedidoActual = pedido;

                tvNumeroPedido.setText(getString(R.string.pedido_numero, pedido.getNumero()));
                tvCliente.setText(getString(R.string.cliente, pedido.getCliente()));
                tvDireccion.setText(getString(R.string.direccion, pedido.getDireccion()));
                tvFecha.setText(getString(R.string.fecha, DateUtils.formatDate(pedido.getFecha())));
                tvEstado.setText(getString(R.string.estado, pedido.getEstado()));

                boolean entregable = "ASIGNADO".equals(pedido.getEstado()) || "EN_RUTA".equals(pedido.getEstado());
                btnEntregado.setEnabled(entregable);

                recyclerProductos.setAdapter(new ProductoAdapter(DetallePedidoActivity.this, productos));
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetallePedidoActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void marcarComoEntregado() {
        progressBar.setVisibility(View.VISIBLE);
        btnEntregado.setEnabled(false);

        pedidoController.marcarPedidoComoEntregado(pedidoId, new PedidoController.OperacionCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetallePedidoActivity.this, R.string.pedido_marcado_entregado, Toast.LENGTH_SHORT).show();
                pedidoActual.setEstado("ENTREGADO");
                tvEstado.setText(getString(R.string.estado, "ENTREGADO"));
                btnEntregado.setEnabled(false);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                btnEntregado.setEnabled(true);
                Toast.makeText(DetallePedidoActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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
        cargarDetallePedido();
    }
}