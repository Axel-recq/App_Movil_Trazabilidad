package com.trazabilidad.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

    private TextInputLayout tilNumero, tilCliente, tilDireccion, tilObservaciones;
    private TextInputEditText etNumero, etCliente, etDireccion, etObservaciones;
    private AutoCompleteTextView actvEstado;
    private MaterialButton btnRegistrar, btnUbicacion;
    private ProgressBar progressBar;
    private Toolbar toolbar;

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
        btnRegistrar.setOnClickListener(v -> validarYRegistrarPedido());
        btnUbicacion.setOnClickListener(v -> obtenerUbicacionActual());
    }

    private void inicializarUI() {
        toolbar = findViewById(R.id.toolbar);
        tilNumero = findViewById(R.id.tilNumero);
        tilCliente = findViewById(R.id.tilCliente);
        tilDireccion = findViewById(R.id.tilDireccion);
        tilObservaciones = findViewById(R.id.tilObservaciones);
        etNumero = findViewById(R.id.etNumero);
        etCliente = findViewById(R.id.etCliente);
        etDireccion = findViewById(R.id.etDireccion);
        etObservaciones = findViewById(R.id.etObservaciones);
        actvEstado = findViewById(R.id.actvEstado);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnUbicacion = findViewById(R.id.btnUbicacion);
        progressBar = findViewById(R.id.progressBar);
    }

    private void configurarEstados() {
        List<String> estados = Arrays.asList("Pendiente", "En proceso", "Entregado", "Cancelado");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, estados);
        actvEstado.setAdapter(adapter);
        actvEstado.setText(estados.get(0), false);  // Establecer "Pendiente" por defecto
    }

    private void validarYRegistrarPedido() {
        // Resetear errores
        tilNumero.setError(null);
        tilCliente.setError(null);
        tilDireccion.setError(null);

        // Obtener valores
        String numero = etNumero.getText().toString().trim();
        String cliente = etCliente.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String observaciones = etObservaciones.getText().toString().trim();
        String estado = actvEstado.getText().toString().trim();

        // Validar campos obligatorios
        boolean isValid = true;

        if (numero.isEmpty()) {
            tilNumero.setError("El número de pedido es obligatorio");
            isValid = false;
        }

        if (cliente.isEmpty()) {
            tilCliente.setError("El nombre del cliente es obligatorio");
            isValid = false;
        }

        if (direccion.isEmpty()) {
            tilDireccion.setError("La dirección es obligatoria");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Crear objeto pedido
        Pedido pedido = new Pedido();
        pedido.setNumero(numero);
        pedido.setCliente(cliente);
        pedido.setDireccion(direccion);
        pedido.setObservaciones(observaciones);
        pedido.setEstado(estado);
        pedido.setFecha(new Date());
        pedido.setUsuarioId(sessionManager.getUsuarioDetails().getId());

        // Asignar coordenadas si se obtuvieron
        if (ubicacionObtenida) {
            pedido.setLatitud(latitud);
            pedido.setLongitud(longitud);
        }

        // Mostrar confirmación
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar registro")
                .setMessage("¿Está seguro de registrar este pedido?")
                .setPositiveButton("Registrar", (dialog, which) -> registrarPedido(pedido))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void registrarPedido(Pedido pedido) {
        mostrarProgreso(true);

        pedidoController.registrarPedido(pedido, new PedidoController.OperacionCallback() {
            @Override
            public void onSuccess() {
                mostrarProgreso(false);
                mostrarSnackbar("Pedido registrado correctamente");

                // Dar tiempo para que el usuario vea el mensaje antes de cerrar la actividad
                new android.os.Handler().postDelayed(() -> {
                    setResult(RESULT_OK);
                    finish();
                }, 1500);
            }

            @Override
            public void onError(String message) {
                mostrarProgreso(false);
                mostrarSnackbar("Error: " + message);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        mostrarProgreso(true);
        btnUbicacion.setText("Obteniendo ubicación...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    mostrarProgreso(false);

                    if (location != null) {
                        latitud = location.getLatitude();
                        longitud = location.getLongitude();
                        ubicacionObtenida = true;

                        btnUbicacion.setText("Ubicación obtenida ✓");
                        btnUbicacion.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map));
                        mostrarSnackbar("Ubicación obtenida correctamente");
                    } else {
                        btnUbicacion.setText("Obtener ubicación");
                        mostrarSnackbar("No se pudo obtener la ubicación. Intente nuevamente.");
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    btnUbicacion.setText("Obtener ubicación");
                    mostrarSnackbar("Error al obtener ubicación: " + e.getMessage());
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                mostrarSnackbar("Se requiere permiso de ubicación para esta función");
            }
        }
    }

    private void mostrarProgreso(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!mostrar);
        btnUbicacion.setEnabled(!mostrar);
        etNumero.setEnabled(!mostrar);
        etCliente.setEnabled(!mostrar);
        etDireccion.setEnabled(!mostrar);
        etObservaciones.setEnabled(!mostrar);
        actvEstado.setEnabled(!mostrar);
    }

    private void mostrarSnackbar(String mensaje) {
        Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}