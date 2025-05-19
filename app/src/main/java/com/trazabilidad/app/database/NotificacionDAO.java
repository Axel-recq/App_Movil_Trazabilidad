package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trazabilidad.app.models.Notificacion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificacionDAO {

    private static final String TAG = "NotificacionDAO";
    private final DatabaseHelper dbHelper;

    public NotificacionDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Inserta una notificación.
     * @param tipo Tipo de notificación (ej. CALIFICACION)
     * @param mensaje Mensaje de la notificación
     * @param referenciaId ID del pedido asociado
     * @param productoId ID del producto (puede ser null)
     * @return true si la inserción fue exitosa, false en caso contrario
     */
    public boolean insertarNotificacion(String tipo, String mensaje, int referenciaId, Integer productoId) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NOTIFICACION_TIPO, tipo);
            values.put(DatabaseHelper.COLUMN_NOTIFICACION_MENSAJE, mensaje);
            values.put(DatabaseHelper.COLUMN_NOTIFICACION_FECHA, System.currentTimeMillis());
            values.put(DatabaseHelper.COLUMN_NOTIFICACION_LEIDA, 0);
            values.put(DatabaseHelper.COLUMN_NOTIFICACION_REFERENCIA_ID, referenciaId);
            if (productoId != null) {
                values.put(DatabaseHelper.COLUMN_NOTIFICACION_PRODUCTO_ID, productoId);
            } else {
                values.putNull(DatabaseHelper.COLUMN_NOTIFICACION_PRODUCTO_ID);
            }

            long id = db.insert(DatabaseHelper.TABLE_NOTIFICACIONES, null, values);
            if (id == -1) {
                Log.w(TAG, "Error al insertar notificación para referenciaId: " + referenciaId);
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar notificación: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Marca una notificación como leída.
     * @param notificacionId ID de la notificación
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean marcarComoLeida(int notificacionId) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NOTIFICACION_LEIDA, 1);

            int rows = db.update(
                    DatabaseHelper.TABLE_NOTIFICACIONES,
                    values,
                    DatabaseHelper.COLUMN_NOTIFICACION_ID + " = ?",
                    new String[]{String.valueOf(notificacionId)}
            );
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al marcar notificación como leída: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtiene todas las notificaciones, opcionalmente filtradas por tipo.
     * @param tipo Tipo de notificación (puede ser null para todas)
     * @return Lista de notificaciones
     */
    public List<Notificacion> obtenerNotificaciones(String tipo) {
        List<Notificacion> notificaciones = new ArrayList<>();
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
                     tipo != null ? DatabaseHelper.COLUMN_NOTIFICACION_TIPO + " = ?" : null,
                     tipo != null ? new String[]{tipo} : null,
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
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener notificaciones: " + e.getMessage(), e);
        }
        return notificaciones;
    }
}