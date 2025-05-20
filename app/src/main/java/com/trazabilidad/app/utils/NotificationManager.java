package com.trazabilidad.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trazabilidad.app.controllers.ProductoController;
import com.trazabilidad.app.database.DatabaseHelper;
import com.trazabilidad.app.database.DevolucionDAO;
import com.trazabilidad.app.database.NotificacionDAO;
import com.trazabilidad.app.models.Devolucion;
import com.trazabilidad.app.models.Notificacion;
import com.trazabilidad.app.models.Producto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NotificationManager {
    private static final String TAG = "NotificationManager";
    private final ProductoController productoController;
    private final DevolucionDAO devolucionDAO;
    private final NotificacionDAO notificacionDAO;
    private final DatabaseHelper dbHelper;
    private final Context context;
    private final Executor executor;

    public NotificationManager(Context context) {
        this.context = context;
        this.productoController = new ProductoController(context);
        this.devolucionDAO = new DevolucionDAO(context);
        this.notificacionDAO = new NotificacionDAO(context);
        this.dbHelper = DatabaseHelper.getInstance(context);
        // Crear un executor para operaciones asíncronas
        this.executor = Executors.newSingleThreadExecutor();
    }

    public interface NotificationCallback {
        void onSuccess(List<Notificacion> notificaciones);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess(String message);
        void onError(String message);
    }

    /**
     * Limpia notificaciones antiguas (más de 30 días)
     */
    public void limpiarNotificacionesAntiguas() {
        executor.execute(() -> {
            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                long hace30Dias = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
                int filasEliminadas = db.delete(DatabaseHelper.TABLE_NOTIFICACIONES,
                        DatabaseHelper.COLUMN_NOTIFICACION_FECHA + " < ?",
                        new String[]{String.valueOf(hace30Dias)});
                if (filasEliminadas > 0) {
                    Log.d(TAG, "Eliminadas " + filasEliminadas + " notificaciones antiguas");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al limpiar notificaciones antiguas: " + e.getMessage());
            }
        });
    }

    /**
     * Verifica y limpia notificaciones si ha pasado un día desde la última limpieza
     */
    public void verificarLimpieza() {
        SharedPreferences prefs = context.getSharedPreferences("TrazabilidadPrefs", Context.MODE_PRIVATE);
        long ultimaLimpieza = prefs.getLong("ultima_limpieza_notificaciones", 0);
        long ahora = System.currentTimeMillis();
        long unDiaEnMilis = 24 * 60 * 60 * 1000;

        if (ahora - ultimaLimpieza > unDiaEnMilis) {
            limpiarNotificacionesAntiguas();
            prefs.edit().putLong("ultima_limpieza_notificaciones", ahora).apply();
        }
    }

    /**
     * Obtener notificaciones con filtro opcional por tipo
     * @param callback Callback para retornar resultados
     * @param tipoFiltro Tipo de notificación para filtrar (null para todas)
     */
    public void obtenerNotificaciones(NotificationCallback callback, Notificacion.Tipo tipoFiltro) {
        // Verificar si es necesario hacer limpieza de notificaciones antiguas
        verificarLimpieza();

        List<Notificacion> notificaciones = new ArrayList<>();
        String tipoFiltroStr = tipoFiltro != null ? tipoFiltro.name() : null;

        // Obtener notificaciones existentes de la base de datos
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_NOTIFICACIONES,
                     new String[]{
                             DatabaseHelper.COLUMN_NOTIFICACION_ID,
                             DatabaseHelper.COLUMN_NOTIFICACION_TIPO,
                             DatabaseHelper.COLUMN_NOTIFICACION_MENSAJE,
                             DatabaseHelper.COLUMN_NOTIFICACION_FECHA,
                             DatabaseHelper.COLUMN_NOTIFICACION_LEIDA,
                             DatabaseHelper.COLUMN_NOTIFICACION_REFERENCIA_ID,
                             DatabaseHelper.COLUMN_NOTIFICACION_PRODUCTO_ID
                     },
                     tipoFiltroStr != null ? DatabaseHelper.COLUMN_NOTIFICACION_TIPO + " = ?" : null,
                     tipoFiltroStr != null ? new String[]{tipoFiltroStr} : null,
                     null,
                     null,
                     DatabaseHelper.COLUMN_NOTIFICACION_FECHA + " DESC"
             )) {
            while (cursor.moveToNext()) {
                Notificacion notificacion = new Notificacion();
                notificacion.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICACION_ID)));
                notificacion.setTipo(Notificacion.Tipo.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICACION_TIPO))));
                notificacion.setMensaje(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICACION_MENSAJE)));
                notificacion.setFecha(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICACION_FECHA))));
                notificacion.setLeida(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICACION_LEIDA)) == 1);
                notificacion.setReferenciaId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICACION_REFERENCIA_ID)));
                int productoIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTIFICACION_PRODUCTO_ID);
                if (!cursor.isNull(productoIdIndex)) {
                    notificacion.setProductoId(cursor.getInt(productoIdIndex));
                }
                notificaciones.add(notificacion);
            }

            // Si estamos filtrando, no necesitamos revisar y generar nuevas notificaciones
            if (tipoFiltroStr != null) {
                callback.onSuccess(notificaciones);
                return;
            }
        } catch (Exception e) {
            callback.onError("Error al leer notificaciones: " + e.getMessage());
            return;
        }

        // Generar nuevas notificaciones de bajo stock si no estamos filtrando
        if (tipoFiltroStr == null) {
            generarNotificacionesActualizadas(notificaciones, callback);
        } else {
            callback.onSuccess(notificaciones);
        }
    }

    /**
     * Sobrecarga del método obtenerNotificaciones para compatibilidad con código existente
     */
    public void obtenerNotificaciones(NotificationCallback callback) {
        obtenerNotificaciones(callback, null);
    }

    /**
     * Genera notificaciones de bajo stock y devoluciones recientes
     */
    private void generarNotificacionesActualizadas(List<Notificacion> notificaciones, NotificationCallback callback) {
        productoController.obtenerProductosBajoStock(new ProductoController.ProductosCallback() {
            @Override
            public void onSuccess(List<Producto> productos) {
                try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                    for (Producto producto : productos) {
                        // Verificar si ya existe una notificación para este producto
                        Cursor cursor = db.query(
                                DatabaseHelper.TABLE_NOTIFICACIONES,
                                new String[]{DatabaseHelper.COLUMN_NOTIFICACION_ID},
                                DatabaseHelper.COLUMN_NOTIFICACION_TIPO + " = ? AND " +
                                        DatabaseHelper.COLUMN_NOTIFICACION_REFERENCIA_ID + " = ?",
                                new String[]{"BAJO_STOCK", String.valueOf(producto.getId())},
                                null,
                                null,
                                null
                        );
                        if (!cursor.moveToFirst()) {
                            Notificacion notificacion = new Notificacion();
                            notificacion.setTipo(Notificacion.Tipo.BAJO_STOCK);
                            notificacion.setMensaje("Producto " + producto.getNombre() + " tiene bajo stock: " + producto.getCantidad() + " unidades");
                            notificacion.setFecha(new Date());
                            notificacion.setLeida(false);
                            notificacion.setReferenciaId(producto.getId());
                            notificacion.setProductoId(producto.getId());

                            ContentValues values = new ContentValues();
                            values.put(DatabaseHelper.COLUMN_NOTIFICACION_TIPO, notificacion.getTipo().name());
                            values.put(DatabaseHelper.COLUMN_NOTIFICACION_MENSAJE, notificacion.getMensaje());
                            values.put(DatabaseHelper.COLUMN_NOTIFICACION_FECHA, notificacion.getFecha().getTime());
                            values.put(DatabaseHelper.COLUMN_NOTIFICACION_LEIDA, notificacion.isLeida() ? 1 : 0);
                            values.put(DatabaseHelper.COLUMN_NOTIFICACION_REFERENCIA_ID, notificacion.getReferenciaId());
                            values.put(DatabaseHelper.COLUMN_NOTIFICACION_PRODUCTO_ID, notificacion.getProductoId());

                            long id = db.insert(DatabaseHelper.TABLE_NOTIFICACIONES, null, values);
                            if (id > 0) {
                                notificacion.setId((int) id);
                                notificaciones.add(notificacion);
                            }
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al generar notificaciones de bajo stock: " + e.getMessage());
                }
                cargarNotificacionesDevoluciones(notificaciones, callback);
            }

            @Override
            public void onError(String message) {
                cargarNotificacionesDevoluciones(notificaciones, callback);
            }
        });
    }

    private void cargarNotificacionesDevoluciones(List<Notificacion> notificaciones, NotificationCallback callback) {
        // Generar notificaciones de devoluciones recientes
        long hace24Horas = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        List<Devolucion> devoluciones = devolucionDAO.obtenerDevolucionesRecientes(hace24Horas);

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            for (Devolucion devolucion : devoluciones) {
                // Verificar si ya existe una notificación para esta devolución
                Cursor cursor = db.query(
                        DatabaseHelper.TABLE_NOTIFICACIONES,
                        new String[]{DatabaseHelper.COLUMN_NOTIFICACION_ID},
                        DatabaseHelper.COLUMN_NOTIFICACION_TIPO + " = ? AND " +
                                DatabaseHelper.COLUMN_NOTIFICACION_REFERENCIA_ID + " = ?",
                        new String[]{"DEVOLUCION", String.valueOf(devolucion.getPedidoId())},
                        null,
                        null,
                        null
                );
                if (!cursor.moveToFirst()) {
                    Notificacion notificacion = new Notificacion();
                    notificacion.setTipo(Notificacion.Tipo.DEVOLUCION);
                    notificacion.setMensaje("Devolución registrada para pedido #" + devolucion.getPedidoId() +
                            ": " + devolucion.getMotivo());
                    notificacion.setFecha(devolucion.getFecha());
                    notificacion.setLeida(false);
                    notificacion.setReferenciaId(devolucion.getPedidoId());
                    notificacion.setProductoId(devolucion.getProductoId());

                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_NOTIFICACION_TIPO, notificacion.getTipo().name());
                    values.put(DatabaseHelper.COLUMN_NOTIFICACION_MENSAJE, notificacion.getMensaje());
                    values.put(DatabaseHelper.COLUMN_NOTIFICACION_FECHA, notificacion.getFecha().getTime());
                    values.put(DatabaseHelper.COLUMN_NOTIFICACION_LEIDA, notificacion.isLeida() ? 1 : 0);
                    values.put(DatabaseHelper.COLUMN_NOTIFICACION_REFERENCIA_ID, notificacion.getReferenciaId());
                    values.put(DatabaseHelper.COLUMN_NOTIFICACION_PRODUCTO_ID, notificacion.getProductoId());

                    long id = db.insert(DatabaseHelper.TABLE_NOTIFICACIONES, null, values);
                    if (id > 0) {
                        notificacion.setId((int) id);
                        notificaciones.add(notificacion);
                    }
                }
                cursor.close();
            }
            callback.onSuccess(notificaciones);
        } catch (Exception e) {
            callback.onError("Error al generar notificaciones de devoluciones: " + e.getMessage());
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    public void marcarComoLeidas() {
        executor.execute(() -> {
            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_NOTIFICACION_LEIDA, 1);
                db.update(DatabaseHelper.TABLE_NOTIFICACIONES, values, null, null);
            } catch (Exception e) {
                Log.e(TAG, "Error al marcar notificaciones como leídas: " + e.getMessage());
            }
        });
    }

    /**
     * Marca como leídas solo las notificaciones de un tipo específico.
     * @param tipo Tipo de notificación a marcar
     */
    public void marcarComoLeidasPorTipo(Notificacion.Tipo tipo) {
        executor.execute(() -> {
            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_NOTIFICACION_LEIDA, 1);
                db.update(
                        DatabaseHelper.TABLE_NOTIFICACIONES,
                        values,
                        DatabaseHelper.COLUMN_NOTIFICACION_TIPO + " = ?",
                        new String[]{tipo.name()}
                );
            } catch (Exception e) {
                Log.e(TAG, "Error al marcar notificaciones como leídas por tipo: " + e.getMessage());
            }
        });
    }

    /**
     * Elimina una notificación específica.
     */
    public void eliminarNotificacion(int notificacionId, OperationCallback callback) {
        executor.execute(() -> {
            boolean exito = notificacionDAO.eliminarNotificacion(notificacionId);
            if (exito) {
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> callback.onSuccess("Notificación eliminada correctamente"));
            } else {
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> callback.onError("No se pudo eliminar la notificación"));
            }
        });
    }

    /**
     * Elimina todas las notificaciones o las de un tipo específico.
     */
    public void eliminarNotificaciones(Notificacion.Tipo tipo, OperationCallback callback) {
        executor.execute(() -> {
            int eliminadas;
            if (tipo != null) {
                eliminadas = notificacionDAO.eliminarNotificaciones(tipo.name());
            } else {
                eliminadas = notificacionDAO.eliminarNotificaciones(null);
            }

            android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
            if (eliminadas > 0) {
                mainHandler.post(() -> callback.onSuccess("Se eliminaron " + eliminadas + " notificaciones"));
            } else {
                mainHandler.post(() -> callback.onError("No se encontraron notificaciones para eliminar"));
            }
        });
    }

    /**
     * Obtiene el conteo de notificaciones no leídas
     */
    public int getNotificacionesNoLeidasCount() {
        int count = 0;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_NOTIFICACIONES,
                     new String[]{DatabaseHelper.COLUMN_NOTIFICACION_ID},
                     DatabaseHelper.COLUMN_NOTIFICACION_LEIDA + " = ?",
                     new String[]{"0"},
                     null,
                     null,
                     null
             )) {
            count = cursor.getCount();
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener conteo de notificaciones no leídas: " + e.getMessage());
        }
        return count;
    }

    /**
     * Obtiene el conteo de notificaciones no leídas por tipo.
     */
    public int getNotificacionesNoLeidasCountPorTipo(Notificacion.Tipo tipo) {
        int count = 0;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_NOTIFICACIONES,
                     new String[]{DatabaseHelper.COLUMN_NOTIFICACION_ID},
                     DatabaseHelper.COLUMN_NOTIFICACION_LEIDA + " = ? AND " +
                             DatabaseHelper.COLUMN_NOTIFICACION_TIPO + " = ?",
                     new String[]{"0", tipo.name()},
                     null,
                     null,
                     null
             )) {
            count = cursor.getCount();
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener conteo de notificaciones no leídas por tipo: " + e.getMessage());
        }
        return count;
    }
}