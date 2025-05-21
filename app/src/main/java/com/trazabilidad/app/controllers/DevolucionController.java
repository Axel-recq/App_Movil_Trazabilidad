package com.trazabilidad.app.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trazabilidad.app.database.DatabaseHelper;
import com.trazabilidad.app.database.DevolucionDAO;
import com.trazabilidad.app.models.Devolucion;
import com.trazabilidad.app.services.StockService;

import java.util.List;

public class DevolucionController {
    private static final String TAG = "DevolucionController";
    private final DevolucionDAO devolucionDAO;
    private final DatabaseHelper dbHelper;
    private final StockService stockService;

    public DevolucionController(Context context) {
        devolucionDAO = new DevolucionDAO(context);
        dbHelper = DatabaseHelper.getInstance(context);
        stockService = new StockService(context);
    }

    public void registrarDevolucion(Devolucion devolucion, OperacionCallback callback) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Validar datos
            if (!validarDevolucion(devolucion, callback, db)) {
                return;
            }

            // Obtener precio unitario y cantidad actual en pedido_productos
            double precioUnitario = obtenerPrecioUnitario(devolucion.getPedidoId(), devolucion.getProductoId(), db);
            int cantidadActual = obtenerCantidadEnPedido(devolucion.getPedidoId(), devolucion.getProductoId(), db);

            if (devolucion.getCantidad() > cantidadActual) {
                callback.onError("La cantidad a devolver (" + devolucion.getCantidad() + ") excede la cantidad en el pedido (" + cantidadActual + ")");
                return;
            }

