package com.trazabilidad.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.DevolucionProductoAdapter;
import com.trazabilidad.app.controllers.DevolucionController;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.database.ProductoDAO;
import com.trazabilidad.app.models.Devolucion;
import com.trazabilidad.app.models.DevolucionItem;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Producto;
import com.trazabilidad.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GestionDevolucionActivity extends AppCompatActivity {
    private static final String TAG = "GestionDevolucionActivity";
    private TextView textViewCodigoPedido, textViewNombrePedido, textViewFechaEntrega, textViewDiasRestantes;
    private RecyclerView recyclerViewProductos;
    private Button buttonRegistrar;
    private DevolucionProductoAdapter adapter;
    private DevolucionController devolucionController;
    private PedidoDAO pedidoDAO;
    private ProductoDAO productoDAO;
    private SessionManager sessionManager;
    private int usuarioId;
    private int pedidoId;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_devolucion);

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, R.string.sesion_expirada, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Obtener usuarioId desde la sesión
        usuarioId = sessionManager.getUsuarioDetails().getId();
        if (usuarioId == 0) {
            Toast.makeText(this, R.string.error_usuario_id, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inicializar vistas
        textViewCodigoPedido = findViewById(R.id.textViewCodigoPedido);
        textViewNombrePedido = findViewById(R.id.textViewNombrePedido);
        textViewFechaEntrega = findViewById(R.id.textViewFechaEntrega);
        textViewDiasRestantes = findViewById(R.id.textViewDiasRestantes);
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);

        // Inicializar controladores y DAOs
        devolucionController = new DevolucionController(this);
        pedidoDAO = new PedidoDAO(this);
        productoDAO = new ProductoDAO(this);

        // Obtener pedidoId desde Intent
        Intent intent = getIntent();
        pedidoId = intent.getIntExtra("pedidoId", 0);
        if (pedidoId == 0) {
            Toast.makeText(this, R.string.error_pedido_id_invalido, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar detalles del pedido
        cargarDetallesPedido();

        // Configurar RecyclerView
        configurarRecyclerView();

        // Configurar botón de registro
        buttonRegistrar.setOnClickListener(v -> registrarDevoluciones());
    }

    private void cargarDetallesPedido() {
        Pedido pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
        if (pedido == null) {
            Toast.makeText(this, R.string.error_pedido_no_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Validar estado del pedido
        if (!"ENTREGADO".equalsIgnoreCase(pedido.getEstado())) {
            Toast.makeText(this, R.string.error_solo_pedidos_entregados, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Validar período de devolución (7 días desde horaEntrega)
        Long horaEntrega = pedido.getHoraEntrega();
        if (horaEntrega == null || horaEntrega == 0L) {
            Log.e(TAG, "horaEntrega es null o 0 para pedidoId: " + pedidoId);
            Toast.makeText(this, R.string.error_fecha_entrega_no_registrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        long diffInMillies = new Date().getTime() - horaEntrega;
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        if (diffInDays > 7) {
            Toast.makeText(this, R.string.error_periodo_devolucion_expirado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar información del pedido
        textViewCodigoPedido.setText(getString(R.string.pedido_numero, pedido.getNumero()));
        textViewNombrePedido.setText(getString(R.string.cliente, pedido.getCliente()));
        textViewFechaEntrega.setText(getString(R.string.fecha_entrega,
                pedido.getHoraEntrega() != null && pedido.getHoraEntrega() != 0
                        ? sdf.format(new Date(pedido.getHoraEntrega()))
                        : "No registrada"));
        textViewDiasRestantes.setText(getString(R.string.dias_restantes, 7));
    }

    private void configurarRecyclerView() {
        List<Producto> productos = productoDAO.obtenerProductosPorPedido(pedidoId);
        List<DevolucionItem> items = new ArrayList<>();
        for (Producto p : productos) {
            items.add(new DevolucionItem(p));
        }
        adapter = new DevolucionProductoAdapter(this, items);
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProductos.setAdapter(adapter);
    }

    @SuppressLint("StringFormatInvalid")
    private void registrarDevoluciones() {
        List<DevolucionItem> selectedItems = new ArrayList<>();
        for (DevolucionItem item : adapter.getItems()) {
            if (item.isSelected()) {
                if (item.getCantidad() <= 0) {
                    Toast.makeText(this, getString(R.string.error_cantidad_requerida, item.getProducto().getNombre()), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (item.getMotivo().trim().isEmpty()) {
                    Toast.makeText(this, getString(R.string.error_motivo_requerido, item.getProducto().getNombre()), Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, R.string.error_seleccionar_producto, Toast.LENGTH_SHORT).show();
            return;
        }

        // Registrar cada devolución
        int[] devolucionesCompletadas = {0};
        for (DevolucionItem item : selectedItems) {
            Devolucion devolucion = new Devolucion();
            devolucion.setPedidoId(pedidoId);
            devolucion.setProductoId(item.getProducto().getId());
            devolucion.setCantidad(item.getCantidad());
            devolucion.setMotivo(item.getMotivo());
            devolucion.setFecha(new Date());
            devolucion.setUsuarioId(usuarioId);

            devolucionController.registrarDevolucion(devolucion, new DevolucionController.OperacionCallback() {
                @Override
                public void onSuccess() {
                    devolucionesCompletadas[0]++;
                    if (devolucionesCompletadas[0] == selectedItems.size()) {
                        Toast.makeText(GestionDevolucionActivity.this, R.string.devoluciones_registradas, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(GestionDevolucionActivity.this, getString(R.string.error_registrar_devolucion, message), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}