package com.trazabilidad.app.controllers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class GPSController {

    private Context context;
    private LocationManager locationManager;

    public GPSController(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void obtenerUbicacionActual(UbicacionCallback callback) {
        // Verificar permisos
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("No se tienen permisos de ubicación");
            return;
        }

        // Intentar obtener última ubicación conocida
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            callback.onUbicacionObtenida(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            return;
        }

        // Si no hay ubicación conocida, solicitar actualizaciones
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

    public void trazarRuta(GoogleMap mMap, LatLng origen, LatLng destino) {
        //aquí se haría una llamada a la API de Google Directions
        // Para simplificar, solo trazamos una línea recta entre origen y destino

        List<LatLng> puntos = new ArrayList<>();
        puntos.add(origen);
        puntos.add(destino);

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(puntos)
                .width(5)
                .color(0xFF0000FF);

        mMap.addPolyline(polylineOptions);
    }

    public void ajustarCamaraParaMostrarRuta(GoogleMap mMap, LatLng origen, LatLng destino) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origen);
        builder.include(destino);

        LatLngBounds bounds = builder.build();

        // Ajustar cámara con padding
        int padding = 100; // en píxeles
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    public interface UbicacionCallback {
        void onUbicacionObtenida(double latitud, double longitud);
        void onError(String message);
    }
}
