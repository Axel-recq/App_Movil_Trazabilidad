package com.trazabilidad.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.ProductoDetalleAdapter;
import com.trazabilidad.app.controllers.PedidoController;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Producto;
import com.trazabilidad.app.utils.DateUtils;
import com.trazabilidad.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class DetallePedidoActivity extends AppCompatActivity {

    private static final String TAG = "DetallePedidoActivity";
    private TextView tvNumeroPedido, tvCliente, tvDireccion, tvFecha;
    private Chip chipEstado, chipProductCount;
    private RecyclerView recyclerProductos;
    private MaterialButton btnVerMapa, btnEntregado, btnIncidencia, btnDevolver;
    private ProgressBar progressBar;
    private PedidoController pedidoController;
    private SessionManager sessionManager;
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

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

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
        btnDevolver = findViewById(R.id.btnDevolver);
        progressBar = findViewById(R.id.progressBar);
        // Set contentDescription programmatically
        btnVerMapa.setContentDescription(getString(R.string.ver_mapa_descripcion));
        btnIncidencia.setContentDescription(getString(R.string.reportar_incidencia));
        btnDevolver.setContentDescription(getString(R.string.devolver_pedido));

        // Controlar visibilidad de botones según el rol del usuario
        if (sessionManager.isRepartidor()) {
            // Repartidores y administradores ven todos los botones menos el de devolver
            btnVerMapa.setVisibility(View.VISIBLE);
            btnEntregado.setVisibility(View.VISIBLE);
            btnIncidencia.setVisibility(View.VISIBLE);
            btnDevolver.setVisibility(View.GONE);
        } else if (sessionManager.isCliente()) {
            // Clientes solo ven el botón de devolver
            btnVerMapa.setVisibility(View.GONE);
            btnEntregado.setVisibility(View.GONE);
            btnIncidencia.setVisibility(View.GONE);
            btnDevolver.setVisibility(View.VISIBLE);
        } else {
            // Por seguridad, ocultar todos los botones si el rol no está definido
            btnVerMapa.setVisibility(View.GONE);
            btnEntregado.setVisibility(View.GONE);
            btnIncidencia.setVisibility(View.GONE);
            btnDevolver.setVisibility(View.GONE);
        }
    }

    private void configurarEventos() {
        btnVerMapa.setOnClickListener(v -> {
            if (sessionManager.isRepartidor() && pedidoActual != null) {
                Intent intent = new Intent(this, MapaActivity.class);
                intent.putExtra("LATITUD", pedidoActual.getLatitud());
                intent.putExtra("LONGITUD", pedidoActual.getLongitud());
                intent.putExtra("DIRECCION", pedidoActual.getDireccion());
                startActivity(intent);
            } else {
                Toast.makeText(this, "No tienes permiso o no hay datos de ubicación", Toast.LENGTH_SHORT).show();
            }
        });

        btnEntregado.setOnClickListener(v -> {
            if (sessionManager.isRepartidor() && pedidoActual != null) {
                String estadoActual = pedidoActual.getEstado();
                String nuevoEstado = determinarNuevoEstado(estadoActual);
                if (nuevoEstado != null) {
                    actualizarEstadoPedido(nuevoEstado);
                } else {
                    Toast.makeText(this, "No se puede cambiar el estado desde " + estadoActual, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No tienes permiso para cambiar el estado", Toast.LENGTH_SHORT).show();
            }
        });

        btnIncidencia.setOnClickListener(v -> {
            if (sessionManager.isRepartidor() && pedidoActual != null) {
                Intent intent = new Intent(this, IncidenciaActivity.class);
                intent.putExtra("PEDIDO_ID", pedidoId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No tienes permiso para reportar incidencias", Toast.LENGTH_SHORT).show();
            }
        });

        btnDevolver.setOnClickListener(v -> {
            if (sessionManager.isCliente() && pedidoActual != null) {
                Intent intent = new Intent(this, GestionDevolucionActivity.class);
                intent.putExtra("pedidoId", pedidoId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No tienes permiso para devolver el pedido", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String determinarNuevoEstado(String estadoActual) {
        switch (estadoActual) {
            case "PENDIENTE":
                return "ASIGNADO";
            case "ASIGNADO":
                return "EN_RUTA";
            case "EN_RUTA":
                return "ENTREGADO";
            default:
                return null; // No hay transición disponible
        }
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

            // Configurar el botón según el estado (solo para repartidores/administradores)
            if (sessionManager.isRepartidor()) {
                String estadoActual = pedido.getEstado();
                switch (estadoActual) {
                    case "PENDIENTE":
                        btnEntregado.setText("ASIGNAR");
                        btnEntregado.setIconResource(R.drawable.ic_assignment);
                        btnEntregado.setContentDescription("Asignar el pedido a un repartidor");
                        btnEntregado.setEnabled(true);
                        break;
                    case "ASIGNADO":
                        btnEntregado.setText("INICIAR RUTA");
                        btnEntregado.setIconResource(R.drawable.ic_directions);
                        btnEntregado.setContentDescription("Iniciar la ruta de entrega");
                        btnEntregado.setEnabled(true);
                        break;
                    case "EN_RUTA":
                        btnEntregado.setText("ENTREGADO");
                        btnEntregado.setIconResource(R.drawable.ic_check_circle);
                        btnEntregado.setContentDescription("Marcar el pedido como entregado");
                        btnEntregado.setEnabled(true);
                        break;
                    case "ENTREGADO":
                        btnEntregado.setText("ENTREGADO");
                        btnEntregado.setIconResource(R.drawable.ic_check_circle);
                        btnEntregado.setContentDescription("Pedido ya entregado");
                        btnEntregado.setEnabled(false);
                        break;
                    default:
                        btnEntregado.setText("ACCIÓN NO DISPONIBLE");
                        btnEntregado.setIconResource(R.drawable.ic_check_circle);
                        btnEntregado.setContentDescription("Acción no disponible para el estado actual");
                        btnEntregado.setEnabled(false);
                        break;
                }
            }

            // Configurar el adaptador de productos usando ProductoDetalleAdapter
            if (!productos.isEmpty()) {
                recyclerProductos.setAdapter(new ProductoDetalleAdapter(
                        DetallePedidoActivity.this,
                        productos,
                        pedidoId,
                        new ProductoDetalleAdapter.OnProductoClickListener() {
                            @Override
                            public void onProductoClick(Producto producto) {
                                Toast.makeText(DetallePedidoActivity.this,
                                        "Producto seleccionado: " + producto.getNombre(),
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onDevolverProducto(int pedidoId, int productoId) {
                                if (sessionManager.isCliente()) {
                                    Intent intent = new Intent(DetallePedidoActivity.this, GestionDevolucionActivity.class);
                                    intent.putExtra("pedidoId", pedidoId);
                                    intent.putExtra("productoId", productoId);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(DetallePedidoActivity.this,
                                            "No tienes permiso para devolver productos",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                ));
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

    private void actualizarEstadoPedido(String nuevoEstado) {
        // Mostrar indicador de carga
        findViewById(R.id.loadingContainer).setVisibility(View.VISIBLE);
        btnEntregado.setEnabled(false);

        pedidoController.actualizarEstadoPedido(pedidoId, nuevoEstado, new PedidoController.OperacionCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess() {
                // Ocultar indicador de carga
                findViewById(R.id.loadingContainer).setVisibility(View.GONE);
                Toast.makeText(DetallePedidoActivity.this, "Pedido actualizado a " + nuevoEstado, Toast.LENGTH_SHORT).show();

                // Actualizar el estado del pedido actual
                pedidoActual.setEstado(nuevoEstado);

                // Actualizar la UI
                chipEstado.setText(nuevoEstado);
                configurarChipEstado(nuevoEstado);
                actualizarUI(pedidoActual, obtenerProductosActuales());

                // Notificar cambio (opcional)
                if (recyclerProductos.getAdapter() != null) {
                    recyclerProductos.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String message) {
                // Ocultar indicador de carga
                findViewById(R.id.loadingContainer).setVisibility(View.GONE);
                btnEntregado.setEnabled(true);
                Toast.makeText(DetallePedidoActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Producto> obtenerProductosActuales() {
        if (recyclerProductos.getAdapter() != null) {
            return ((ProductoDetalleAdapter) recyclerProductos.getAdapter()).getProductos();
        }
        return new ArrayList<>();
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