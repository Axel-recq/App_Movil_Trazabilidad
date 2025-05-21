package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GestionDevolucionActivity extends AppCompatActivity {
    private TextView textViewCodigoPedido, textViewNombrePedido;
    private RecyclerView recyclerViewProductos;
    private Button buttonRegistrar;
    private DevolucionProductoAdapter adapter;
    private DevolucionController devolucionController;
    private PedidoDAO pedidoDAO;
    private ProductoDAO productoDAO;
    private SessionManager sessionManager;
    private int usuarioId;
    private int pedidoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_devolucion);

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Sesión expirada. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Obtener usuarioId desde la sesión
        usuarioId = sessionManager.getUsuarioDetails().getId();
        if (usuarioId == 0) {
            Toast.makeText(this, "Error: No se pudo obtener el ID del usuario.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inicializar vistas
        textViewCodigoPedido = findViewById(R.id.textViewCodigoPedido);
        textViewNombrePedido = findViewById(R.id.textViewNombrePedido);
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
            Toast.makeText(this, "ID de pedido inválido", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "El pedido no existe.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Validar estado del pedido
        if (!"ENTREGADO".equalsIgnoreCase(pedido.getEstado())) {
            Toast.makeText(this, "Solo se pueden devolver productos de pedidos entregados.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Validar período de devolución (7 días desde horaEntrega)
        if (pedido.getHoraEntrega() == 0) {
            Toast.makeText(this, "El pedido no tiene una fecha de entrega registrada.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        long diffInMillies = new Date().getTime() - pedido.getHoraEntrega();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        if (diffInDays > 7) {
            Toast.makeText(this, "El período de devolución (7 días) ha expirado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar código y nombre del pedido
        textViewCodigoPedido.setText("Código del Pedido: " + pedido.getNumero());
        textViewNombrePedido.setText("Nombre del Pedido: " + pedido.getCliente());
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

    private void registrarDevoluciones() {
        List<DevolucionItem> selectedItems = new ArrayList<>();
        for (DevolucionItem item : adapter.getItems()) {
            if (item.isSelected()) {
                if (item.getCantidad() <= 0) {
                    Toast.makeText(this, "La cantidad debe ser mayor que cero para " + item.getProducto().getNombre(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (item.getMotivo().trim().isEmpty()) {
                    Toast.makeText(this, "El motivo es requerido para " + item.getProducto().getNombre(), Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un producto para devolver.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Registrar cada devolución
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
                    // Se maneja el éxito en el bucle completo
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(GestionDevolucionActivity.this, "Error al registrar devolución: " + message, Toast.LENGTH_LONG).show();
                }
            });
        }

        Toast.makeText(this, "Devoluciones registradas correctamente", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}