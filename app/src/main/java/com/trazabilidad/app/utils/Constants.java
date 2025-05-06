package com.trazabilidad.app.utils;

public class Constants {

    // API URLs
    public static final String API_URL = "https://api.example.com/trazabilidad";

    // Estados de pedidos
    public static final String ESTADO_ASIGNADO = "ASIGNADO";
    public static final String ESTADO_EN_RUTA = "EN_RUTA";
    public static final String ESTADO_ENTREGADO = "ENTREGADO";
    public static final String ESTADO_INCIDENCIA = "INCIDENCIA";

    // Tipos de incidencias
    public static final String INCIDENCIA_DIRECCION = "DIRECCION_INCORRECTA";
    public static final String INCIDENCIA_CLIENTE = "CLIENTE_AUSENTE";
    public static final String INCIDENCIA_PRODUCTO = "PRODUCTO_DAÑADO";
    public static final String INCIDENCIA_OTRO = "OTRO";

    // Roles de usuario
    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_REPARTIDOR = "REPARTIDOR";
    public static final String ROL_SUPERVISOR = "SUPERVISOR";

    // Preferencias
    public static final String PREF_TRACKING_ENABLED = "tracking_enabled";
    public static final String PREF_SYNC_INTERVAL = "sync_interval";

    // Códigos de solicitud
    public static final int REQUEST_LOCATION_PERMISSION = 100;
    public static final int REQUEST_CAMERA_PERMISSION = 101;
    public static final int REQUEST_STORAGE_PERMISSION = 102;

    // Notificaciones
    public static final String NOTIFICATION_CHANNEL_ID = "trazabilidad_channel";
    public static final int NOTIFICATION_ID = 1001;

    // Intervalos de tiempo (en milisegundos)
    public static final long LOCATION_UPDATE_INTERVAL = 60000; // 1 minuto
    public static final long SYNC_INTERVAL_DEFAULT = 900000; // 15 minutos
}
