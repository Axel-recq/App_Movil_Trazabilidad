package com.trazabilidad.app.activities;

import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.ProductoAdapter;
import com.trazabilidad.app.controllers.PedidoController;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Producto;
import com.trazabilidad.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class DetallePedidoActivity extends AppCompatActivity {

    private static final String TAG = "DetallePedidoActivity";
    private TextView tvNumeroPedido, tvCliente, tvDireccion, tvFecha;
    private Chip chipEstado,chipProductCount;
    private RecyclerView recyclerProductos;
    private Button btnVerMapa, btnEntregado, btnIncidencia;
    private ProgressBar progressBar;
    private PedidoController pedidoController;
    private int pedidoId = -1;
    private Pedido pedidoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pedido);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.detalle_pedido);
        }

        // Inicializar UI y eventos
        inicializarUI();

        // Obtener el ID del pedido del intent
        if (getIntent() != null && getIntent().hasExtra("PEDIDO_ID")) {
            pedidoId = getIntent().getIntExtra("PEDIDO_ID", -1);
            Log.d(TAG, "ID del pedido recibido: " + pedidoId);
        } else {
            Log.e(TAG, "No se recibió ID de pedido en el intent");
        }

        if (pedidoId == -1) {
            Toast.makeText(this, R.string.error_pedido_no_encontrado, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ID de pedido no válido: -1");
            finish();
            return;
        }

        configurarEventos();
        pedidoController = new PedidoController(this);

        // Cargar detalle del pedido
        cargarDetallePedido();
    }

    private void inicializarUI() {
        tvNumeroPedido = findViewById(R.id.tvNumeroPedido);
        tvCliente = findViewById(R.id.tvCliente);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvFecha = findViewById(R.id.tvFecha);
        chipEstado = findViewById(R.id.chipEstado);
        chipProductCount = findViewById(R.id.chipProductCount);
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
        Log.d(TAG, "Iniciando carga de detalle del pedido con ID: " + pedidoId);

        pedidoController.obtenerDetallePedido(pedidoId, new PedidoController.PedidoDetalleCallback() {
            @Override
            public void onSuccess(Pedido pedido, List<Producto> productos) {
                Log.d(TAG, "Detalle del pedido cargado exitosamente. Productos: " +
                        (productos != null ? productos.size() : 0));
                progressBar.setVisibility(View.GONE);
                pedidoActual = pedido;

                // Actualizar la UI con los datos del pedido
                actualizarUI(pedido, productos != null ? productos : new ArrayList<>());
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error al cargar detalle del pedido: " + message);
                Toast.makeText(DetallePedidoActivity.this,
                        "Error al cargar el pedido: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUI(Pedido pedido, List<Producto> productos) {
        try {
            // Actualizar la información del pedido
            tvNumeroPedido.setText(getString(R.string.pedido_numero, pedido.getNumero()));
            tvCliente.setText(getString(R.string.cliente, pedido.getCliente()));
            tvDireccion.setText(getString(R.string.direccion, pedido.getDireccion()));
            tvFecha.setText(getString(R.string.fecha, DateUtils.formatDate(pedido.getFecha())));

            // Actualizar el chip de estado
            chipEstado.setText(pedido.getEstado());
            int cantidadProductos = productos.size();
            chipProductCount.setText(String.valueOf(cantidadProductos));
            // Configurar color del chip según el estado
            configurarChipEstado(pedido.getEstado());

            // Verificar si el pedido puede ser marcado como entregado
            boolean entregable = "ASIGNADO".equals(pedido.getEstado()) || "EN_RUTA".equals(pedido.getEstado());
            btnEntregado.setEnabled(entregable);

            // Configurar el adaptador de productos
            if (!productos.isEmpty()) {
                recyclerProductos.setAdapter(new ProductoAdapter(DetallePedidoActivity.this, productos));
            } else {
                // Mostrar mensaje si no hay productos
                Toast.makeText(this, "Este pedido no tiene productos asociados", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar UI: " + e.getMessage(), e);
            Toast.makeText(this, "Error al mostrar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void configurarChipEstado(String estado) {
        int colorFondo;

        // Determinar el color según el estado
        switch (estado.toUpperCase()) {
            case "ENTREGADO":
                colorFondo = R.color.success;
                break;
            case "EN_RUTA":
                colorFondo = R.color.info;
                break;
            case "ASIGNADO":
                colorFondo = R.color.colorPrimary;
                break;
            case "INCIDENCIA":
                colorFondo = R.color.error;
                break;
            default:
                colorFondo = R.color.colorPrimary;
                break;
        }

        chipEstado.setChipBackgroundColorResource(colorFondo);
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
                chipEstado.setText("ENTREGADO");
                configurarChipEstado("ENTREGADO");
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
        if (pedidoId != -1) {
            cargarDetallePedido();
        }
    }
}