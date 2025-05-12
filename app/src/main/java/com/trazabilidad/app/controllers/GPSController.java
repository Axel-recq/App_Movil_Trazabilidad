package com.trazabilidad.app.controllers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.trazabilidad.app.database.UbicacionDAO;
import com.trazabilidad.app.models.Ubicacion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GPSController implements LocationListener {

    private static final String TAG = "GPSController";
    private static final long MIN_TIME_BETWEEN_UPDATES = 60000; // 1 minuto
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 50 metros

    private Context context;
    private LocationManager locationManager;
    private UbicacionDAO ubicacionDAO;
    private int currentPedidoId = -1;
    private int usuarioId;
    private boolean isTracking = false;
    private LocationUpdateCallback locationUpdateCallback;

    public GPSController(Context context, int usuarioId) {
        this.context = context;
        this.usuarioId = usuarioId;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.ubicacionDAO = new UbicacionDAO(context);
    }

    public void setLocationUpdateCallback(LocationUpdateCallback callback) {
        this.locationUpdateCallback = callback;
    }

    public void startTracking(int pedidoId) {
        if (isTracking) {
            Log.w(TAG, "Ya se está rastreando un pedido.");
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No se tienen permisos de ubicación");
            return;
        }
        currentPedidoId = pedidoId;
        isTracking = true;
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this
        );
        Log.d(TAG, "Rastreo iniciado para pedido ID: " + pedidoId);
    }

    public void stopTracking() {
        if (!isTracking) {
            return;
        }
        locationManager.removeUpdates(this);
        isTracking = false;
        currentPedidoId = -1;
        Log.d(TAG, "Rastreo detenido");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!isTracking) {
            return;
        }
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setLatitud(location.getLatitude());
        ubicacion.setLongitud(location.getLongitude());
        ubicacion.setFecha(new Date());
        ubicacion.setUsuarioId(usuarioId);
        ubicacion.setPedidoId(currentPedidoId);
        boolean inserted = ubicacionDAO.insertarUbicacion(ubicacion);
        if (inserted) {
            Log.d(TAG, "Ubicación insertada para pedido ID: " + currentPedidoId);
            if (locationUpdateCallback != null) {
                locationUpdateCallback.onLocationUpdated(location.getLatitude(), location.getLongitude());
            }
        } else {
            Log.e(TAG, "Error al insertar ubicación");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (isTracking) {
            stopTracking();
            Log.w(TAG, "Proveedor de ubicación desactivado, rastreo detenido");
            if (locationUpdateCallback != null) {
                locationUpdateCallback.onError("El proveedor de ubicación está desactivado");
            }
        }
    }

    public void obtenerUbicacionActual(UbicacionCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("No se tienen permisos de ubicación");
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            callback.onUbicacionObtenida(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            return;
        }
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                callback.onUbicacionObtenida(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                callback.onError("El proveedor de ubicación está desactivado");
            }
        }, null);
    }

    public void trazarRuta(GoogleMap mMap, int pedidoId) {
        List<Ubicacion> ubicaciones = ubicacionDAO.obtenerUbicacionesPorPedido(pedidoId);
        if (ubicaciones.isEmpty()) {
            Log.w(TAG, "No hay ubicaciones para el pedido ID: " + pedidoId);
            return;
        }
        List<LatLng> puntos = new ArrayList<>();
        for (Ubicacion ubicacion : ubicaciones) {
            puntos.add(new LatLng(ubicacion.getLatitud(), ubicacion.getLongitud()));
        }
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(puntos)
                .width(5)
                .color(0xFF0000FF);
        mMap.addPolyline(polylineOptions);
        ajustarCamaraParaMostrarRuta(mMap, puntos);
    }

    public void trazarRutaEntreDosPuntos(GoogleMap mMap, LatLng origen, LatLng destino, String destinoTitle) {
        // Añadir marcadores para origen y destino
        mMap.addMarker(new MarkerOptions().position(origen).title("Origen"));
        mMap.addMarker(new MarkerOptions().position(destino).title(destinoTitle));

        // Trazar una línea recta simple entre los dos puntos
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(origen, destino)
                .width(5)
                .color(0xFF0000FF);
        mMap.addPolyline(polylineOptions);

        // Ajustar la cámara para mostrar ambos puntos
        List<LatLng> puntos = new ArrayList<>();
        puntos.add(origen);
        puntos.add(destino);
        ajustarCamaraParaMostrarRuta(mMap, puntos);
    }

    public void ajustarCamaraParaMostrarRuta(GoogleMap mMap, List<LatLng> puntos) {
        if (puntos.isEmpty()) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng punto : puntos) {
            builder.include(punto);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // en píxeles
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    public interface UbicacionCallback {
        void onUbicacionObtenida(double latitud, double longitud);
        void onError(String message);
    }

    public interface LocationUpdateCallback {
        void onLocationUpdated(double latitud, double longitud);
        void onError(String message);
    }
}