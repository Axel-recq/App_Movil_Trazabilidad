package com.trazabilidad.app.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.trazabilidad.app.R;
import com.trazabilidad.app.activities.MainActivity;
import com.trazabilidad.app.models.Ubicacion;
import com.trazabilidad.app.utils.SessionManager;

import java.util.Date;

public class GPSService extends Service implements LocationListener {

    private static final String CHANNEL_ID = "GPSServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final long MIN_TIME_BETWEEN_UPDATES = 60000; // 1 minuto
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 50 metros

    private LocationManager locationManager;
    private SessionManager sessionManager;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Crear canal de notificación para Android 8.0+
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            startForeground(NOTIFICATION_ID, createNotification());
            startLocationUpdates();
            isRunning = true;
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        isRunning = false;
    }

    private void startLocationUpdates() {
        // Verificar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Solicitar actualizaciones de ubicación
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this
        );
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Verificar si hay sesión activa
        if (!sessionManager.isLoggedIn()) {
            stopSelf();
            return;
        }

        // Crear objeto Ubicacion
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setLatitud(location.getLatitude());
        ubicacion.setLongitud(location.getLongitude());
        ubicacion.setFecha(new Date());
        ubicacion.setUsuarioId(sessionManager.getUsuarioDetails().getId());

        // Aquí se guardaría la ubicación en la base de datos local
        // Y opcionalmente se enviaría al servidor

        // Actualizar notificación
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "GPS Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Canal para el servicio de seguimiento GPS");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Servicio GPS Activo")
                .setContentText("Rastreando ubicación...")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return builder.build();
    }
}
