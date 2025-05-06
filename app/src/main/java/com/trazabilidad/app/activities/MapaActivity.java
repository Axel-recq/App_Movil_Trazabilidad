package com.trazabilidad.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.GPSController;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private GPSController gpsController;

    private double latitudDestino;
    private double longitudDestino;
    private String direccionDestino;
    private boolean mostrarRuta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mapa de Entrega");
        }

        // Inicializar servicios
        gpsController = new GPSController(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener datos del intent
        latitudDestino = getIntent().getDoubleExtra("LATITUD", 0);
        longitudDestino = getIntent().getDoubleExtra("LONGITUD", 0);
        direccionDestino = getIntent().getStringExtra("DIRECCION");
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

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15));
                            mMap.addMarker(new MarkerOptions().position(miPosicion).title("Mi ubicación"));

                            if (mostrarRuta) {
                                LatLng destino = new LatLng(latitudDestino, longitudDestino);
                                String markerTitle = (direccionDestino != null && !direccionDestino.isEmpty())
                                        ? direccionDestino : "Destino";
                                mMap.addMarker(new MarkerOptions().position(destino).title(markerTitle));
                                gpsController.trazarRuta(mMap, miPosicion, destino);
                                gpsController.ajustarCamaraParaMostrarRuta(mMap, miPosicion, destino);
                            }
                        } else {
                            Toast.makeText(MapaActivity.this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MapaActivity.this, "Error al obtener ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}