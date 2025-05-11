package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.models.Devolucion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DevolucionDAO {

    private final DatabaseHelper dbHelper;

    // Constructor que inicializa el DatabaseHelper
    public DevolucionDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Método para insertar una devolución
    public boolean insertarDevolucion(Devolucion devolucion) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_PEDIDO_ID, devolucion.getPedidoId());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_PRODUCTO_ID, devolucion.getProductoId());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_CANTIDAD, devolucion.getCantidad());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_MOTIVO, devolucion.getMotivo());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_FECHA, devolucion.getFecha().getTime());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_USUARIO_ID, devolucion.getUsuarioId());
            long id = db.insert(DatabaseHelper.TABLE_DEVOLUCIONES, null, values);
            return id != -1; // Retorna true si la inserción fue exitosa
        } catch (Exception e) {
            return false; // Retorna false si ocurre un error
        }
    }

    // Método para obtener devoluciones por pedido
    public List<Devolucion> obtenerDevolucionesPorPedido(int pedidoId) {
        List<Devolucion> devoluciones = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_DEVOLUCIONES,
                     getDevolucionColumns(),
                     DatabaseHelper.COLUMN_DEVOLUCION_PEDIDO_ID + " = ?",
                     new String[]{String.valueOf(pedidoId)},
                     null,
                     null,
                     DatabaseHelper.COLUMN_DEVOLUCION_FECHA + " ASC"
             )) {
            while (cursor.moveToNext()) {
                devoluciones.add(cursorToDevolucion(cursor));
            }
        } catch (Exception e) {
            // Manejo básico de errores, podría mejorarse según necesidades
        }
        return devoluciones;
    }

    // Método auxiliar para obtener las columnas de la tabla de devoluciones
    private String[] getDevolucionColumns() {
        return new String[]{
                DatabaseHelper.COLUMN_DEVOLUCION_ID,
                DatabaseHelper.COLUMN_DEVOLUCION_PEDIDO_ID,
                DatabaseHelper.COLUMN_DEVOLUCION_PRODUCTO_ID,
                DatabaseHelper.COLUMN_DEVOLUCION_CANTIDAD,
                DatabaseHelper.COLUMN_DEVOLUCION_MOTIVO,
                DatabaseHelper.COLUMN_DEVOLUCION_FECHA,
                DatabaseHelper.COLUMN_DEVOLUCION_USUARIO_ID
        };
    }

    // Método auxiliar para convertir un Cursor en un objeto Devolucion
    private Devolucion cursorToDevolucion(Cursor cursor) {
        Devolucion devolucion = new Devolucion();
        devolucion.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEVOLUCION_ID)));
        devolucion.setPedidoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEVOLUCION_PEDIDO_ID)));
        devolucion.setProductoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEVOLUCION_PRODUCTO_ID)));
        devolucion.setCantidad(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEVOLUCION_CANTIDAD)));
        devolucion.setMotivo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEVOLUCION_MOTIVO)));
        long fechaMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEVOLUCION_FECHA));
        devolucion.setFecha(new Date(fechaMillis));
        devolucion.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEVOLUCION_USUARIO_ID)));
        return devolucion;
    }
}