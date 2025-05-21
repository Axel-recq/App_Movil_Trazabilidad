package com.trazabilidad.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.PedidoProductoAdapter;
import com.trazabilidad.app.controllers.PedidoController;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Producto;
import com.trazabilidad.app.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NuevoPedidoActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private static final int REQUEST_SELECCIONAR_PRODUCTOS = 101;
    private static final int REQUEST_EDITAR_CANTIDAD = 102;

    private TextInputLayout tilNumero, tilCliente, tilDireccion, tilObservaciones, tilEstado;
    private TextInputEditText etNumero, etCliente, etDireccion, etObservaciones;
    private AutoCompleteTextView actvEstado;
    private MaterialButton btnRegistrar, btnUbicacion;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private FloatingActionButton fabAgregarProductos;
    private RecyclerView recyclerViewProductos;
    private TextView textViewTotalPedido;
    private TextView textViewNoProductos;

    private FusedLocationProviderClient fusedLocationClient;
    private PedidoController pedidoController;
    private SessionManager sessionManager;
    private PedidoProductoAdapter productoAdapter;
    private List<Producto> productosSeleccionados;

    private double latitud = 0.0;
    private double longitud = 0.0;
    private boolean ubicacionObtenida = false;
    private NumberFormat formatoMoneda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_pedido);

        // Inicializar formato de moneda
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        // Inicializar componentes de la UI
        inicializarUI();
        aplicarAnimaciones();

        // Configurar la toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nuevo Pedido");
        }

        // Inicializar controladores y servicios
        pedidoController = new PedidoController(this);
        sessionManager = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        productosSeleccionados = new ArrayList<>();

        // Configurar los estados disponibles
        configurarEstados();

        // Configurar RecyclerView para productos
        configurarRecyclerViewProductos();

        // Configurar listeners
        configurarListeners();
    }

    private void inicializarUI() {
        toolbar = findViewById(R.id.toolbar);
        tilNumero = findViewById(R.id.tilNumero);
        tilCliente = findViewById(R.id.tilCliente);
        tilDireccion = findViewById(R.id.tilDireccion);
        tilObservaciones = findViewById(R.id.tilObservaciones);
        tilEstado = findViewById(R.id.tilEstado);
        etNumero = findViewById(R.id.etNumero);
        etCliente = findViewById(R.id.etCliente);
        etDireccion = findViewById(R.id.etDireccion);
        etObservaciones = findViewById(R.id.etObservaciones);
        actvEstado = findViewById(R.id.actvEstado);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnUbicacion = findViewById(R.id.btnUbicacion);
        progressBar = findViewById(R.id.progressBar);
        fabAgregarProductos = findViewById(R.id.fabAgregarProductos);
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        textViewTotalPedido = findViewById(R.id.textViewTotalPedido);
        textViewNoProductos = findViewById(R.id.textViewNoProductos);

        // Añadir animación para FloatingActionButton
        fabAgregarProductos.hide();
        fabAgregarProductos.postDelayed(() -> fabAgregarProductos.show(), 500);
    }

    private void aplicarAnimaciones() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(300);
    }

    private void configurarEstados() {
        List<String> estados = Arrays.asList("Pendiente", "En proceso", "Entregado", "Cancelado");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, estados);
        actvEstado.setAdapter(adapter);
        actvEstado.setText(estados.get(0), false); // "Pendiente" por defecto
    }

    private void configurarRecyclerViewProductos() {
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        productoAdapter = new PedidoProductoAdapter(productosSeleccionados, new PedidoProductoAdapter.OnProductoActionListener() {
            @Override
            public void onRemoveProducto(int position) {
                if (position >= 0 && position < productosSeleccionados.size()) {
                    Producto productoEliminado = productosSeleccionados.get(position);
                    productosSeleccionados.remove(position);
                    actualizarListaProductos();

                    // Mostrar mensaje con opción para deshacer
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout),
                            "Producto eliminado", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Deshacer", v -> {
                        productosSeleccionados.add(position, productoEliminado);
                        actualizarListaProductos();
                    });
                    snackbar.show();
                }
            }

            @Override
            public void onEditCantidad(int position, Producto producto) {
                mostrarDialogoCantidad(position, producto);
            }
        });
        recyclerViewProductos.setAdapter(productoAdapter);
        actualizarListaProductos();
    }

    @SuppressLint("SetTextI18n")
    private void mostrarDialogoCantidad(int position, Producto producto) {
        View view = getLayoutInflater().inflate(R.layout.dialog_editar_cantidad, null);
        TextInputLayout tilCantidad = view.findViewById(R.id.tilCantidad);
        TextInputEditText etCantidad = view.findViewById(R.id.etCantidad);
        TextView tvProductoNombre = view.findViewById(R.id.tvProductoNombre);
        TextView tvPrecioUnitario = view.findViewById(R.id.tvPrecioUnitario);

        tvProductoNombre.setText(producto.getNombre());
        tvPrecioUnitario.setText("Precio unitario: " + formatoMoneda.format(producto.getPrecio()));
        etCantidad.setText(String.valueOf(producto.getCantidad()));

        // Calcular y mostrar subtotal mientras el usuario cambia la cantidad
        etCantidad.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int cantidad = Integer.parseInt(s.toString());
                    double subtotal = cantidad * producto.getPrecio();
                    tilCantidad.setHelperText("Subtotal: " + formatoMoneda.format(subtotal));
                } catch (NumberFormatException e) {
                    tilCantidad.setHelperText("");
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Editar cantidad")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (etCantidad.getText() != null && !etCantidad.getText().toString().isEmpty()) {
                        try {
                            int nuevaCantidad = Integer.parseInt(etCantidad.getText().toString());
                            if (nuevaCantidad > 0) {
                                producto.setCantidad(nuevaCantidad);
                                productosSeleccionados.set(position, producto);
                                actualizarListaProductos();

                                // Feedback visual
                                Animation animation = AnimationUtils.loadAnimation(this, R.anim.item_animation_fall_down);
                                recyclerViewProductos.getChildAt(position).startAnimation(animation);
                            } else {
                                mostrarError("La cantidad debe ser mayor a 0");
                            }
                        } catch (NumberFormatException e) {
                            mostrarError("Ingrese un número válido");
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_rounded_bg))
                .show();
    }

    private void configurarListeners() {
        btnRegistrar.setOnClickListener(v -> validarYRegistrarPedido());
        btnUbicacion.setOnClickListener(v -> obtenerUbicacionActual());
        fabAgregarProductos.setOnClickListener(v -> seleccionarProductos());

        // Validaciones en tiempo real
        configurarValidacionesCampos();
    }

    private void configurarValidacionesCampos() {
        etNumero.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && etNumero.getText() != null) {
                String texto = etNumero.getText().toString().trim();
                if (texto.isEmpty()) {
                    tilNumero.setError("El número de pedido es obligatorio");
                } else if (texto.length() < 3) {
                    tilNumero.setError("El número debe tener al menos 3 caracteres");
                } else {
                    tilNumero.setError(null);
                }
            }
        });

        etCliente.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && etCliente.getText() != null) {
                String texto = etCliente.getText().toString().trim();
                if (texto.isEmpty()) {
                    tilCliente.setError("El nombre del cliente es obligatorio");
                } else if (texto.length() < 3) {
                    tilCliente.setError("El nombre debe tener al menos 3 caracteres");
                } else {
                    tilCliente.setError(null);
                }
            }
        });

        etDireccion.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && etDireccion.getText() != null) {
                String texto = etDireccion.getText().toString().trim();
                if (texto.isEmpty()) {
                    tilDireccion.setError("La dirección es obligatoria");
                } else if (texto.length() < 5) {
                    tilDireccion.setError("La dirección debe ser más detallada");
                } else {
                    tilDireccion.setError(null);
                }
            }
        });
    }

    private void seleccionarProductos() {
        Intent intent = new Intent(this, SeleccionProductosActivity.class);

        // Si ya hay productos seleccionados, pasarlos a la actividad
        // para que se muestren como ya seleccionados
        if (!productosSeleccionados.isEmpty()) {
            ArrayList<Producto> productosActuales = new ArrayList<>(productosSeleccionados);
            intent.putExtra("productos_actuales", productosActuales);
        }

        startActivityForResult(intent, REQUEST_SELECCIONAR_PRODUCTOS);

        // Añadir animación de transición
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECCIONAR_PRODUCTOS && resultCode == RESULT_OK && data != null) {
            ArrayList<Producto> nuevosProductos = data.getParcelableArrayListExtra("productos_seleccionados");

            if (nuevosProductos != null && !nuevosProductos.isEmpty()) {
                boolean seProcesaronProductos = false;

                // Para cada producto nuevo, verificar si ya está en la lista
                for (Producto nuevoProducto : nuevosProductos) {
                    boolean existeProducto = false;
                    int posicionExistente = -1;

                    // Buscar si ya existe el producto por ID
                    for (int i = 0; i < productosSeleccionados.size(); i++) {
                        Producto productoExistente = productosSeleccionados.get(i);
                        if (productoExistente.getId() == nuevoProducto.getId()) {
                            existeProducto = true;
                            posicionExistente = i;
                            break;
                        }
                    }

                    // Manejar producto según si ya existe o no
                    if (existeProducto) {
                        // Mostrar diálogo para agregar unidades adicionales
                        mostrarDialogoProductoExistente(posicionExistente, nuevoProducto);
                    } else {
                        // Si no existe, añadirlo con cantidad 1 por defecto
                        nuevoProducto.setCantidad(1);
                        productosSeleccionados.add(nuevoProducto);
                        seProcesaronProductos = true;
                    }
                }

                if (seProcesaronProductos) {
                    actualizarListaProductos();
                    mostrarSnackbar("Productos agregados al pedido");
                }
            }
        }
    }

    private void mostrarDialogoProductoExistente(int posicion, Producto nuevoProducto) {
        Producto productoExistente = productosSeleccionados.get(posicion);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Producto ya agregado")
                .setMessage("¿Desea sumar una unidad más del producto '" + productoExistente.getNombre() + "' (Actual: " + productoExistente.getCantidad() + ")?")
                .setPositiveButton("Agregar unidad", (dialog, which) -> {
                    // Incrementar la cantidad del producto existente
                    productoExistente.setCantidad(productoExistente.getCantidad() + 1);
                    productosSeleccionados.set(posicion, productoExistente);
                    actualizarListaProductos();

                    // Destacar visualmente el producto actualizado
                    recyclerViewProductos.scrollToPosition(posicion);
                    View viewItem = recyclerViewProductos.getLayoutManager().findViewByPosition(posicion);
                    if (viewItem != null) {
                        Animation animation = AnimationUtils.loadAnimation(this, R.anim.item_animation_fall_down);
                        viewItem.startAnimation(animation);
                    }
                })
                .setNegativeButton("Editar cantidad", (dialog, which) -> {
                    mostrarDialogoCantidad(posicion, productoExistente);
                })
                .setNeutralButton("Cancelar", null)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_rounded_bg))
                .show();
    }

    @SuppressLint("SetTextI18n")
    private void actualizarListaProductos() {
        productoAdapter.actualizarProductos(productosSeleccionados);

        if (productosSeleccionados.isEmpty()) {
            textViewNoProductos.setVisibility(View.VISIBLE);
            recyclerViewProductos.setVisibility(View.GONE);
            textViewTotalPedido.setVisibility(View.GONE);
        } else {
            textViewNoProductos.setVisibility(View.GONE);
            recyclerViewProductos.setVisibility(View.VISIBLE);
            textViewTotalPedido.setVisibility(View.VISIBLE);

            // Actualizar total
            double total = productoAdapter.calcularTotal();
            textViewTotalPedido.setText("Total del pedido: " + formatoMoneda.format(total));

            // Animar el cambio del total
            Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
            textViewTotalPedido.startAnimation(bounceAnim);
        }

        // Actualizar visibilidad del fab según contenido
        if (productosSeleccionados.isEmpty()) {
            // Cambiar ícono a agregar
            fabAgregarProductos.setImageResource(R.drawable.ic_add);
        } else {
            // Cambiar ícono a editar lista
            fabAgregarProductos.setImageResource(R.drawable.ic_edit);
        }
    }

    private void validarYRegistrarPedido() {
        tilNumero.setError(null);
        tilCliente.setError(null);
        tilDireccion.setError(null);

        String numero = etNumero.getText().toString().trim();
        String cliente = etCliente.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String observaciones = etObservaciones.getText().toString().trim();
        String estado = actvEstado.getText().toString().trim();

        boolean isValid = true;

        if (numero.isEmpty()) {
            tilNumero.setError("El número de pedido es obligatorio");
            tilNumero.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            isValid = false;
        }

        if (cliente.isEmpty()) {
            tilCliente.setError("El nombre del cliente es obligatorio");
            tilCliente.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            isValid = false;
        }

        if (direccion.isEmpty()) {
            tilDireccion.setError("La dirección es obligatoria");
            tilDireccion.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            isValid = false;
        }

        if (!isValid) {
            Snackbar.make(findViewById(android.R.id.content),
                            "Por favor, completa todos los campos obligatorios",
                            Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.error, getTheme()))
                    .setTextColor(getResources().getColor(android.R.color.white, getTheme()))
                    .show();
            return;
        }

        if (productosSeleccionados.isEmpty()) {
            mostrarDialogoConfirmacionSinProductos(numero, cliente, direccion, observaciones, estado);
        } else {
            registrarPedidoConProductos(numero, cliente, direccion, observaciones, estado);
        }
    }

    private void mostrarDialogoConfirmacionSinProductos(String numero, String cliente, String direccion, String observaciones, String estado) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Advertencia")
                .setMessage("Está a punto de crear un pedido sin productos. ¿Desea continuar o agregar productos al pedido?")
                .setPositiveButton("Continuar sin productos", (dialog, which) -> {
                    // Crear pedido sin productos
                    Pedido pedido = crearPedido(numero, cliente, direccion, observaciones, estado);
                    registrarPedido(pedido, new ArrayList<>());
                })
                .setNegativeButton("Agregar productos", (dialog, which) -> seleccionarProductos())
                .setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_rounded_bg))
                .show();
    }

    private void registrarPedidoConProductos(String numero, String cliente, String direccion, String observaciones, String estado) {
        // Crear una vista personalizada para el diálogo de confirmación
        View viewConfirmacion = getLayoutInflater().inflate(R.layout.dialog_confirmar_pedido, null);
        TextView tvCliente = viewConfirmacion.findViewById(R.id.tvCliente);
        TextView tvDireccion = viewConfirmacion.findViewById(R.id.tvDireccion);
        TextView tvCantidadProductos = viewConfirmacion.findViewById(R.id.tvCantidadProductos);
        TextView tvTotalPedido = viewConfirmacion.findViewById(R.id.tvTotalPedido);

        tvCliente.setText(cliente);
        tvDireccion.setText(direccion);
        tvCantidadProductos.setText(String.valueOf(productosSeleccionados.size()));
        tvTotalPedido.setText(formatoMoneda.format(productoAdapter.calcularTotal()));

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar pedido")
                .setView(viewConfirmacion)
                .setIcon(R.drawable.ic_check)
                .setPositiveButton("Registrar", (dialog, which) -> {
                    Pedido pedido = crearPedido(numero, cliente, direccion, observaciones, estado);
                    registrarPedido(pedido, productosSeleccionados);
                })
                .setNegativeButton("Cancelar", null)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_rounded_bg))
                .show();
    }

    private Pedido crearPedido(String numero, String cliente, String direccion, String observaciones, String estado) {
        Pedido pedido = new Pedido();
        pedido.setNumero(numero);
        pedido.setCliente(cliente);
        pedido.setDireccion(direccion);
        pedido.setObservaciones(observaciones);
        pedido.setEstado(estado);
        pedido.setFecha(new Date());
        pedido.setUsuarioId(sessionManager.getUsuarioDetails().getId());
        pedido.setTotal(productoAdapter.calcularTotal());

        if (ubicacionObtenida) {
            pedido.setLatitud(latitud);
            pedido.setLongitud(longitud);
        }

        return pedido;
    }

    private void registrarPedido(Pedido pedido, List<Producto> productos) {
        mostrarProgreso(true);

        pedidoController.registrarPedido(pedido, productos, new PedidoController.OperacionCallback() {
            @Override
            public void onSuccess() {
                mostrarProgreso(false);

                // Mostrar diálogo de éxito con animación
                View viewExito = getLayoutInflater().inflate(R.layout.dialog_pedido_exitoso, null);

                new MaterialAlertDialogBuilder(NuevoPedidoActivity.this)
                        .setView(viewExito)
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", (dialogInterface, i) -> {
                            setResult(RESULT_OK);
                            finish();
                            // Animar salida
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        })
                        .show();
            }

            @Override
            public void onError(String message) {
                mostrarProgreso(false);
                if (message.contains("stock insuficiente")) {
                    mostrarError("No hay suficiente stock para uno o más productos. Por favor, revisa las cantidades.");
                } else {
                    mostrarError("Error al registrar el pedido: " + message);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Permiso de ubicación")
                    .setMessage("Para registrar la ubicación del pedido necesitamos acceder a la ubicación del dispositivo.")
                    .setPositiveButton("Permitir", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION_PERMISSION);
                    })
                    .setNegativeButton("Cancelar", null)
                    .setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_rounded_bg))
                    .show();
            return;
        }

        mostrarProgreso(true);
        btnUbicacion.setText("Obteniendo ubicación...");
        btnUbicacion.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_loading));
        btnUbicacion.setEnabled(false);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    mostrarProgreso(false);
                    btnUbicacion.setEnabled(true);

                    if (location != null) {
                        latitud = location.getLatitude();
                        longitud = location.getLongitude();
                        ubicacionObtenida = true;

                        btnUbicacion.setText("Ubicación obtenida ✓");
                        btnUbicacion.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map));
                        btnUbicacion.setIconTint(ContextCompat.getColorStateList(this, R.color.success));
                        mostrarSnackbar("Ubicación obtenida correctamente");
                    } else {
                        btnUbicacion.setText("Obtener ubicación");
                        btnUbicacion.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map));
                        mostrarError("No se pudo obtener la ubicación. Intente nuevamente.");
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    btnUbicacion.setText("Obtener ubicación");
                    btnUbicacion.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map));
                    btnUbicacion.setEnabled(true);
                    mostrarError("Error al obtener ubicación: " + e.getMessage());
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Si hay datos ingresados, mostrar diálogo de confirmación
            if (hayDatosIngresados()) {
                mostrarDialogoConfirmacionSalir();
            } else {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hayDatosIngresados() {
        return (etNumero.getText() != null && !etNumero.getText().toString().trim().isEmpty()) ||
                (etCliente.getText() != null && !etCliente.getText().toString().trim().isEmpty()) ||
                (etDireccion.getText() != null && !etDireccion.getText().toString().trim().isEmpty()) ||
                (etObservaciones.getText() != null && !etObservaciones.getText().toString().trim().isEmpty()) ||
                !productosSeleccionados.isEmpty();
    }

    private void mostrarDialogoConfirmacionSalir() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Desea salir?")
                .setMessage("Tiene información sin guardar. ¿Está seguro que desea salir sin registrar el pedido?")
                .setPositiveButton("Salir", (dialog, which) -> {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                })
                .setNegativeButton("Cancelar", null)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_rounded_bg))
                .show();
    }

    @Override
    public void onBackPressed() {
        if (hayDatosIngresados()) {
            mostrarDialogoConfirmacionSalir();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                mostrarError("Se requiere permiso de ubicación para esta función");
            }
        }
    }

    private void mostrarProgreso(boolean mostrar) {
        if (mostrar) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegistrar.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegistrar.setEnabled(true);
        }
    }

    private void mostrarError(String mensaje) {
        Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.error, getTheme()))
                .setTextColor(getResources().getColor(android.R.color.white, getTheme()))
                .show();
    }

    private void mostrarSnackbar(String mensaje) {
        Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimary, getTheme()))
                .setTextColor(getResources().getColor(android.R.color.white, getTheme()))
                .show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar estado de ubicación y productos seleccionados
        outState.putDouble("latitud", latitud);
        outState.putDouble("longitud", longitud);
        outState.putBoolean("ubicacionObtenida", ubicacionObtenida);
        outState.putParcelableArrayList("productosSeleccionados", new ArrayList<>(productosSeleccionados));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restaurar estado de ubicación y productos seleccionados
        latitud = savedInstanceState.getDouble("latitud", 0.0);
        longitud = savedInstanceState.getDouble("longitud", 0.0);
        ubicacionObtenida = savedInstanceState.getBoolean("ubicacionObtenida", false);

        if (ubicacionObtenida) {
            btnUbicacion.setText("Ubicación obtenida ✓");
            btnUbicacion.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map));
            btnUbicacion.setIconTint(ContextCompat.getColorStateList(this, R.color.success));
        }

        ArrayList<Producto> productos = savedInstanceState.getParcelableArrayList("productosSeleccionados");
        if (productos != null && !productos.isEmpty()) {
            productosSeleccionados.clear();
            productosSeleccionados.addAll(productos);
            actualizarListaProductos();
        }
    }
}