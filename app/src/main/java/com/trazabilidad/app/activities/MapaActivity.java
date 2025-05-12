package com.trazabilidad.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.GPSController;
import com.trazabilidad.app.utils.SessionManager;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private GoogleMap mMap;
    private GPSController gpsController;
    private Marker currentLocationMarker;

    private double latitudDestino;
    private double longitudDestino;
    private String direccionDestino;
    private boolean mostrarRuta;
    private int pedidoId;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mapa de Entrega");
        }

        // Obtener usuarioId desde SessionManager
        SessionManager sessionManager = new SessionManager(this);
        usuarioId = sessionManager.getUsuarioDetails().getId();

        // Inicializar GPSController
        gpsController = new GPSController(this, usuarioId);

        // Obtener datos del intent
        latitudDestino = getIntent().getDoubleExtra("LATITUD", 0);
        longitudDestino = getIntent().getDoubleExtra("LONGITUD", 0);
        direccionDestino = getIntent().getStringExtra("DIRECCION");
        pedidoId = getIntent().getIntExtra("PEDIDO_ID", -1);
        mostrarRuta = latitudDestino != 0 && longitudDestino != 0;

        // Cargar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error al cargar el mapa", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Configurar el callback para el botón de retroceso
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            Toast.makeText(this, "Error al inicializar el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkLocationPermission()) {
            configurarMapa();
        } else {
            requestLocationPermission();
        }
    }

    private void configurarMapa() {
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Iniciar rastreo si hay un pedidoId
            if (pedidoId != -1) {
                gpsController.startTracking(pedidoId);
            }

            // Obtener la ubicación actual
            gpsController.obtenerUbicacionActual(new GPSController.UbicacionCallback() {
                @Override
                public void onUbicacionObtenida(double latitud, double longitud) {
                    LatLng miPosicion = new LatLng(latitud, longitud);

                    // Actualizar o crear el marcador de la ubicación actual
                    if (currentLocationMarker == null) {
                        currentLocationMarker = mMap.addMarker(new MarkerOptions().position(miPosicion).title("Mi ubicación"));
                    } else {
                        currentLocationMarker.setPosition(miPosicion);
                    }

                    // Centrar el mapa en la ubicación actual
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15));

                    // Manejar la ruta
                    if (mostrarRuta) {
                        LatLng destino = new LatLng(latitudDestino, longitudDestino);
                        String marcadorTitle = (direccionDestino != null && !direccionDestino.isEmpty())
                                ? direccionDestino : "Destino";
                        gpsController.trazarRutaEntreDosPuntos(mMap, miPosicion, destino, marcadorTitle);
                    } else if (pedidoId != -1) {
                        gpsController.trazarRuta(mMap, pedidoId);
                    }
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(MapaActivity.this, message, Toast.LENGTH_LONG).show();

                    // Si hay un destino, centrar el mapa en él
                    if (mostrarRuta) {
                        LatLng destino = new LatLng(latitudDestino, longitudDestino);
                        String marcadorTitle = (direccionDestino != null && !direccionDestino.isEmpty())
                                ? direccionDestino : "Destino";
                        mMap.addMarker(new MarkerOptions().position(destino).title(marcadorTitle));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 15));
                    } else if (pedidoId != -1) {
                        gpsController.trazarRuta(mMap, pedidoId);
                    } else {
                        Toast.makeText(MapaActivity.this, "No hay datos de ubicación disponibles", Toast.LENGTH_LONG).show();
                    }
                }
            });

            // Configurar listener para actualizaciones en tiempo real
            mMap.setOnMapLoadedCallback(() -> {
                if (checkLocationPermission()) {
                    gpsController.obtenerUbicacionActual(new GPSController.UbicacionCallback() {
                        @Override
                        public void onUbicacionObtenida(double latitud, double longitud) {
                            LatLng miPosicion = new LatLng(latitud, longitud);
                            if (currentLocationMarker != null) {
                                currentLocationMarker.setPosition(miPosicion);
                            }
                        }

                        @Override
                        public void onError(String message) {
                            // Ignorar errores en actualizaciones en tiempo real
                        }
                    });
                }
            });

        } catch (SecurityException e) {
            Toast.makeText(this, "Permiso denegado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                configurarMapa();
            } else {
                Toast.makeText(this, "Se requieren permisos de ubicación para usar el mapa", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el rastreo al salir de la actividad
        gpsController.stopTracking();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}