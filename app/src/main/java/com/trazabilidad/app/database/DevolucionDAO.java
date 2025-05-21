package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trazabilidad.app.models.Devolucion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DevolucionDAO {
    private final DatabaseHelper dbHelper;

    public DevolucionDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean insertarDevolucion(Devolucion devolucion, SQLiteDatabase db) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_PEDIDO_ID, devolucion.getPedidoId());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_PRODUCTO_ID, devolucion.getProductoId());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_CANTIDAD, devolucion.getCantidad());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_MOTIVO, devolucion.getMotivo());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_FECHA, devolucion.getFecha().getTime());
            values.put(DatabaseHelper.COLUMN_DEVOLUCION_USUARIO_ID, devolucion.getUsuarioId());
            long id = db.insert(DatabaseHelper.TABLE_DEVOLUCIONES, null, values);
            return id != -1;
        } catch (Exception e) {
            Log.e("DevolucionDAO", "Error al insertar devolución: " + e.getMessage());
            return false;
        }
    }

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
            // Log error
        }
        return devoluciones;
    }

    public List<Devolucion> obtenerDevolucionesRecientes(long desdeFecha) {
        List<Devolucion> devoluciones = new ArrayList<>();
        String query = "SELECT d.*, p." + DatabaseHelper.COLUMN_PRODUCTO_NOMBRE +
                " FROM " + DatabaseHelper.TABLE_DEVOLUCIONES + " d" +
                " INNER JOIN " + DatabaseHelper.TABLE_PRODUCTOS + " p" +
                " ON d." + DatabaseHelper.COLUMN_DEVOLUCION_PRODUCTO_ID + " = p." + DatabaseHelper.COLUMN_PRODUCTO_ID +
                " WHERE d." + DatabaseHelper.COLUMN_DEVOLUCION_FECHA + " >= ?" +
                " ORDER BY d." + DatabaseHelper.COLUMN_DEVOLUCION_FECHA + " DESC";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(desdeFecha)})) {
            while (cursor.moveToNext()) {
                Devolucion devolucion = cursorToDevolucion(cursor);
                String nombreProducto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE));
                devolucion.setMotivo(devolucion.getMotivo() + " (Producto: " + nombreProducto + ")");
                devoluciones.add(devolucion);
            }
        } catch (Exception e) {
            // Log error
        }
        return devoluciones;
    }

    public List<Devolucion> obtenerTodasDevolucionesConDetalles() {
        List<Devolucion> devoluciones = new ArrayList<>();
        String query = "SELECT d.*, p." + DatabaseHelper.COLUMN_PRODUCTO_NOMBRE + ", " +
                "ped." + DatabaseHelper.COLUMN_PEDIDO_NUMERO + ", " +
                "u." + DatabaseHelper.COLUMN_USUARIO_NOMBRE +
                " FROM " + DatabaseHelper.TABLE_DEVOLUCIONES + " d" +
                " INNER JOIN " + DatabaseHelper.TABLE_PRODUCTOS + " p" +
                " ON d." + DatabaseHelper.COLUMN_DEVOLUCION_PRODUCTO_ID + " = p." + DatabaseHelper.COLUMN_PRODUCTO_ID +
                " INNER JOIN " + DatabaseHelper.TABLE_PEDIDOS + " ped" +
                " ON d." + DatabaseHelper.COLUMN_DEVOLUCION_PEDIDO_ID + " = ped." + DatabaseHelper.COLUMN_PEDIDO_ID +
                " INNER JOIN " + DatabaseHelper.TABLE_USUARIOS + " u" +
                " ON d." + DatabaseHelper.COLUMN_DEVOLUCION_USUARIO_ID + " = u." + DatabaseHelper.COLUMN_USUARIO_ID +
                " ORDER BY d." + DatabaseHelper.COLUMN_DEVOLUCION_FECHA + " DESC";

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                Devolucion devolucion = cursorToDevolucion(cursor);
                String productoNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE));
                String pedidoNumero = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_NUMERO));
                String usuarioNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_NOMBRE));
                devolucion.setMotivo(String.format("%s\n[Pedido: %s, Producto: %s, Registrado por: %s]",
                        devolucion.getMotivo(), pedidoNumero, productoNombre, usuarioNombre));
                devoluciones.add(devolucion);
            }
        } catch (Exception e) {
            // Log error
            throw e;
        }
        return devoluciones;
    }

    public List<Devolucion> obtenerDevolucionesPorPedidoConDetalles(int pedidoId) {
        List<Devolucion> devoluciones = new ArrayList<>();
        String query = "SELECT d.*, p." + DatabaseHelper.COLUMN_PRODUCTO_NOMBRE + ", " +
                "ped." + DatabaseHelper.COLUMN_PEDIDO_NUMERO + ", " +
                "u." + DatabaseHelper.COLUMN_USUARIO_NOMBRE +
                " FROM " + DatabaseHelper.TABLE_DEVOLUCIONES + " d" +
                " INNER JOIN " + DatabaseHelper.TABLE_PRODUCTOS + " p" +
                " ON d." + DatabaseHelper.COLUMN_DEVOLUCION_PRODUCTO_ID + " = p." + DatabaseHelper.COLUMN_PRODUCTO_ID +
                " INNER JOIN " + DatabaseHelper.TABLE_PEDIDOS + " ped" +
                " ON d." + DatabaseHelper.COLUMN_DEVOLUCION_PEDIDO_ID + " = ped." + DatabaseHelper.COLUMN_PEDIDO_ID +
                " INNER JOIN " + DatabaseHelper.TABLE_USUARIOS + " u" +
                " ON d." + DatabaseHelper.COLUMN_DEVOLUCION_USUARIO_ID + " = u." + DatabaseHelper.COLUMN_USUARIO_ID +
                " WHERE d." + DatabaseHelper.COLUMN_DEVOLUCION_PEDIDO_ID + " = ?" +
                " ORDER BY d." + DatabaseHelper.COLUMN_DEVOLUCION_FECHA + " DESC";

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(pedidoId)})) {
            while (cursor.moveToNext()) {
                Devolucion devolucion = cursorToDevolucion(cursor);
                String productoNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE));
                String pedidoNumero = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_NUMERO));
                String usuarioNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_NOMBRE));
                devolucion.setMotivo(String.format("%s\n[Pedido: %s, Producto: %s, Registrado por: %s]",
                        devolucion.getMotivo(), pedidoNumero, productoNombre, usuarioNombre));
                devoluciones.add(devolucion);
            }
        } catch (Exception e) {
            // Log error
            throw e;
        }
        return devoluciones;
    }

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