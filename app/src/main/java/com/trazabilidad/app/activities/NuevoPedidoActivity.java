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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.PedidoController;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.utils.SessionManager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NuevoPedidoActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 100;

    private TextInputLayout tilNumero, tilCliente, tilDireccion, tilObservaciones, tilEstado;
    private TextInputEditText etNumero, etCliente, etDireccion, etObservaciones;
    private AutoCompleteTextView actvEstado;
    private MaterialButton btnRegistrar, btnUbicacion;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private Chip chipNuevoPedido;

    private FusedLocationProviderClient fusedLocationClient;
    private PedidoController pedidoController;
    private SessionManager sessionManager;

    private double latitud = 0.0;
    private double longitud = 0.0;
    private boolean ubicacionObtenida = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_pedido);

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

        // Configurar los estados disponibles
        configurarEstados();

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
        chipNuevoPedido = findViewById(R.id.chipNuevoPedido);
    }

    private void aplicarAnimaciones() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(300);
        chipNuevoPedido.startAnimation(fadeIn);
    }

    private void configurarEstados() {
        List<String> estados = Arrays.asList("Pendiente", "En proceso", "Entregado", "Cancelado");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, estados);
        actvEstado.setAdapter(adapter);
        actvEstado.setText(estados.get(0), false); // "Pendiente" por defecto
    }

    private void configurarListeners() {
        btnRegistrar.setOnClickListener(v -> validarYRegistrarPedido());
        btnUbicacion.setOnClickListener(v -> obtenerUbicacionActual());

        etNumero.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && etNumero.getText() != null) {
                if (etNumero.getText().toString().trim().isEmpty()) {
                    tilNumero.setError("El número de pedido es obligatorio");
                } else {
                    tilNumero.setError(null);
                }
            }
        });

        etCliente.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && etCliente.getText() != null) {
                if (etCliente.getText().toString().trim().isEmpty()) {
                    tilCliente.setError("El nombre del cliente es obligatorio");
                } else {
                    tilCliente.setError(null);
                }
            }
        });

        etDireccion.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && etDireccion.getText() != null) {
                if (etDireccion.getText().toString().trim().isEmpty()) {
                    tilDireccion.setError("La dirección es obligatoria");
                } else {
                    tilDireccion.setError(null);
                }
            }
        });
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

        Pedido pedido = new Pedido();
        pedido.setNumero(numero);
        pedido.setCliente(cliente);
        pedido.setDireccion(direccion);
        pedido.setObservaciones(observaciones);
        pedido.setEstado(estado);
        pedido.setFecha(new Date());
        pedido.setUsuarioId(sessionManager.getUsuarioDetails().getId());

        if (ubicacionObtenida) {
            pedido.setLatitud(latitud);
            pedido.setLongitud(longitud);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar registro")
                .setMessage("¿Está seguro de registrar este pedido?")
                .setIcon(R.drawable.ic_map)
                .setPositiveButton("Registrar", (dialog, which) -> registrarPedido(pedido))
                .setNegativeButton("Cancelar", null)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_rounded_bg))
                .show();
    }

    private void registrarPedido(Pedido pedido) {
        mostrarProgreso(true);

        pedidoController.registrarPedido(pedido, new PedidoController.OperacionCallback() {
            @Override
            public void onSuccess() {
                mostrarProgreso(false);
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Pedido registrado correctamente", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(getResources().getColor(R.color.success, getTheme()));
                snackbar.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
                snackbar.setAction("OK", v -> {
                    setResult(RESULT_OK);
                    finish();
                });
                snackbar.show();

                new android.os.Handler().postDelayed(() -> {
                    setResult(RESULT_OK);
                    finish();
                }, 2000);
            }

            @Override
            public void onError(String message) {
                mostrarProgreso(false);
                mostrarError("Error: " + message);
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
        btnUbicacion.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map));
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
                        mostrarError("No se pudo obtener la ubicación. Intente nuevamente.");
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    btnUbicacion.setText("Obtener ubicación");
                    btnUbicacion.setEnabled(true);
                    mostrarError("Error al obtener ubicación: " + e.getMessage());
                });
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
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        if (mostrar) {
            progressBar.setAlpha(0f);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.animate().alpha(1f).setDuration(250).start();
        } else {
            progressBar.animate().alpha(0f).setDuration(250).withEndAction(() ->
                    progressBar.setVisibility(View.GONE)).start();
        }
        btnRegistrar.setEnabled(!mostrar);
        btnUbicacion.setEnabled(!mostrar);
        etNumero.setEnabled(!mostrar);
        etCliente.setEnabled(!mostrar);
        etDireccion.setEnabled(!mostrar);
        etObservaciones.setEnabled(!mostrar);
        actvEstado.setEnabled(!mostrar);
    }

    private void mostrarSnackbar(String mensaje) {
        Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.success, getTheme()))
                .setTextColor(getResources().getColor(android.R.color.white, getTheme()))
                .show();
    }

    private void mostrarError(String mensaje) {
        Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.error, getTheme()))
                .setTextColor(getResources().getColor(android.R.color.white, getTheme()))
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hayDatosIngresados() {
        return !etNumero.getText().toString().trim().isEmpty() ||
                !etCliente.getText().toString().trim().isEmpty() ||
                !etDireccion.getText().toString().trim().isEmpty() ||
                !etObservaciones.getText().toString().trim().isEmpty() ||
                !actvEstado.getText().toString().trim().isEmpty();
    }

    @Override
    public void onBackPressed() {
        if (hayDatosIngresados()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Salir sin guardar?")
                    .setMessage("Hay datos ingresados que no se han guardado. ¿Está seguro de que desea salir?")
                    .setPositiveButton("Salir", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancelar", null)
                    .setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_rounded_bg))
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}