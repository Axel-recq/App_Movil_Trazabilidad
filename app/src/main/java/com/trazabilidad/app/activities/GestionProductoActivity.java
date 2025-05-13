package com.trazabilidad.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.ProductoController;
import com.trazabilidad.app.models.Producto;

public class GestionProductoActivity extends AppCompatActivity {

    private EditText editTextCodigo;
    private EditText editTextNombre;
    private EditText editTextDescripcion;
    private EditText editTextPrecio;
    private EditText editTextCantidad;
    private Button buttonGuardar;

    private ProductoController productoController;
    private int productoId = -1;
    private int pedidoId = -1;
    private boolean modoEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_producto);

        // Inicializar controlador
        productoController = new ProductoController(this);

        // Inicializar vistas
        editTextCodigo = findViewById(R.id.editTextCodigo);
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        editTextPrecio = findViewById(R.id.editTextPrecio);
        editTextCantidad = findViewById(R.id.editTextCantidad);
        buttonGuardar = findViewById(R.id.buttonGuardar);

        // Obtener datos del intent
        if (getIntent().hasExtra("pedido_id")) {
            pedidoId = getIntent().getIntExtra("pedido_id", -1);
        } else {
            Toast.makeText(this, "Error: ID de pedido no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getIntent().hasExtra("producto_id")) {
            productoId = getIntent().getIntExtra("producto_id", -1);
            modoEdicion = true;
            cargarDatosProducto(productoId);
            setTitle("Editar Producto");
        } else {
            setTitle("Nuevo Producto");
        }

        // Habilitar botón de retroceso en ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Configurar botón de guardar
        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarProducto();
            }
        });
    }

    private void cargarDatosProducto(int idProducto) {
        productoController.obtenerProductoPorId(idProducto, new ProductoController.ProductoCallback() {
            @Override
            public void onSuccess(Producto producto) {
                editTextCodigo.setText(producto.getCodigo());
                editTextNombre.setText(producto.getNombre());
                editTextDescripcion.setText(producto.getDescripcion());
                editTextPrecio.setText(String.valueOf(producto.getPrecio()));
                editTextCantidad.setText(String.valueOf(producto.getCantidad()));
                pedidoId = producto.getPedidoId();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(GestionProductoActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void guardarProducto() {
        // Validar datos
        if (!validarFormulario()) {
            return;
        }

        // Crear objeto producto con los datos del formulario
        final Producto producto = new Producto();
        if (modoEdicion) {
            producto.setId(productoId);
        }
        producto.setCodigo(editTextCodigo.getText().toString().trim());
        producto.setNombre(editTextNombre.getText().toString().trim());
        producto.setDescripcion(editTextDescripcion.getText().toString().trim());

        String precioStr = editTextPrecio.getText().toString().trim();
        double precio = 0;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        producto.setPrecio(precio);

        String cantidadStr = editTextCantidad.getText().toString().trim();
        int cantidad = 0;
        try {
            cantidad = Integer.parseInt(cantidadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return;
        }
        producto.setCantidad(cantidad);

        producto.setPedidoId(pedidoId);

        // Guardar producto según el modo (nuevo o edición)
        if (modoEdicion) {
            productoController.actualizarProducto(producto, new ProductoController.OperacionCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(GestionProductoActivity.this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(GestionProductoActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            productoController.insertarProducto(producto, new ProductoController.OperacionCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(GestionProductoActivity.this, "Producto agregado correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(GestionProductoActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validarFormulario() {
        boolean valido = true;

        // Validar código
        if (TextUtils.isEmpty(editTextCodigo.getText())) {
            editTextCodigo.setError("El código es requerido");
            valido = false;
        }

        // Validar nombre
        if (TextUtils.isEmpty(editTextNombre.getText())) {
            editTextNombre.setError("El nombre es requerido");
            valido = false;
        }

        // Validar precio
        String precioStr = editTextPrecio.getText().toString().trim();
        if (TextUtils.isEmpty(precioStr)) {
            editTextPrecio.setError("El precio es requerido");
            valido = false;
        } else {
            try {
                double precio = Double.parseDouble(precioStr);
                if (precio <= 0) {
                    editTextPrecio.setError("El precio debe ser mayor que cero");
                    valido = false;
                }
            } catch (NumberFormatException e) {
                editTextPrecio.setError("Precio inválido");
                valido = false;
            }
        }

        // Validar cantidad
        String cantidadStr = editTextCantidad.getText().toString().trim();
        if (TextUtils.isEmpty(cantidadStr)) {
            editTextCantidad.setError("La cantidad es requerida");
            valido = false;
        } else {
            try {
                int cantidad = Integer.parseInt(cantidadStr);
                if (cantidad < 0) {
                    editTextCantidad.setError("La cantidad no puede ser negativa");
                    valido = false;
                }
            } catch (NumberFormatException e) {
                editTextCantidad.setError("Cantidad inválida");
                valido = false;
            }
        }

        return valido;
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