package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.models.Ubicacion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UbicacionDAO {

    private final DatabaseHelper dbHelper;

    public UbicacionDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean insertarUbicacion(Ubicacion ubicacion) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_UBICACION_PEDIDO_ID, ubicacion.getPedidoId());
            values.put(DatabaseHelper.COLUMN_UBICACION_USUARIO_ID, ubicacion.getUsuarioId());
            values.put(DatabaseHelper.COLUMN_UBICACION_LATITUD, ubicacion.getLatitud());
            values.put(DatabaseHelper.COLUMN_UBICACION_LONGITUD, ubicacion.getLongitud());
            values.put(DatabaseHelper.COLUMN_UBICACION_FECHA, ubicacion.getFecha().getTime());
            long id = db.insert(DatabaseHelper.TABLE_UBICACIONES, null, values);
            return id != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Ubicacion> obtenerUbicacionesPorPedido(int pedidoId) {
        List<Ubicacion> ubicaciones = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_UBICACIONES,
                     getUbicacionColumns(),
                     DatabaseHelper.COLUMN_UBICACION_PEDIDO_ID + " = ?",
                     new String[]{String.valueOf(pedidoId)},
                     null,
                     null,
                     DatabaseHelper.COLUMN_UBICACION_FECHA + " ASC"
             )) {
            while (cursor.moveToNext()) {
                ubicaciones.add(cursorToUbicacion(cursor));
            }
        } catch (Exception e) {
            // Manejar error
        }
        return ubicaciones;
    }
    public List<Ubicacion> obtenerUbicacionesPorUsuario(int usuarioId) {
        List<Ubicacion> ubicaciones = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_UBICACIONES,
                     getUbicacionColumns(),
                     DatabaseHelper.COLUMN_UBICACION_USUARIO_ID + " = ?",
                     new String[]{String.valueOf(usuarioId)},
                     null,
                     null,
                     DatabaseHelper.COLUMN_UBICACION_FECHA + " ASC")) {
            while (cursor.moveToNext()) {
                ubicaciones.add(cursorToUbicacion(cursor));
            }
        } catch (Exception e) {
            // Manejar error si es necesario
        }
        return ubicaciones;
    }

    private ContentValues getUbicacionContentValues(Ubicacion ubicacion) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_UBICACION_LATITUD, ubicacion.getLatitud());
        values.put(DatabaseHelper.COLUMN_UBICACION_LONGITUD, ubicacion.getLongitud());
        values.put(DatabaseHelper.COLUMN_UBICACION_FECHA, ubicacion.getFecha().getTime());
        values.put(DatabaseHelper.COLUMN_UBICACION_USUARIO_ID, ubicacion.getUsuarioId());
        values.put(DatabaseHelper.COLUMN_UBICACION_PEDIDO_ID, ubicacion.getPedidoId());
        return values;
    }

    private String[] getUbicacionColumns() {
        return new String[]{
                DatabaseHelper.COLUMN_UBICACION_ID,
                DatabaseHelper.COLUMN_UBICACION_LATITUD,
                DatabaseHelper.COLUMN_UBICACION_LONGITUD,
                DatabaseHelper.COLUMN_UBICACION_FECHA,
                DatabaseHelper.COLUMN_UBICACION_USUARIO_ID,
                DatabaseHelper.COLUMN_UBICACION_PEDIDO_ID
        };
    }

    private Ubicacion cursorToUbicacion(Cursor cursor) {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION_ID)));
        ubicacion.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION_LATITUD)));
        ubicacion.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION_LONGITUD)));
        long fechaMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION_FECHA));
        ubicacion.setFecha(new Date(fechaMillis));
        ubicacion.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION_USUARIO_ID)));
        ubicacion.setPedidoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION_PEDIDO_ID)));
        return ubicacion;
    }
}