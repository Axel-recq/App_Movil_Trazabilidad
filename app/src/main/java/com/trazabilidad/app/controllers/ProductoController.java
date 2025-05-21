package com.trazabilidad.app.controllers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.database.DatabaseHelper;
import com.trazabilidad.app.database.ProductoDAO;
import com.trazabilidad.app.models.Producto;

import java.util.ArrayList;
import java.util.List;

public class ProductoController {

    private final ProductoDAO productoDAO;
    public static final int STOCK_BAJO_LIMITE = 10;
    private final DatabaseHelper dbHelper;
    public ProductoController(Context context) {
        productoDAO = new ProductoDAO(context);
        productoDAO.actualizarEstructuraTabla();
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Método mejorado para insertar producto general, asegurando que no se asocie a ningún pedido
    public void insertarProductoGeneral(Producto producto, OperacionCallback callback) {
        try {
            // Validaciones
            if (producto.getCodigo() == null || producto.getCodigo().isEmpty()) {
                callback.onError("El código del producto es requerido");
                return;
            }
            if (producto.getNombre() == null || producto.getNombre().isEmpty()) {
                callback.onError("El nombre del producto es requerido");
                return;
            }
            if (producto.getPrecio() <= 0) {
                callback.onError("El precio debe ser mayor que cero");
                return;
            }
            if (producto.getCantidad() < 0) {
                callback.onError("La cantidad no puede ser negativa");
                return;
            }

            // Asegurarse de que el producto esté activo por defecto
            producto.setActivo(true);

            // Llamada al DAO para insertar
            boolean resultado = productoDAO.insertarProducto(producto);
            if (resultado) {
                // Verificar stock bajo después de insertar
                if (producto.getCantidad() < STOCK_BAJO_LIMITE) {
                    callback.onSuccess();
                    notificarStockBajo(producto);
                } else {
                    callback.onSuccess();
                }
            } else {
                callback.onError("Error al insertar el producto en la base de datos");
            }
        } catch (Exception e) {
            callback.onError("Error al insertar el producto: " + e.getMessage());
        }
    }

    public void actualizarProducto(Producto producto, OperacionCallback callback) {
        try {
            // Validaciones
            if (producto.getId() <= 0) {
                callback.onError("ID de producto inválido");
                return;
            }
            if (producto.getCodigo() == null || producto.getCodigo().isEmpty()) {
                callback.onError("El código del producto es requerido");
                return;
            }
            if (producto.getNombre() == null || producto.getNombre().isEmpty()) {
                callback.onError("El nombre del producto es requerido");
                return;
            }
            if (producto.getPrecio() <= 0) {
                callback.onError("El precio debe ser mayor que cero");
                return;
            }
            if (producto.getCantidad() < 0) {
                callback.onError("La cantidad no puede ser negativa");
                return;
            }

            // Llamada al DAO para actualizar
            boolean resultado = productoDAO.actualizarProducto(producto);
            if (resultado) {
                // Verificar stock bajo después de actualizar
                if (producto.getCantidad() < STOCK_BAJO_LIMITE) {
                    callback.onSuccess();
                    notificarStockBajo(producto);
                } else {
                    callback.onSuccess();
                }
            } else {
                callback.onError("Error al actualizar el producto");
            }
        } catch (Exception e) {
            callback.onError("Error al actualizar el producto: " + e.getMessage());
        }
    }

    // Método para desactivar un producto
    public void desactivarProducto(int productoId, OperacionCallback callback) {
        try {
            boolean resultado = productoDAO.desactivarProducto(productoId);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al desactivar el producto");
            }
        } catch (Exception e) {
            callback.onError("Error al desactivar el producto: " + e.getMessage());
        }
    }

