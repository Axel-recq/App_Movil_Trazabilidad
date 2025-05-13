package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.SeleccionProductoAdapter;
import com.trazabilidad.app.controllers.ProductoController;
import com.trazabilidad.app.models.Producto;

import java.util.ArrayList;
import java.util.List;

public class SeleccionProductosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProductos;
    private SeleccionProductoAdapter productoAdapter;
    private List<Producto> listaProductos;
    private List<Producto> productosSeleccionados;
    private ProductoController productoController;
    private SearchView searchViewProductos;
    private MaterialButton btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_productos);
        searchViewProductos = findViewById(R.id.searchViewProductos);
        // Configurar título y botón de retroceso
        setTitle("Seleccionar Productos");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar el controlador
        productoController = new ProductoController(this);

        // Inicializar componentes de UI
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        searchViewProductos = findViewById(R.id.searchViewProductos);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        // Inicializar listas
        listaProductos = new ArrayList<>();
        productosSeleccionados = new ArrayList<>();

        // Configurar RecyclerView
        productoAdapter = new SeleccionProductoAdapter(listaProductos, new SeleccionProductoAdapter.OnProductoSeleccionadoListener() {
            @Override
            public void onProductoSeleccionado(Producto producto, boolean isChecked) {
                if (isChecked) {
                    if (!productosSeleccionados.contains(producto)) {
                        productosSeleccionados.add(producto);
                    }
                } else {
                    productosSeleccionados.remove(producto);
                }

                actualizarEstadoBotonConfirmar();
            }
        });
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProductos.setAdapter(productoAdapter);

        // Configurar búsqueda
        searchViewProductos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarProductos(newText);
                return true;
            }
        });

        // Configurar botón de confirmar
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarSeleccion();
            }
        });

        // Cargar todos los productos disponibles
        cargarProductos();

        // Inicializar estado del botón
        actualizarEstadoBotonConfirmar();
    }

    private void cargarProductos() {
        productoController.obtenerTodosLosProductos(new ProductoController.ProductosCallback() {
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
                Toast.makeText(SeleccionProductosActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarProductos(String texto) {
        productoController.obtenerTodosLosProductos(new ProductoController.ProductosCallback() {
            @Override
            public void onSuccess(List<Producto> productos) {
                List<Producto> listaFiltrada = new ArrayList<>();

                for (Producto producto : productos) {
                    if (producto.getNombre().toLowerCase().contains(texto.toLowerCase()) ||
                            producto.getCodigo().toLowerCase().contains(texto.toLowerCase())) {
                        listaFiltrada.add(producto);
                    }
                }

                listaProductos.clear();
                listaProductos.addAll(listaFiltrada);
                productoAdapter.notifyDataSetChanged();

                // Mantener las selecciones previas
                for (Producto productoSeleccionado : productosSeleccionados) {
                    productoAdapter.setProductoSeleccionado(productoSeleccionado);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SeleccionProductosActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarEstadoBotonConfirmar() {
        btnConfirmar.setEnabled(!productosSeleccionados.isEmpty());

        if (productosSeleccionados.isEmpty()) {
            btnConfirmar.setText("Confirmar");
        } else {
            btnConfirmar.setText("Confirmar (" + productosSeleccionados.size() + ")");
        }
    }

    private void confirmarSeleccion() {
        if (productosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un producto", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        ArrayList<Producto> productosParaEnviar = new ArrayList<>(productosSeleccionados);
        resultIntent.putExtra("productos_seleccionados", productosParaEnviar);
        setResult(RESULT_OK, resultIntent);
        finish();
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