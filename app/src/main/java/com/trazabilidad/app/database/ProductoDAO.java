package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.models.Producto;

import java.util.ArrayList;
import java.util.List;


public class ProductoDAO {

    private final DatabaseHelper dbHelper;

    public ProductoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }
    public boolean insertarProducto(Producto producto) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getProductoContentValues(producto);
            long id = db.insert(DatabaseHelper.TABLE_PRODUCTOS, null, values);
            return id != -1;
        } catch (Exception e) {

            return false;
        }
    }

    public boolean actualizarProducto(Producto producto) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getProductoContentValues(producto);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTOS,
                    values,
                    DatabaseHelper.COLUMN_PRODUCTO_ID + " = ?",
                    new String[]{String.valueOf(producto.getId())}
            );
            return rowsAffected > 0;
        } catch (Exception e) {

            return false;
        }
    }

    public boolean eliminarProducto(int id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            int rowsAffected = db.delete(
                    DatabaseHelper.TABLE_PRODUCTOS,
                    DatabaseHelper.COLUMN_PRODUCTO_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {

            return false;
        }
    }

    public Producto obtenerProductoPorId(int id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PRODUCTOS,
                     getProductoColumns(),
                     DatabaseHelper.COLUMN_PRODUCTO_ID + " = ?",
                     new String[]{String.valueOf(id)},
                     null,
                     null,
                     null
             )) {
            if (cursor.moveToFirst()) {
                return cursorToProducto(cursor);
            }
            return null;
        } catch (Exception e) {

            return null;
        }
    }

    public List<Producto> obtenerProductosPorPedido(int pedidoId) {
        List<Producto> productos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PRODUCTOS,
                     getProductoColumns(),
                     DatabaseHelper.COLUMN_PRODUCTO_PEDIDO_ID + " = ?",
                     new String[]{String.valueOf(pedidoId)},
                     null,
                     null,
                     DatabaseHelper.COLUMN_PRODUCTO_NOMBRE + " ASC"
             )) {
            while (cursor.moveToNext()) {
                productos.add(cursorToProducto(cursor));
            }
        } catch (Exception e) {

        }
        return productos;
    }

    private ContentValues getProductoContentValues(Producto producto) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRODUCTO_CODIGO, producto.getCodigo());
        values.put(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE, producto.getNombre());
        values.put(DatabaseHelper.COLUMN_PRODUCTO_DESCRIPCION, producto.getDescripcion());
        values.put(DatabaseHelper.COLUMN_PRODUCTO_PRECIO, producto.getPrecio());
        values.put(DatabaseHelper.COLUMN_PRODUCTO_CANTIDAD, producto.getCantidad());
        values.put(DatabaseHelper.COLUMN_PRODUCTO_PEDIDO_ID, producto.getPedidoId());
        return values;
    }

    private String[] getProductoColumns() {
        return new String[]{
                DatabaseHelper.COLUMN_PRODUCTO_ID,
                DatabaseHelper.COLUMN_PRODUCTO_CODIGO,
                DatabaseHelper.COLUMN_PRODUCTO_NOMBRE,
                DatabaseHelper.COLUMN_PRODUCTO_DESCRIPCION,
                DatabaseHelper.COLUMN_PRODUCTO_PRECIO,
                DatabaseHelper.COLUMN_PRODUCTO_CANTIDAD,
                DatabaseHelper.COLUMN_PRODUCTO_PEDIDO_ID
        };
    }

    private Producto cursorToProducto(Cursor cursor) {
        Producto producto = new Producto();
        producto.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ID)));
        producto.setCodigo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_CODIGO)));
        producto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE)));
        producto.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_DESCRIPCION)));
        producto.setPrecio(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_PRECIO)));
        producto.setCantidad(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_CANTIDAD)));
        producto.setPedidoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_PEDIDO_ID)));
        return producto;
    }
}