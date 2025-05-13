package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.ProductoAdapter;
import com.trazabilidad.app.controllers.ProductoController;
import com.trazabilidad.app.models.Producto;

import java.util.ArrayList;
import java.util.List;

public class ListaProductosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProductos;
    private ProductoAdapter productoAdapter;
    private List<Producto> listaProductos;
    private ProductoController productoController;
    private int pedidoId;
    private static final int REQUEST_ADD_PRODUCTO = 1;
    private static final int REQUEST_EDIT_PRODUCTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_productos);

        // Obtener el ID del pedido de los extras
        if (getIntent().hasExtra("pedido_id")) {
            pedidoId = getIntent().getIntExtra("pedido_id", -1);
        } else {
            Toast.makeText(this, "Error: ID de pedido no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar título
        setTitle("Productos del Pedido #" + pedidoId);

        // Inicializar el controlador
        productoController = new ProductoController(this);

        // Inicializar componentes de UI
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        FloatingActionButton fabAgregarProducto = findViewById(R.id.fabAgregarProducto);

        // Configurar RecyclerView
        listaProductos = new ArrayList<>();
        productoAdapter = new ProductoAdapter(listaProductos, new ProductoAdapter.OnProductoClickListener() {
            @Override
            public void onProductoClick(Producto producto) {
                editarProducto(producto);
            }

            @Override
            public void onDeleteClick(Producto producto) {
                eliminarProducto(producto);
            }
        });
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProductos.setAdapter(productoAdapter);

        // Configurar el botón flotante para agregar productos
        fabAgregarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarNuevoProducto();
            }
        });

        // Cargar lista de productos
        cargarProductos();
    }

    private void cargarProductos() {
        if (pedidoId <= 0) {
            Toast.makeText(this, "ID de pedido inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        productoController.obtenerProductosPorPedido(pedidoId, new ProductoController.ProductosCallback() {
            @Override
            public void onSuccess(List<Producto> productos) {
                listaProductos.clear();
                listaProductos.addAll(productos);
                productoAdapter.notifyDataSetChanged();

                // Mostrar mensaje si no hay productos
                if (productos.isEmpty()) {
                    findViewById(R.id.textViewNoProductos).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.textViewNoProductos).setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ListaProductosActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarNuevoProducto() {
        Intent intent = new Intent(this, GestionProductoActivity.class);
        intent.putExtra("pedido_id", pedidoId);
        startActivityForResult(intent, REQUEST_ADD_PRODUCTO);
    }

    private void editarProducto(Producto producto) {
        Intent intent = new Intent(this, GestionProductoActivity.class);
        intent.putExtra("producto_id", producto.getId());
        intent.putExtra("pedido_id", pedidoId);
        startActivityForResult(intent, REQUEST_EDIT_PRODUCTO);
    }

    private void eliminarProducto(final Producto producto) {
        // Implementación de un diálogo de confirmación
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Eliminar Producto");
        builder.setMessage("¿Está seguro que desea eliminar el producto " + producto.getNombre() + "?");
        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            productoController.eliminarProducto(producto.getId(), new ProductoController.OperacionCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ListaProductosActivity.this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                    cargarProductos();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(ListaProductosActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_ADD_PRODUCTO || requestCode == REQUEST_EDIT_PRODUCTO) && resultCode == RESULT_OK) {
            cargarProductos();
        }
    }
}