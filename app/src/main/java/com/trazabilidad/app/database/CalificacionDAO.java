package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trazabilidad.app.models.Calificacion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalificacionDAO {

    private static final String TAG = "CalificacionDAO";
    private final DatabaseHelper dbHelper;
    private final NotificacionDAO notificacionDAO;

    public CalificacionDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        notificacionDAO = new NotificacionDAO(context);
    }

    public boolean insertarCalificacion(Calificacion calificacion) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CALIFICACION_PEDIDO_ID, calificacion.getPedidoId());
            values.put(DatabaseHelper.COLUMN_CALIFICACION_USUARIO_ID, calificacion.getUsuarioId());
            values.put(DatabaseHelper.COLUMN_CALIFICACION_VALOR, calificacion.getValor());
            values.put(DatabaseHelper.COLUMN_CALIFICACION_COMENTARIO, calificacion.getComentario());
            values.put(DatabaseHelper.COLUMN_CALIFICACION_FECHA, calificacion.getFecha().getTime());

            long id = db.insert(DatabaseHelper.TABLE_CALIFICACIONES, null, values);
            if (id == -1) {
                Log.e(TAG, "Error al insertar calificación para pedidoId: " + calificacion.getPedidoId());
                return false;
            }

            String numeroPedido = obtenerNumeroPedido(db, calificacion.getPedidoId());
            if (numeroPedido == null) {
                numeroPedido = "PED-" + calificacion.getPedidoId();
            }
            notificacionDAO.insertarNotificacion(
                    "CALIFICACION",
                    "Calificación recibida para pedido #" + numeroPedido + ": " + calificacion.getValor() + " estrellas",
                    calificacion.getPedidoId(),
                    null
            );

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar calificación: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    public boolean actualizarCalificacion(Calificacion calificacion) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CALIFICACION_VALOR, calificacion.getValor());
            values.put(DatabaseHelper.COLUMN_CALIFICACION_COMENTARIO, calificacion.getComentario());
            values.put(DatabaseHelper.COLUMN_CALIFICACION_FECHA, calificacion.getFecha().getTime());

            int rows = db.update(
                    DatabaseHelper.TABLE_CALIFICACIONES,
                    values,
                    DatabaseHelper.COLUMN_CALIFICACION_ID + " = ?",
                    new String[]{String.valueOf(calificacion.getId())}
            );

            if (rows > 0) {
                String numeroPedido = obtenerNumeroPedido(db, calificacion.getPedidoId());
                if (numeroPedido == null) {
                    numeroPedido = "PED-" + calificacion.getPedidoId();
                }
                notificacionDAO.insertarNotificacion(
                        "CALIFICACION",
                        "Calificación actualizada para pedido #" + numeroPedido + ": " + calificacion.getValor() + " estrellas",
                        calificacion.getPedidoId(),
                        null
                );
            }

            db.setTransactionSuccessful();
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar calificación: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    public Calificacion obtenerCalificacionPorPedido(int pedidoId) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_CALIFICACIONES,
                     getCalificacionColumns(),
                     DatabaseHelper.COLUMN_CALIFICACION_PEDIDO_ID + " = ?",
                     new String[]{String.valueOf(pedidoId)},
                     null,
                     null,
                     null
             )) {
            if (cursor.moveToFirst()) {
                return cursorToCalificacion(cursor);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener calificación para pedidoId: " + pedidoId, e);
            return null;
        }
    }

    public List<Calificacion> obtenerTodasCalificaciones() {
        List<Calificacion> calificaciones = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT c.*, u." + DatabaseHelper.COLUMN_USUARIO_NOMBRE +
                             " FROM " + DatabaseHelper.TABLE_CALIFICACIONES + " c" +
                             " INNER JOIN " + DatabaseHelper.TABLE_USUARIOS + " u ON c." + DatabaseHelper.COLUMN_CALIFICACION_USUARIO_ID + " = u." + DatabaseHelper.COLUMN_USUARIO_ID +
                             " ORDER BY c." + DatabaseHelper.COLUMN_CALIFICACION_FECHA + " DESC",
                     null
             )) {
            while (cursor.moveToNext()) {
                Calificacion calificacion = cursorToCalificacion(cursor);
                calificaciones.add(calificacion);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener todas las calificaciones: " + e.getMessage(), e);
        }
        return calificaciones;
    }

    public boolean existeCalificacion(int pedidoId) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT 1 FROM " + DatabaseHelper.TABLE_CALIFICACIONES +
                             " WHERE " + DatabaseHelper.COLUMN_CALIFICACION_PEDIDO_ID + " = ?",
                     new String[]{String.valueOf(pedidoId)}
             )) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar calificación para pedidoId: " + pedidoId, e);
            return false;
        }
    }

    private String obtenerNumeroPedido(SQLiteDatabase db, int pedidoId) {
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_PEDIDOS,
                new String[]{DatabaseHelper.COLUMN_PEDIDO_NUMERO},
                DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                new String[]{String.valueOf(pedidoId)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_NUMERO));
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener número de pedido: " + e.getMessage(), e);
            return null;
        }
    }

    private String[] getCalificacionColumns() {
        return new String[]{
                DatabaseHelper.COLUMN_CALIFICACION_ID,
                DatabaseHelper.COLUMN_CALIFICACION_PEDIDO_ID,
                DatabaseHelper.COLUMN_CALIFICACION_USUARIO_ID,
                DatabaseHelper.COLUMN_CALIFICACION_VALOR,
                DatabaseHelper.COLUMN_CALIFICACION_COMENTARIO,
                DatabaseHelper.COLUMN_CALIFICACION_FECHA,
                DatabaseHelper.COLUMN_USUARIO_NOMBRE
        };
    }

    private Calificacion cursorToCalificacion(Cursor cursor) {
        Calificacion calificacion = new Calificacion();
        calificacion.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_ID)));
        calificacion.setPedidoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_PEDIDO_ID)));
        calificacion.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_USUARIO_ID)));
        try {
            calificacion.setValor(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_VALOR)));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Columna " + DatabaseHelper.COLUMN_CALIFICACION_VALOR + " no encontrada", e);
            calificacion.setValor(0);
        }
        calificacion.setComentario(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_COMENTARIO)));
        long fechaMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_FECHA));
        calificacion.setFecha(new Date(fechaMillis));
        String nombreCliente = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_NOMBRE));
        calificacion.setNombreCliente(nombreCliente != null ? nombreCliente : "Desconocido");
        return calificacion;
    }
}