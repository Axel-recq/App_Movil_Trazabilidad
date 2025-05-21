package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trazabilidad.app.models.Producto;

import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "ProductoDAO";

    public ProductoDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public void actualizarEstructuraTabla() {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            // Verificar si la columna activo existe en la tabla productos
            boolean columnaActivoExiste = false;
            try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + DatabaseHelper.TABLE_PRODUCTOS + ")", null)) {
                while (cursor.moveToNext()) {
                    String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    if ("activo".equals(columnName)) {
                        columnaActivoExiste = true;
                        break;
                    }
                }
            }

            // Si la columna no existe, añadirla
            if (!columnaActivoExiste) {
                db.execSQL("ALTER TABLE " + DatabaseHelper.TABLE_PRODUCTOS + " ADD COLUMN " +
                        DatabaseHelper.COLUMN_PRODUCTO_ACTIVO + " INTEGER DEFAULT 1");
                Log.d(TAG, "Columna activo añadida a la tabla productos");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar estructura de la tabla: " + e.getMessage());
        }
    }

    public boolean insertarProducto(Producto producto) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getProductoContentValues(producto);
            long id = db.insert(DatabaseHelper.TABLE_PRODUCTOS, null, values);
            return id != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar producto: " + e.getMessage());
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
            Log.e(TAG, "Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean desactivarProducto(int id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PRODUCTO_ACTIVO, 0);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTOS,
                    values,
                    DatabaseHelper.COLUMN_PRODUCTO_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al desactivar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarProducto(Producto producto, SQLiteDatabase db) {
        try {
            ContentValues values = getProductoContentValues(producto);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTOS,
                    values,
                    DatabaseHelper.COLUMN_PRODUCTO_ID + " = ?",
                    new String[]{String.valueOf(producto.getId())}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar producto: " + e.getMessage());
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
            Log.e(TAG, "Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }
    public Producto obtenerProductoPorId(int id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            return obtenerProductoPorId(id, db);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener producto por ID: " + e.getMessage());
            return null;
        }
    }
    public Producto obtenerProductoPorId(int id, SQLiteDatabase db) {
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_PRODUCTOS +
                " WHERE " + DatabaseHelper.COLUMN_PRODUCTO_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)})) {
            if (cursor.moveToFirst()) {
                Producto producto = new Producto();
                producto.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ID)));
                producto.setCodigo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_CODIGO)));
                producto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE)));
                producto.setCantidad(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_CANTIDAD)));
                producto.setPrecio(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_PRECIO)));
                producto.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ACTIVO)) == 1);
                return producto;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al consultar producto por ID: " + e.getMessage());
        }
        return null;
    }
    public boolean activarProducto(int id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PRODUCTO_ACTIVO, 1);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTOS,
                    values,
                    DatabaseHelper.COLUMN_PRODUCTO_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al activar producto: " + e.getMessage());
            return false;
        }
    }
    public List<Producto> obtenerTodosLosProductosOrdenadosPorId() {
        List<Producto> productos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PRODUCTOS,
                     getProductoColumns(),
                     null,
                     null,
                     null,
                     null,
                     DatabaseHelper.COLUMN_PRODUCTO_ID + " DESC"
             )) {
            while (cursor.moveToNext()) {
                productos.add(cursorToProducto(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener todos los productos: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> obtenerProductosActivos() {
        List<Producto> productos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PRODUCTOS,
                     getProductoColumns(),
                     DatabaseHelper.COLUMN_PRODUCTO_ACTIVO + " = 1",
                     null,
                     null,
                     null,
                     DatabaseHelper.COLUMN_PRODUCTO_ID + " DESC"
             )) {
            while (cursor.moveToNext()) {
                productos.add(cursorToProducto(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener productos activos: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> obtenerProductosInactivos() {
        List<Producto> productos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PRODUCTOS,
                     getProductoColumns(),
                     DatabaseHelper.COLUMN_PRODUCTO_ACTIVO + " = 0",
                     null,
                     null,
                     null,
                     DatabaseHelper.COLUMN_PRODUCTO_ID + " DESC"
             )) {
            while (cursor.moveToNext()) {
                productos.add(cursorToProducto(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener productos inactivos: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> obtenerProductosBajoStock(int limiteStockBajo) {
        List<Producto> productos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PRODUCTOS,
                     getProductoColumns(),
                     DatabaseHelper.COLUMN_PRODUCTO_CANTIDAD + " < ? AND " +
                             DatabaseHelper.COLUMN_PRODUCTO_ACTIVO + " = 1",
                     new String[]{String.valueOf(limiteStockBajo)},
                     null,
                     null,
                     DatabaseHelper.COLUMN_PRODUCTO_ID + " DESC"
             )) {
            while (cursor.moveToNext()) {
                productos.add(cursorToProducto(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener productos con stock bajo: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> obtenerProductosPorTexto(String texto) {
        List<Producto> productos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String busqueda = "%" + texto.toLowerCase() + "%";
            String sql = "SELECT * FROM " + DatabaseHelper.TABLE_PRODUCTOS +
                    " WHERE LOWER(" + DatabaseHelper.COLUMN_PRODUCTO_NOMBRE + ") LIKE ? OR " +
                    "LOWER(" + DatabaseHelper.COLUMN_PRODUCTO_CODIGO + ") LIKE ? OR " +
                    "LOWER(" + DatabaseHelper.COLUMN_PRODUCTO_DESCRIPCION + ") LIKE ? " +
                    "ORDER BY " + DatabaseHelper.COLUMN_PRODUCTO_ID + " DESC";

            try (Cursor cursor = db.rawQuery(sql, new String[]{busqueda, busqueda, busqueda})) {
                while (cursor.moveToNext()) {
                    productos.add(cursorToProducto(cursor));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al buscar productos por texto: " + e.getMessage());
        }
        return productos;
    }

    public int obtenerIdProductoPorCodigo(String codigo) {
        int id = -1;
        String query = "SELECT " + DatabaseHelper.COLUMN_PRODUCTO_ID + " FROM " + DatabaseHelper.TABLE_PRODUCTOS +
                " WHERE " + DatabaseHelper.COLUMN_PRODUCTO_CODIGO + " = ?";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{codigo})) {
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener ID del producto por código: " + e.getMessage());
        }
        return id;
    }

    public int obtenerUltimoProductoId() {
        int id = -1;
        String query = "SELECT MAX(" + DatabaseHelper.COLUMN_PRODUCTO_ID + ") FROM " + DatabaseHelper.TABLE_PRODUCTOS;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener el último ID del producto: " + e.getMessage());
        }
        return id;
    }

    public List<Producto> obtenerProductosPorPedido(int pedidoId) {
        List<Producto> productos = new ArrayList<>();
        String query = "SELECT p.*, pp." + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_CANTIDAD + " AS cantidad_pedido, " +
                "pp." + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRECIO_UNITARIO + " " +
                "FROM " + DatabaseHelper.TABLE_PRODUCTOS + " p " +
                "INNER JOIN " + DatabaseHelper.TABLE_PEDIDO_PRODUCTOS + " pp " +
                "ON p." + DatabaseHelper.COLUMN_PRODUCTO_ID + " = pp." + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRODUCTO_ID + " " +
                "WHERE pp." + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID + " = ?";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(pedidoId)})) {
            while (cursor.moveToNext()) {
                Producto producto = cursorToProducto(cursor);
                producto.setCantidad(cursor.getInt(cursor.getColumnIndexOrThrow("cantidad_pedido")));
                producto.setPrecio(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRECIO_UNITARIO)));
                Log.d(TAG, "Loaded product ID " + producto.getId() + " with price: " + producto.getPrecio());
                productos.add(producto);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener productos por pedido: " + e.getMessage());
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
        values.put(DatabaseHelper.COLUMN_PRODUCTO_ACTIVO, producto.isActivo() ? 1 : 0);
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
                DatabaseHelper.COLUMN_PRODUCTO_ACTIVO
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
        int activoColumnIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ACTIVO);
        producto.setActivo(cursor.getInt(activoColumnIndex) == 1);
        return producto;
    }
}