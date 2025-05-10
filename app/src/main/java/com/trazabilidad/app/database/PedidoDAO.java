package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.models.Pedido;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PedidoDAO {

    private final DatabaseHelper dbHelper;

    public PedidoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public PedidoDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Pedido> getRecentPedidos(int limit) {
        List<Pedido> pedidos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PEDIDOS,
                     getPedidoColumns(),
                     null,
                     null,
                     null,
                     null,
                     DatabaseHelper.COLUMN_PEDIDO_FECHA + " DESC",
                     String.valueOf(limit))) {

            while (cursor.moveToNext()) {
                pedidos.add(cursorToPedido(cursor));
            }
        } catch (Exception e) {
            // Loggear el error o manejarlo según sea necesario
        }
        return pedidos;
    }

    // Métodos existentes...
    public boolean insertarPedido(Pedido pedido) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getPedidoContentValues(pedido);
            long id = db.insert(DatabaseHelper.TABLE_PEDIDOS, null, values);
            return id != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean actualizarPedido(Pedido pedido) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getPedidoContentValues(pedido);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PEDIDOS,
                    values,
                    DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                    new String[]{String.valueOf(pedido.getId())}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean eliminarPedido(int id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            int rowsAffected = db.delete(
                    DatabaseHelper.TABLE_PEDIDOS,
                    DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public Pedido obtenerPedidoPorId(int id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PEDIDOS,
                     getPedidoColumns(),
                     DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                     new String[]{String.valueOf(id)},
                     null,
                     null,
                     null
             )) {
            if (cursor.moveToFirst()) {
                return cursorToPedido(cursor);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Pedido> obtenerPedidosPorUsuario(int usuarioId) {
        List<Pedido> pedidos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PEDIDOS,
                     getPedidoColumns(),
                     DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID + " = ?",
                     new String[]{String.valueOf(usuarioId)},
                     null,
                     null,
                     DatabaseHelper.COLUMN_PEDIDO_FECHA + " DESC"
             )) {
            while (cursor.moveToNext()) {
                pedidos.add(cursorToPedido(cursor));
            }
        } catch (Exception e) {
            // Loggear el error o manejarlo según sea necesario
        }
        return pedidos;
    }
    public List<Pedido> obtenerPedidosPorEstado(String estado) {
        List<Pedido> pedidos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PEDIDOS,
                     getPedidoColumns(),
                     DatabaseHelper.COLUMN_PEDIDO_ESTADO + " = ?",
                     new String[]{estado},
                     null,
                     null,
                     DatabaseHelper.COLUMN_PEDIDO_FECHA + " DESC"
             )) {
            while (cursor.moveToNext()) {
                pedidos.add(cursorToPedido(cursor));
            }
        } catch (Exception e) {
            // Loggear el error o manejarlo según sea necesario
        }
        return pedidos;
    }
    private ContentValues getPedidoContentValues(Pedido pedido) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PEDIDO_NUMERO, pedido.getNumero());
        values.put(DatabaseHelper.COLUMN_PEDIDO_CLIENTE, pedido.getCliente());
        values.put(DatabaseHelper.COLUMN_PEDIDO_DIRECCION, pedido.getDireccion());
        values.put(DatabaseHelper.COLUMN_PEDIDO_FECHA, pedido.getFecha().getTime());
        values.put(DatabaseHelper.COLUMN_PEDIDO_ESTADO, pedido.getEstado());
        values.put(DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID, pedido.getUsuarioId());
        values.put(DatabaseHelper.COLUMN_PEDIDO_LATITUD, pedido.getLatitud());
        values.put(DatabaseHelper.COLUMN_PEDIDO_LONGITUD, pedido.getLongitud());
        values.put(DatabaseHelper.COLUMN_PEDIDO_OBSERVACIONES, pedido.getObservaciones());
        return values;
    }

    private String[] getPedidoColumns() {
        return new String[]{
                DatabaseHelper.COLUMN_PEDIDO_ID,
                DatabaseHelper.COLUMN_PEDIDO_NUMERO,
                DatabaseHelper.COLUMN_PEDIDO_CLIENTE,
                DatabaseHelper.COLUMN_PEDIDO_DIRECCION,
                DatabaseHelper.COLUMN_PEDIDO_FECHA,
                DatabaseHelper.COLUMN_PEDIDO_ESTADO,
                DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID,
                DatabaseHelper.COLUMN_PEDIDO_LATITUD,
                DatabaseHelper.COLUMN_PEDIDO_LONGITUD,
                DatabaseHelper.COLUMN_PEDIDO_OBSERVACIONES
        };
    }

    private Pedido cursorToPedido(Cursor cursor) {
        Pedido pedido = new Pedido();
        pedido.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_ID)));
        pedido.setNumero(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_NUMERO)));
        pedido.setCliente(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_CLIENTE)));
        pedido.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_DIRECCION)));
        long fechaMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_FECHA));
        pedido.setFecha(new Date(fechaMillis));
        pedido.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_ESTADO)));
        pedido.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID)));
        pedido.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_LATITUD)));
        pedido.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_LONGITUD)));
        pedido.setObservaciones(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_OBSERVACIONES)));
        return pedido;
    }
}