    // Método para activar un producto
    public void activarProducto(int productoId, OperacionCallback callback) {
        try {
            boolean resultado = productoDAO.activarProducto(productoId);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al activar el producto");
            }
        } catch (Exception e) {
            callback.onError("Error al activar el producto: " + e.getMessage());
        }
    }

    // Método para descontar stock de un producto
    public void descontarStock(int productoId, int cantidad, OperacionStockCallback callback) {
        try {
            if (cantidad <= 0) {
                callback.onError("La cantidad a descontar debe ser mayor que cero");
                return;
            }

            Producto producto = productoDAO.obtenerProductoPorId(productoId);
            if (producto == null) {
                callback.onError("Producto no encontrado");
                return;
            }

            if (!producto.isActivo()) {
                callback.onError("No se puede usar un producto inactivo");
                return;
            }

            if (producto.getCantidad() < cantidad) {
                callback.onError("No hay suficiente stock disponible");
                return;
            }

            // Descontar stock
            int nuevoStock = producto.getCantidad() - cantidad;
            producto.setCantidad(nuevoStock);

            // Actualizar producto
            boolean resultado = productoDAO.actualizarProducto(producto);
            if (resultado) {
                boolean bajoStock = nuevoStock < STOCK_BAJO_LIMITE;
                if (bajoStock) {
                    notificarStockBajo(producto);
                }
                callback.onSuccess(nuevoStock, bajoStock);
            } else {
                callback.onError("Error al actualizar el stock");
            }
        } catch (Exception e) {
            callback.onError("Error al descontar stock: " + e.getMessage());
        }
    }

    // Método para aumentar stock de un producto
    public void aumentarStock(int productoId, int cantidad, OperacionStockCallback callback) {
        try {
            if (cantidad <= 0) {
                callback.onError("La cantidad a aumentar debe ser mayor que cero");
                return;
            }

            Producto producto = productoDAO.obtenerProductoPorId(productoId);
            if (producto == null) {
                callback.onError("Producto no encontrado");
                return;
            }

            // Aumentar stock
            int nuevoStock = producto.getCantidad() + cantidad;
            producto.setCantidad(nuevoStock);

            // Actualizar producto
            boolean resultado = productoDAO.actualizarProducto(producto);
            if (resultado) {
                callback.onSuccess(nuevoStock, nuevoStock < STOCK_BAJO_LIMITE);
            } else {
                callback.onError("Error al actualizar el stock");
            }
        } catch (Exception e) {
            callback.onError("Error al aumentar stock: " + e.getMessage());
        }
    }

    // Método para notificar stock bajo (implementar según necesidad)
    private void notificarStockBajo(Producto producto) {
        // Aquí se podría implementar una notificación push, guardar en tabla de alertas, etc.
        // Por ahora solo imprimimos en log
        android.util.Log.w("StockBajo", "ALERTA: Producto " + producto.getNombre() +
                " tiene stock bajo (" + producto.getCantidad() + ")");
    }

    // Obtener todos los productos (incluidos activos e inactivos)
    public void obtenerTodosLosProductos(ProductosCallback callback) {
        try {
            List<Producto> productos = productoDAO.obtenerTodosLosProductosOrdenadosPorId();
            callback.onSuccess(productos);
        } catch (Exception e) {
            callback.onError("Error al obtener todos los productos: " + e.getMessage());
        }
    }
    public void obtenerProductosPorPedido(int pedidoId, ProductosCallback callback) {
        try {
            List<Producto> productos = productoDAO.obtenerProductosPorPedido(pedidoId);
            if (productos != null && !productos.isEmpty()) {
                callback.onSuccess(productos);
            } else {
                callback.onSuccess(new ArrayList<>()); // Devolver lista vacía en lugar de error
            }
        } catch (Exception e) {
            callback.onError("Error al obtener productos del pedido: " + e.getMessage());
        }
    }
    // Obtener solo los productos activos
    public void obtenerProductosActivos(ProductosCallback callback) {
        try {
            List<Producto> productos = productoDAO.obtenerProductosActivos();
            callback.onSuccess(productos);
        } catch (Exception e) {
            callback.onError("Error al obtener productos activos: " + e.getMessage());
        }
    }

    // Obtener solo los productos con stock bajo
    public void obtenerProductosBajoStock(ProductosCallback callback) {
        List<Producto> productos = new ArrayList<>();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_PRODUCTOS +
                " WHERE " + DatabaseHelper.COLUMN_PRODUCTO_CANTIDAD + " < ? AND " +
                DatabaseHelper.COLUMN_PRODUCTO_ACTIVO + " = ?";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{"10", "1"})) {
            while (cursor.moveToNext()) {
                Producto producto = new Producto();
                producto.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ID)));
                producto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE)));
                producto.setCantidad(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_CANTIDAD)));
                producto.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ACTIVO)) == 1);
                productos.add(producto);
            }
            callback.onSuccess(productos);
        } catch (Exception e) {
            callback.onError("Error al obtener productos con bajo stock: " + e.getMessage());
        }
    }

    public void obtenerProductoPorId(int productoId, ProductoCallback callback) {
        try {
            Producto producto = productoDAO.obtenerProductoPorId(productoId);
            if (producto != null) {
                callback.onSuccess(producto);
            } else {
                callback.onError("Producto no encontrado");
            }
        } catch (Exception e) {
            callback.onError("Error al obtener el producto: " + e.getMessage());
        }
    }
    public void obtenerProductoPorId(int id, SQLiteDatabase db, ProductoCallback callback) {
        Producto producto = productoDAO.obtenerProductoPorId(id, db);
        if (producto != null) {
            callback.onSuccess(producto);
        } else {
            callback.onError("Producto no encontrado");
        }
    }

    public void actualizarProducto(Producto producto, SQLiteDatabase db, OperacionCallback callback) {
        boolean resultado = productoDAO.actualizarProducto(producto, db);
        if (resultado) {
            callback.onSuccess();
        } else {
            callback.onError("Error al actualizar el producto");
        }
    }
    // Interfaz para operaciones de stock
    public interface OperacionStockCallback {
        void onSuccess(int nuevoStock, boolean bajoStock);
        void onError(String message);
    }

    // Interfaz para notificar éxito o error en operaciones sin retorno de datos
    public interface OperacionCallback {
        void onSuccess();
        void onError(String message);
    }

    // Interfaz para notificar éxito o error en operaciones que devuelven un producto
    public interface ProductoCallback {
        void onSuccess(Producto producto);
        void onError(String message);
    }

    // Interfaz para notificar éxito o error en operaciones que devuelven una lista de productos
    public interface ProductosCallback {
        void onSuccess(List<Producto> productos);
        void onError(String message);
    }
}