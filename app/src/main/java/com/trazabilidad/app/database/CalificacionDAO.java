package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.models.Calificacion;

import java.util.Date;

public class CalificacionDAO {

    private final DatabaseHelper dbHelper;


    public CalificacionDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Método para insertar una calificación
    public boolean insertarCalificacion(Calificacion calificacion) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CALIFICACION_PEDIDO_ID, calificacion.getPedidoId());
            values.put(DatabaseHelper.COLUMN_CALIFICACION_VALOR, calificacion.getValor());
            values.put(DatabaseHelper.COLUMN_CALIFICACION_COMENTARIO, calificacion.getComentario());
            values.put(DatabaseHelper.COLUMN_CALIFICACION_FECHA, calificacion.getFecha().getTime());
            long id = db.insert(DatabaseHelper.TABLE_CALIFICACIONES, null, values);
            return id != -1; // Retorna true si la inserción fue exitosa
        } catch (Exception e) {
            return false; // Retorna false si ocurre un error
        }
    }

    // Método para obtener la calificación por pedido
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
            return null; // No se encontró calificación para el pedido
        } catch (Exception e) {
            return null; // Retorna null en caso de error
        }
    }

    // Método auxiliar para obtener las columnas de la tabla de calificaciones
    private String[] getCalificacionColumns() {
        return new String[]{
                DatabaseHelper.COLUMN_CALIFICACION_ID,
                DatabaseHelper.COLUMN_CALIFICACION_PEDIDO_ID,
                DatabaseHelper.COLUMN_CALIFICACION_VALOR,
                DatabaseHelper.COLUMN_CALIFICACION_COMENTARIO,
                DatabaseHelper.COLUMN_CALIFICACION_FECHA
        };
    }

    // Método auxiliar para convertir un Cursor en un objeto Calificacion
    private Calificacion cursorToCalificacion(Cursor cursor) {
        Calificacion calificacion = new Calificacion();
        calificacion.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_ID)));
        calificacion.setPedidoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_PEDIDO_ID)));
        calificacion.setValor(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_VALOR)));
        calificacion.setComentario(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_COMENTARIO)));
        long fechaMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION_FECHA));
        calificacion.setFecha(new Date(fechaMillis));
        return calificacion;
    }
}