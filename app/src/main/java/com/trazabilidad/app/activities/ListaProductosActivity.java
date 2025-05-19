package com.trazabilidad.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.ProductoAdapter;
import com.trazabilidad.app.controllers.ProductoController;
import com.trazabilidad.app.dialogs.EditarStockDialog;
import com.trazabilidad.app.models.Producto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListaProductosActivity extends AppCompatActivity implements ProductoAdapter.OnProductoClickListener {

    private RecyclerView recyclerViewProductos;
    private ProductoAdapter productoAdapter;
    private List<Producto> listaProductos;
    private ProductoController productoController;
    private EditText editTextBuscar;
    private Spinner spinnerFiltro;
    private Spinner spinnerOrdenamiento;
    private TextView textViewNoProductos;

    private static final int REQUEST_ADD_PRODUCTO = 1;
    private static final int REQUEST_EDIT_PRODUCTO = 2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_productos);

        // Configurar título
        setTitle("Gestión de Productos");

        // Inicializar el controlador
        productoController = new ProductoController(this);

        // Inicializar componentes de UI
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        FloatingActionButton fabAgregarProducto = findViewById(R.id.fabAgregarProducto);
        editTextBuscar = findViewById(R.id.editTextBuscar);
        spinnerFiltro = findViewById(R.id.spinnerFiltro);
        spinnerOrdenamiento = findViewById(R.id.spinnerOrdenamiento);
        textViewNoProductos = findViewById(R.id.textViewNoProductos);

        // Configurar spinner de filtros
        ArrayAdapter<CharSequence> adapterFiltro = ArrayAdapter.createFromResource(this,
                R.array.filtros_productos, android.R.layout.simple_spinner_item);
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapterFiltro);

        // Configurar spinner de ordenamiento
        ArrayAdapter<CharSequence> adapterOrdenamiento = ArrayAdapter.createFromResource(this,
                R.array.ordenamiento_productos, android.R.layout.simple_spinner_item);
        adapterOrdenamiento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrdenamiento.setAdapter(adapterOrdenamiento);

        // Configurar RecyclerView
        listaProductos = new ArrayList<>();
        productoAdapter = new ProductoAdapter(this, listaProductos, this);
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProductos.setAdapter(productoAdapter);

        // Configurar el botón flotante para agregar productos
        fabAgregarProducto.setOnClickListener(v -> agregarNuevoProducto());

        // Configurar búsqueda
        editTextBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                productoAdapter.filtrarPorTexto(s.toString());
                actualizarMensajeNoProductos();
            }
        });

        // Configurar filtro por categoría
        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String categoriaSeleccionada = parent.getItemAtPosition(position).toString();
                productoAdapter.filtrarPorCategoria(categoriaSeleccionada);
                actualizarMensajeNoProductos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Configurar ordenamiento
        spinnerOrdenamiento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String criterioOrdenamiento = parent.getItemAtPosition(position).toString();
                ordenarProductos(criterioOrdenamiento);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Cargar lista de productos
        cargarProductos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_productos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            cargarProductos();
            return true;
        } else if (id == R.id.action_export) {
            exportarListaProductos();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportarListaProductos() {
        // Implementar exportación a CSV/PDF
        Toast.makeText(this, "Función de exportación en desarrollo", Toast.LENGTH_SHORT).show();
    }

    private void ordenarProductos(String criterio) {
        if (listaProductos == null || listaProductos.isEmpty()) {
            return;
        }

        List<Producto> listaOrdenada = new ArrayList<>(listaProductos);

        switch (criterio) {
            case "Más recientes primero":
                Collections.sort(listaOrdenada, (p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
                break;
            case "Más antiguos primero":
                Collections.sort(listaOrdenada, Comparator.comparingInt(Producto::getId));
                break;
            case "Nombre (A-Z)":
                Collections.sort(listaOrdenada, Comparator.comparing(Producto::getNombre));
                break;
            case "Nombre (Z-A)":
                Collections.sort(listaOrdenada, (p1, p2) -> p2.getNombre().compareTo(p1.getNombre()));
                break;
            case "Precio (menor a mayor)":
                Collections.sort(listaOrdenada, Comparator.comparingDouble(Producto::getPrecio));
                break;
            case "Precio (mayor a menor)":
                Collections.sort(listaOrdenada, (p1, p2) -> Double.compare(p2.getPrecio(), p1.getPrecio()));
                break;
            case "Stock (menor a mayor)":
                Collections.sort(listaOrdenada, Comparator.comparingInt(Producto::getCantidad));
                break;
            case "Stock (mayor a menor)":
                Collections.sort(listaOrdenada, (p1, p2) -> Integer.compare(p2.getCantidad(), p1.getCantidad()));
                break;
        }

        productoAdapter.actualizarLista(listaOrdenada);

        // Aplicar filtros actuales después de ordenar
        if (spinnerFiltro != null && spinnerFiltro.getSelectedItem() != null) {
            productoAdapter.filtrarPorCategoria(spinnerFiltro.getSelectedItem().toString());
        }

        if (editTextBuscar != null && editTextBuscar.getText() != null) {
            productoAdapter.filtrarPorTexto(editTextBuscar.getText().toString());
        }
    }

    private void actualizarMensajeNoProductos() {
        if (productoAdapter.getItemCount() == 0) {
            textViewNoProductos.setVisibility(View.VISIBLE);

            // Personalizar mensaje según el filtro seleccionado
            if (spinnerFiltro != null && spinnerFiltro.getSelectedItem() != null) {
                String filtro = spinnerFiltro.getSelectedItem().toString();
                switch (filtro) {
                    case "Bajo Stock":
                        textViewNoProductos.setText("No hay productos con stock bajo");
                        break;
                    case "Inactivos":
                        textViewNoProductos.setText("No hay productos inactivos");
                        break;
                    default:
                        textViewNoProductos.setText("No se encontraron productos");
                        break;
                }
            }
        } else {
            textViewNoProductos.setVisibility(View.GONE);
        }
    }

    private void cargarProductos() {
        productoController.obtenerTodosLosProductos(new ProductoController.ProductosCallback() {
            @Override
            public void onSuccess(List<Producto> productos) {
                listaProductos.clear();
                listaProductos.addAll(productos);

                // Aplicar ordenamiento
                if (spinnerOrdenamiento != null && spinnerOrdenamiento.getSelectedItem() != null) {
                    ordenarProductos(spinnerOrdenamiento.getSelectedItem().toString());
                } else {
                    // Ordenamiento por defecto: más reciente primero
                    Collections.sort(productos, (p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
                    productoAdapter.actualizarLista(productos);
                }

                // Mantener el filtro seleccionado
                if (spinnerFiltro != null && spinnerFiltro.getSelectedItem() != null) {
                    productoAdapter.filtrarPorCategoria(spinnerFiltro.getSelectedItem().toString());
                }

                actualizarMensajeNoProductos();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ListaProductosActivity.this, message, Toast.LENGTH_SHORT).show();
                actualizarMensajeNoProductos();
            }
        });
    }

    private void agregarNuevoProducto() {
        Intent intent = new Intent(this, GestionProductoActivity.class);
        startActivityForResult(intent, REQUEST_ADD_PRODUCTO);
    }

    @Override
    public void onProductoClick(Producto producto) {
        editarProducto(producto);
    }

    @Override
    public void onDeleteClick(Producto producto) {
        mostrarDialogoEliminarProducto(producto);
    }

    @Override
    public void onActivarClick(Producto producto) {
        activarProducto(producto);
    }

    @Override
    public void onEditarStockClick(Producto producto) {
        mostrarDialogoEditarStock(producto);
    }

    private void mostrarDialogoEditarStock(Producto producto) {
        EditarStockDialog dialog = new EditarStockDialog(this, producto);
        dialog.setOnStockUpdateListener(new EditarStockDialog.OnStockUpdateListener() {
            @Override
            public void onStockUpdated() {
                cargarProductos();
                Toast.makeText(ListaProductosActivity.this, "Stock actualizado", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void editarProducto(Producto producto) {
        Intent intent = new Intent(this, GestionProductoActivity.class);
        intent.putExtra("producto_id", producto.getId());
        startActivityForResult(intent, REQUEST_EDIT_PRODUCTO);
    }

    private void mostrarDialogoEliminarProducto(final Producto producto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Desactivar Producto");
        builder.setMessage("¿Está seguro que desea desactivar el producto " + producto.getNombre() + "?");
        builder.setPositiveButton("Desactivar", (dialog, which) -> {
            desactivarProducto(producto);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void desactivarProducto(Producto producto) {
        productoController.desactivarProducto(producto.getId(), new ProductoController.OperacionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ListaProductosActivity.this,
                        "Producto desactivado correctamente", Toast.LENGTH_SHORT).show();
                cargarProductos();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ListaProductosActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void activarProducto(Producto producto) {
        productoController.activarProducto(producto.getId(), new ProductoController.OperacionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ListaProductosActivity.this,
                        "Producto activado correctamente", Toast.LENGTH_SHORT).show();
                cargarProductos();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ListaProductosActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_ADD_PRODUCTO || requestCode == REQUEST_EDIT_PRODUCTO) && resultCode == RESULT_OK) {
            cargarProductos();
        }
    }
}