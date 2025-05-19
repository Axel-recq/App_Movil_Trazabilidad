package com.trazabilidad.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.controllers.ProductoController;
import com.trazabilidad.app.database.DatabaseHelper;
import com.trazabilidad.app.database.DevolucionDAO;
import com.trazabilidad.app.models.Devolucion;
import com.trazabilidad.app.models.Notificacion;
import com.trazabilidad.app.models.Producto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationManager {
    private final ProductoController productoController;
    private final DevolucionDAO devolucionDAO;
    private final DatabaseHelper dbHelper;
    private final Context context;

    public NotificationManager(Context context) {
        this.context = context;
        this.productoController = new ProductoController(context);
        this.devolucionDAO = new DevolucionDAO(context);
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public interface NotificationCallback {
        void onSuccess(List<Notificacion> notificaciones);
        void onError(String message);
    }
    public void limpiarNotificacionesAntiguas() {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            long hace30Dias = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
            int filasEliminadas = db.delete(DatabaseHelper.TABLE_NOTIFICACIONES,
                    DatabaseHelper.COLUMN_NOTIFICACION_FECHA + " < ?",
                    new String[]{String.valueOf(hace30Dias)});
            if (filasEliminadas > 0) {
                android.util.Log.d("NotificationManager", "Eliminadas " + filasEliminadas + " notificaciones antiguas");
            }
        } catch (Exception e) {
            android.util.Log.e("NotificationManager", "Error al limpiar notificaciones antiguas: " + e.getMessage());
        }
    }
    public void obtenerNotificaciones(NotificationCallback callback) {
        List<Notificacion> notificaciones = new ArrayList<>();

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
                     null,
                     null,
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
                notificacion.setProductoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICACION_PRODUCTO_ID)));
                notificaciones.add(notificacion);
            }
        } catch (Exception e) {
            callback.onError("Error al leer notificaciones: " + e.getMessage());
            return;
        }

        // Generar nuevas notificaciones de bajo stock
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
                            notificacion.setId(producto.getId());
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
                            db.insert(DatabaseHelper.TABLE_NOTIFICACIONES, null, values);

                            notificaciones.add(notificacion);
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    // Log error
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
                    notificacion.setId(devolucion.getId());
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
                    db.insert(DatabaseHelper.TABLE_NOTIFICACIONES, null, values);

                    notificaciones.add(notificacion);
                }
                cursor.close();
            }
            callback.onSuccess(notificaciones);
        } catch (Exception e) {
            callback.onError("Error al generar notificaciones de devoluciones: " + e.getMessage());
        }
    }

    public void marcarComoLeidas() {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NOTIFICACION_LEIDA, 1);
            db.update(DatabaseHelper.TABLE_NOTIFICACIONES, values, null, null);
        } catch (Exception e) {
            // Log error
        }
    }

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
            // Log error
        }
        return count;
    }
}