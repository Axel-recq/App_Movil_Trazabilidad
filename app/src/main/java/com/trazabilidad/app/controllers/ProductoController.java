package com.trazabilidad.app.controllers;

import android.content.Context;

import com.trazabilidad.app.database.ProductoDAO;
import com.trazabilidad.app.models.Producto;

import java.util.List;

public class ProductoController {

    private final ProductoDAO productoDAO;

    public ProductoController(Context context) {
        productoDAO = new ProductoDAO(context);
    }

    public void insertarProducto(Producto producto, OperacionCallback callback) {
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
            if (producto.getPedidoId() <= 0) {
                callback.onError("ID de pedido inválido");
                return;
            }

            // Llamada al DAO para insertar
            boolean resultado = productoDAO.insertarProducto(producto);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al insertar el producto en la base de datos");
            }
        } catch (Exception e) {
            callback.onError("Error al insertar el producto: " + e.getMessage());
        }
    }

    public void actualizarProducto(Producto producto, OperacionCallback callback) {
        try {
            // Validaciones similares a insertarProducto
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
            if (producto.getPedidoId() <= 0) {
                callback.onError("ID de pedido inválido");
                return;
            }

            // Llamada al DAO para actualizar
            boolean resultado = productoDAO.actualizarProducto(producto);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al actualizar el producto");
            }
        } catch (Exception e) {
            callback.onError("Error al actualizar el producto: " + e.getMessage());
        }
    }

    public void eliminarProducto(int productoId, OperacionCallback callback) {
        try {
            boolean resultado = productoDAO.eliminarProducto(productoId);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al eliminar el producto");
            }
        } catch (Exception e) {
            callback.onError("Error al eliminar el producto: " + e.getMessage());
        }
    }
    public void obtenerTodosLosProductos(ProductosCallback callback) {
        try {
            List<Producto> productos = productoDAO.obtenerTodosLosProductos();
            callback.onSuccess(productos);
        } catch (Exception e) {
            callback.onError("Error al obtener todos los productos: " + e.getMessage());
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

    public void obtenerProductosPorPedido(int pedidoId, ProductosCallback callback) {
        try {
            List<Producto> productos = productoDAO.obtenerProductosPorPedido(pedidoId);
            callback.onSuccess(productos);
        } catch (Exception e) {
            callback.onError("Error al obtener los productos: " + e.getMessage());
        }
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