            // Actualizar cantidad en pedido_productos o eliminar entrada
            if (devolucion.getCantidad() == cantidadActual) {
                // Eliminar la entrada si se devuelve toda la cantidad
                int rowsDeleted = db.delete(
                        DatabaseHelper.TABLE_PEDIDO_PRODUCTOS,
                        DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID + " = ? AND " +
                                DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRODUCTO_ID + " = ?",
                        new String[]{String.valueOf(devolucion.getPedidoId()), String.valueOf(devolucion.getProductoId())}
                );
                if (rowsDeleted == 0) {
                    callback.onError("Error al eliminar la relación pedido-producto");
                    return;
                }
            } else {
                // Reducir la cantidad
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_CANTIDAD, cantidadActual - devolucion.getCantidad());
                int rowsUpdated = db.update(
                        DatabaseHelper.TABLE_PEDIDO_PRODUCTOS,
                        values,
                        DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID + " = ? AND " +
                                DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRODUCTO_ID + " = ?",
                        new String[]{String.valueOf(devolucion.getPedidoId()), String.valueOf(devolucion.getProductoId())}
                );
                if (rowsUpdated == 0) {
                    callback.onError("Error al actualizar la cantidad en pedido-producto");
                    return;
                }
            }

            // Actualizar stock usando StockService
            stockService.aumentarStock(devolucion.getProductoId(), devolucion.getCantidad(), new StockService.OperacionStockCallback() {
                @Override
                public void onSuccess(int nuevoStock, boolean bajoStock) {
                    try {
                        // Actualizar el total del pedido
                        double subtotal = precioUnitario * devolucion.getCantidad();
                        actualizarTotalPedido(devolucion.getPedidoId(), -subtotal, db);

                        // Registrar la devolución
                        boolean resultado = devolucionDAO.insertarDevolucion(devolucion);
                        if (!resultado) {
                            callback.onError("Error al registrar la devolución en la base de datos");
                            return;
                        }

                        // Verificar si el pedido quedó sin productos
                        if (pedidoSinProductos(devolucion.getPedidoId(), db)) {
                            ContentValues pedidoValues = new ContentValues();
                            pedidoValues.put(DatabaseHelper.COLUMN_PEDIDO_ESTADO, "DEVUELTO");
                            db.update(
                                    DatabaseHelper.TABLE_PEDIDOS,
                                    pedidoValues,
                                    DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                                    new String[]{String.valueOf(devolucion.getPedidoId())}
                            );
                        }

                        db.setTransactionSuccessful();
                        callback.onSuccess();
                    } catch (Exception e) {
                        Log.e(TAG, "Error al completar la devolución: " + e.getMessage());
                        callback.onError("Error interno: " + e.getMessage());
                    }
                }

                @Override
                public void onError(String message) {
                    callback.onError("Error al actualizar el stock: " + message);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al registrar devolución: " + e.getMessage());
            callback.onError("Error interno: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void obtenerDevolucionesPorPedido(int pedidoId, DevolucionesCallback callback) {
        try {
            List<Devolucion> devoluciones = devolucionDAO.obtenerDevolucionesPorPedido(pedidoId);
            callback.onSuccess(devoluciones);
        } catch (Exception e) {
            callback.onError("Error al obtener las devoluciones: " + e.getMessage());
        }
    }

    private boolean validarDevolucion(Devolucion devolucion, OperacionCallback callback, SQLiteDatabase db) {
        // Validar campos obligatorios
        if (devolucion.getPedidoId() <= 0) {
            callback.onError("ID de pedido inválido");
            return false;
        }
        if (devolucion.getProductoId() <= 0) {
            callback.onError("ID de producto inválido");
            return false;
        }
        if (devolucion.getCantidad() <= 0) {
            callback.onError("La cantidad debe ser mayor que cero");
            return false;
        }
        if (devolucion.getMotivo() == null || devolucion.getMotivo().trim().isEmpty()) {
            callback.onError("El motivo es requerido");
            return false;
        }
        if (devolucion.getFecha() == null) {
            callback.onError("La fecha es requerida");
            return false;
        }
        if (devolucion.getUsuarioId() <= 0) {
            callback.onError("ID de usuario inválido");
            return false;
        }

        // Verificar que el pedido existe
        String queryPedido = "SELECT " + DatabaseHelper.COLUMN_PEDIDO_ID + " FROM " + DatabaseHelper.TABLE_PEDIDOS +
                " WHERE " + DatabaseHelper.COLUMN_PEDIDO_ID + " = ?";
        try (Cursor cursor = db.rawQuery(queryPedido, new String[]{String.valueOf(devolucion.getPedidoId())})) {
            if (!cursor.moveToFirst()) {
                callback.onError("El pedido no existe");
                return false;
            }
        }

        // Verificar que el producto está asociado al pedido
        String queryProducto = "SELECT " + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_ID + " FROM " + DatabaseHelper.TABLE_PEDIDO_PRODUCTOS +
                " WHERE " + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID + " = ? AND " +
                DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRODUCTO_ID + " = ?";
        try (Cursor cursor = db.rawQuery(queryProducto, new String[]{
                String.valueOf(devolucion.getPedidoId()),
                String.valueOf(devolucion.getProductoId())
        })) {
            if (!cursor.moveToFirst()) {
                callback.onError("El producto no está asociado al pedido");
                return false;
            }
        }

        // Verificar que el usuario existe
        String queryUsuario = "SELECT " + DatabaseHelper.COLUMN_USUARIO_ID + " FROM " + DatabaseHelper.TABLE_USUARIOS +
                " WHERE " + DatabaseHelper.COLUMN_USUARIO_ID + " = ?";
        try (Cursor cursor = db.rawQuery(queryUsuario, new String[]{String.valueOf(devolucion.getUsuarioId())})) {
            if (!cursor.moveToFirst()) {
                callback.onError("El usuario no existe");
                return false;
            }
        }

        return true;
    }

    private double obtenerPrecioUnitario(int pedidoId, int productoId, SQLiteDatabase db) {
        String query = "SELECT " + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRECIO_UNITARIO + " FROM " +
                DatabaseHelper.TABLE_PEDIDO_PRODUCTOS +
                " WHERE " + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID + " = ? AND " +
                DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRODUCTO_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(pedidoId),
                String.valueOf(productoId)
        })) {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRECIO_UNITARIO));
            }
            throw new IllegalStateException("No se encontró precio unitario para pedidoId: " + pedidoId + ", productoId: " + productoId);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener precio unitario: " + e.getMessage());
            throw e;
        }
    }

    private int obtenerCantidadEnPedido(int pedidoId, int productoId, SQLiteDatabase db) {
        String query = "SELECT " + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_CANTIDAD + " FROM " +
                DatabaseHelper.TABLE_PEDIDO_PRODUCTOS +
                " WHERE " + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID + " = ? AND " +
                DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRODUCTO_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(pedidoId),
                String.valueOf(productoId)
        })) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_CANTIDAD));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener cantidad en pedido: " + e.getMessage());
        }
        return 0;
    }

    private void actualizarTotalPedido(int pedidoId, double subtotal, SQLiteDatabase db) {
        try {
            // Obtener el total actual
            String query = "SELECT " + DatabaseHelper.COLUMN_PEDIDO_TOTAL + " FROM " + DatabaseHelper.TABLE_PEDIDOS +
                    " WHERE " + DatabaseHelper.COLUMN_PEDIDO_ID + " = ?";
            double currentTotal = 0;
            try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(pedidoId)})) {
                if (cursor.moveToFirst()) {
                    currentTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_TOTAL));
                }
            }

            // Calcular el nuevo total
            double newTotal = Math.max(0, currentTotal + subtotal); // Evitar totales negativos

            // Actualizar el total
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PEDIDO_TOTAL, newTotal);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PEDIDOS,
                    values,
                    DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                    new String[]{String.valueOf(pedidoId)}
            );
            if (rowsAffected == 0) {
                Log.e(TAG, "Error al actualizar el total del pedido ID: " + pedidoId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar total del pedido: " + e.getMessage());
        }
    }

    private boolean pedidoSinProductos(int pedidoId, SQLiteDatabase db) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PEDIDO_PRODUCTOS +
                " WHERE " + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(pedidoId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar productos en pedido: " + e.getMessage());
        }
        return false;
    }

    public interface OperacionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface DevolucionesCallback {
        void onSuccess(List<Devolucion> devoluciones);
        void onError(String message);
    }
}