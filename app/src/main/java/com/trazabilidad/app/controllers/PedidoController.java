package com.trazabilidad.app.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trazabilidad.app.database.DatabaseHelper;
import com.trazabilidad.app.database.IncidenciaDAO;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.database.ProductoDAO;
import com.trazabilidad.app.models.Incidencia;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Producto;
import com.trazabilidad.app.models.Ubicacion;
import com.trazabilidad.app.services.APIService;
import com.trazabilidad.app.services.StockService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PedidoController {
    private static final String TAG = "PedidoController";
    private PedidoDAO pedidoDAO;
    private ProductoDAO productoDAO;
    private IncidenciaDAO incidenciaDAO;
    private GPSController gpsController;
    private DatabaseHelper dbHelper;
    private APIService apiService;
    private StockService stockService;
    private Context context;

    public PedidoController(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
        pedidoDAO = new PedidoDAO(context);
        productoDAO = new ProductoDAO(context);
        incidenciaDAO = new IncidenciaDAO(context);
        gpsController = new GPSController(context, 0);
        apiService = new APIService(context);
        stockService = new StockService(context);
    }

    private boolean isNetworkAvailable() {
        return apiService.isNetworkAvailable();
    }

    public void registrarPedido(Pedido pedido, List<Producto> productos, OperacionCallback callback) {
        try {
            if (productos == null || productos.isEmpty()) {
                callback.onError("No se puede crear un pedido sin productos");
                return;
            }

            verificarStockDisponible(productos, new VerificacionStockCallback() {
                @Override
                public void onStockVerificado(boolean stockSuficiente, String mensaje) {
                    if (!stockSuficiente) {
                        callback.onError(mensaje);
                        return;
                    }

                    boolean pedidoInsertado = pedidoDAO.insertarPedido(pedido);
                    if (!pedidoInsertado) {
                        callback.onError("Error al insertar el pedido en la base de datos");
                        return;
                    }

                    int pedidoId = obtenerUltimoPedidoId();
                    if (pedidoId == -1) {
                        callback.onError("No se pudo obtener el ID del pedido recién creado");
                        return;
                    }

                    final AtomicBoolean todosProductosInsertados = new AtomicBoolean(true);
                    final AtomicInteger productosCompletados = new AtomicInteger(0);
                    final int totalProductos = productos.size();

                    for (Producto producto : productos) {
                        int productoId = productoDAO.obtenerIdProductoPorCodigo(producto.getCodigo());
                        if (productoId == -1) {
                            boolean productoInsertado = productoDAO.insertarProducto(producto);
                            if (!productoInsertado) {
                                todosProductosInsertados.set(false);
                                Log.e(TAG, "Error al insertar el producto: " + producto.getNombre());
                                callback.onError("Error al insertar el producto: " + producto.getNombre());
                                return;
                            }
                            productoId = productoDAO.obtenerUltimoProductoId();
                        }

                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID, pedidoId);
                        values.put(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRODUCTO_ID, productoId);
                        values.put(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_CANTIDAD, producto.getCantidad());

                        Producto productoDB = productoDAO.obtenerProductoPorId(productoId);
                        if (productoDB == null) {
                            todosProductosInsertados.set(false);
                            Log.e(TAG, "Error: No se encontró el producto con ID: " + productoId);
                            callback.onError("Error: Producto no encontrado para el ID: " + productoId);
                            return;
                        }
                        values.put(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRECIO_UNITARIO, productoDB.getPrecio());

                        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                            long id = db.insert(DatabaseHelper.TABLE_PEDIDO_PRODUCTOS, null, values);
                            if (id == -1) {
                                todosProductosInsertados.set(false);
                                Log.e(TAG, "Error al asociar el producto al pedido: " + producto.getNombre());
                                continue;
                            }
                        }

                        double subtotal = productoDB.getPrecio() * producto.getCantidad();
                        actualizarTotalPedido(pedidoId, subtotal);

                        final int cantidadADescontar = producto.getCantidad();
                        final int productoGeneralId = obtenerIdProductoGeneral(producto.getCodigo());

                        if (productoGeneralId > 0) {
                            stockService.descontarStock(productoGeneralId, cantidadADescontar,
                                    new StockService.OperacionStockCallback() {
                                        @Override
                                        public void onSuccess(int nuevoStock, boolean bajoStock) {
                                            if (productosCompletados.incrementAndGet() == totalProductos) {
                                                if (todosProductosInsertados.get()) {
                                                    callback.onSuccess();
                                                } else {
                                                    callback.onError("Hubo errores al insertar algunos productos del pedido");
                                                }
                                            }
                                        }

                                        @Override
                                        public void onError(String message) {
                                            todosProductosInsertados.set(false);
                                            Log.e(TAG, "Error al descontar stock: " + message);
                                            if (productosCompletados.incrementAndGet() == totalProductos) {
                                                callback.onError("Hubo errores al descontar el stock de algunos productos");
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "No se encontró producto general para el código: " + producto.getCodigo());
                            productosCompletados.incrementAndGet();
                        }
                    }

                    if (totalProductos == 0) {
                        callback.onSuccess();
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Error al registrar el pedido y productos: " + e.getMessage());
        }
    }

    private void actualizarTotalPedido(int pedidoId, double subtotalToAdd) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            String query = "SELECT " + DatabaseHelper.COLUMN_PEDIDO_TOTAL + " FROM " + DatabaseHelper.TABLE_PEDIDOS +
                    " WHERE " + DatabaseHelper.COLUMN_PEDIDO_ID + " = ?";
            double currentTotal = 0;
            try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(pedidoId)})) {
                if (cursor.moveToFirst()) {
                    currentTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_TOTAL));
                }
            }

            double newTotal = currentTotal + subtotalToAdd;

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
            Log.e(TAG, "Error al actualizar el total del pedido: " + e.getMessage());
        }
    }

    private void verificarStockDisponible(List<Producto> productos, VerificacionStockCallback callback) {
        if (productos == null || productos.isEmpty()) {
            callback.onStockVerificado(true, "");
            return;
        }

        final AtomicInteger productosVerificados = new AtomicInteger(0);
        final int totalProductos = productos.size();
        final List<String> productosSinStock = new ArrayList<>();

        for (Producto productoPedido : productos) {
            int productoGeneralId = obtenerIdProductoGeneral(productoPedido.getCodigo());

            if (productoGeneralId <= 0) {
                String error = "No se encontró el producto con código: " + productoPedido.getCodigo();
                productosSinStock.add(error);
                Log.e(TAG, error);

                if (productosVerificados.incrementAndGet() == totalProductos) {
                    finalizarVerificacion(productosSinStock, callback);
                }
                continue;
            }

            ProductoController productoController = new ProductoController(this.context);
            productoController.obtenerProductoPorId(productoGeneralId, new ProductoController.ProductoCallback() {
                @Override
                public void onSuccess(Producto productoGeneral) {
                    if (!productoGeneral.isActivo()) {
                        productosSinStock.add("El producto " + productoGeneral.getNombre() + " está inactivo");
                    } else if (productoGeneral.getCantidad() < productoPedido.getCantidad()) {
                        productosSinStock.add("Stock insuficiente para " + productoGeneral.getNombre() +
                                ". Disponible: " + productoGeneral.getCantidad() +
                                ", Requerido: " + productoPedido.getCantidad());
                    }

                    if (productosVerificados.incrementAndGet() == totalProductos) {
                        finalizarVerificacion(productosSinStock, callback);
                    }
                }

                @Override
                public void onError(String message) {
                    productosSinStock.add("Error al verificar stock: " + message);

                    if (productosVerificados.incrementAndGet() == totalProductos) {
                        finalizarVerificacion(productosSinStock, callback);
                    }
                }
            });
        }
    }

    private void finalizarVerificacion(List<String> productosSinStock, VerificacionStockCallback callback) {
        if (productosSinStock.isEmpty()) {
            callback.onStockVerificado(true, "");
        } else {
            StringBuilder mensaje = new StringBuilder("No se puede crear el pedido debido a:\n");
            for (String error : productosSinStock) {
                mensaje.append("- ").append(error).append("\n");
            }
            callback.onStockVerificado(false, mensaje.toString());
        }
    }

    private int obtenerIdProductoGeneral(String codigoProducto) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PRODUCTOS,
                     new String[]{DatabaseHelper.COLUMN_PRODUCTO_ID},
                     DatabaseHelper.COLUMN_PRODUCTO_CODIGO + " = ? AND " +
                             DatabaseHelper.COLUMN_PRODUCTO_ACTIVO + " = 1",
                     new String[]{codigoProducto},
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ID));
            }
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Error al buscar producto general: " + e.getMessage());
            return -1;
        }
    }

    public void obtenerPedidosAsignados(int usuarioId, PedidosCallback callback) {
        try {
            List<Pedido> pedidos = pedidoDAO.obtenerPedidosPorUsuario(usuarioId);
            callback.onSuccess(pedidos);
        } catch (Exception e) {
            callback.onError("Error al obtener los pedidos: " + e.getMessage());
        }
    }

    public void obtenerDetallePedido(int pedidoId, PedidoDetalleCallback callback) {
        try {
            Pedido pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
            if (pedido == null) {
                callback.onError("Pedido no encontrado");
                return;
            }
            List<Producto> productos = productoDAO.obtenerProductosPorPedido(pedidoId);
            callback.onSuccess(pedido, productos);
        } catch (Exception e) {
            callback.onError("Error al obtener detalle del pedido: " + e.getMessage());
        }
    }

    public void marcarPedidoComoEntregado(int pedidoId, OperacionCallback callback) {
        try {
            Pedido pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
            if (pedido == null) {
                callback.onError("Pedido no encontrado");
                return;
            }
            pedido.setEstado("ENTREGADO");
            boolean resultado = pedidoDAO.actualizarPedido(pedido);
            if (resultado) {
                gpsController.obtenerUbicacionActual(new GPSController.UbicacionCallback() {
                    @Override
                    public void onUbicacionObtenida(double latitud, double longitud) {
                        Ubicacion ubicacion = new Ubicacion();
                        ubicacion.setLatitud(latitud);
                        ubicacion.setLongitud(longitud);
                        ubicacion.setFecha(new Date());
                        ubicacion.setUsuarioId(pedido.getUsuarioId());
                        ubicacion.setPedidoId(pedidoId);
                        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                            ContentValues values = new ContentValues();
                            values.put(DatabaseHelper.COLUMN_UBICACION_LATITUD, ubicacion.getLatitud());
                            values.put(DatabaseHelper.COLUMN_UBICACION_LONGITUD, ubicacion.getLongitud());
                            values.put(DatabaseHelper.COLUMN_UBICACION_FECHA, ubicacion.getFecha().getTime());
                            values.put(DatabaseHelper.COLUMN_UBICACION_USUARIO_ID, ubicacion.getUsuarioId());
                            values.put(DatabaseHelper.COLUMN_UBICACION_PEDIDO_ID, ubicacion.getPedidoId());
                            db.insert(DatabaseHelper.TABLE_UBICACIONES, null, values);
                        }
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String message) {
                        callback.onSuccess();
                    }
                });
            } else {
                callback.onError("Error al actualizar el estado del pedido");
            }
        } catch (Exception e) {
            callback.onError("Error al marcar pedido como entregado: " + e.getMessage());
        }
    }

    public void actualizarEstadoPedido(int pedidoId, String nuevoEstado, OperacionCallback callback) {
        try {
            Pedido pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
            if (pedido == null) {
                callback.onError("Pedido no encontrado");
                return;
            }

            if (!esTransicionValida(pedido.getEstado(), nuevoEstado)) {
                callback.onError("Transición de estado no válida desde " + pedido.getEstado() + " a " + nuevoEstado);
                return;
            }

            boolean resultado = pedidoDAO.actualizarEstado(pedidoId, nuevoEstado);
            if (resultado) {
                if ("ENTREGADO".equals(nuevoEstado)) {
                    gpsController.obtenerUbicacionActual(new GPSController.UbicacionCallback() {
                        @Override
                        public void onUbicacionObtenida(double latitud, double longitud) {
                            Ubicacion ubicacion = new Ubicacion();
                            ubicacion.setLatitud(latitud);
                            ubicacion.setLongitud(longitud);
                            ubicacion.setFecha(new Date());
                            ubicacion.setUsuarioId(pedido.getUsuarioId());
                            ubicacion.setPedidoId(pedidoId);
                            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                                ContentValues values = new ContentValues();
                                values.put(DatabaseHelper.COLUMN_UBICACION_LATITUD, ubicacion.getLatitud());
                                values.put(DatabaseHelper.COLUMN_UBICACION_LONGITUD, ubicacion.getLongitud());
                                values.put(DatabaseHelper.COLUMN_UBICACION_FECHA, ubicacion.getFecha().getTime());
                                values.put(DatabaseHelper.COLUMN_UBICACION_USUARIO_ID, ubicacion.getUsuarioId());
                                values.put(DatabaseHelper.COLUMN_UBICACION_PEDIDO_ID, ubicacion.getPedidoId());
                                db.insert(DatabaseHelper.TABLE_UBICACIONES, null, values);
                            }
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(String message) {
                            callback.onSuccess();
                        }
                    });
                } else {
                    callback.onSuccess();
                }
            } else {
                callback.onError("Error al actualizar el estado del pedido");
            }
        } catch (Exception e) {
            callback.onError("Error al actualizar estado del pedido: " + e.getMessage());
        }
    }

    public void registrarIncidencia(Incidencia incidencia, OperacionCallback callback) {
        try {
            boolean resultado = incidenciaDAO.insertarIncidencia(incidencia);
            if (resultado) {
                if (isNetworkAvailable()) {
                    apiService.registrarIncidencia(incidencia, new APIService.APICallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(String message) {
                            callback.onSuccess();
                        }
                    });
                } else {
                    callback.onSuccess();
                }

                if (incidencia.getPedidoId() > 0) {
                    Pedido pedido = pedidoDAO.obtenerPedidoPorId(incidencia.getPedidoId());
                    if (pedido != null) {
                        pedido.setEstado("INCIDENCIA");
                        pedidoDAO.actualizarPedido(pedido);
                    }
                }
            } else {
                callback.onError("Error al registrar la incidencia");
            }
        } catch (Exception e) {
            callback.onError("Error al registrar incidencia: " + e.getMessage());
        }
    }

    private int obtenerUltimoPedidoId() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT MAX(" + DatabaseHelper.COLUMN_PEDIDO_ID + ") FROM " + DatabaseHelper.TABLE_PEDIDOS, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public void cancelarPedido(int pedidoId, OperacionCallback callback) {
        try {
            Pedido pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
            if (pedido == null) {
                callback.onError("Pedido no encontrado");
                return;
            }

            if ("ENTREGADO".equals(pedido.getEstado())) {
                callback.onError("No se puede cancelar un pedido ya entregado");
                return;
            }

            List<Producto> productosPedido = productoDAO.obtenerProductosPorPedido(pedidoId);
            if (productosPedido.isEmpty()) {
                pedido.setEstado("CANCELADO");
                boolean resultado = pedidoDAO.actualizarPedido(pedido);
                if (resultado) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al cancelar el pedido");
                }
                return;
            }

            final AtomicInteger productosCompletados = new AtomicInteger(0);
            final int totalProductos = productosPedido.size();
            final AtomicBoolean todosProductosDevueltos = new AtomicBoolean(true);

            for (Producto productoPedido : productosPedido) {
                int productoGeneralId = obtenerIdProductoGeneral(productoPedido.getCodigo());
                if (productoGeneralId > 0) {
                    double precioUnitario = obtenerPrecioUnitarioPedido(productoGeneralId, pedidoId);
                    double subtotal = precioUnitario * productoPedido.getCantidad();

                    stockService.aumentarStock(productoGeneralId, productoPedido.getCantidad(),
                            new StockService.OperacionStockCallback() {
                                @Override
                                public void onSuccess(int nuevoStock, boolean bajoStock) {
                                    actualizarTotalPedido(pedidoId, -subtotal);
                                    if (productosCompletados.incrementAndGet() == totalProductos) {
                                        finalizarCancelacionPedido(pedidoId, todosProductosDevueltos.get(), callback);
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    todosProductosDevueltos.set(false);
                                    Log.e(TAG, "Error al devolver stock: " + message);
                                    if (productosCompletados.incrementAndGet() == totalProductos) {
                                        finalizarCancelacionPedido(pedidoId, false, callback);
                                    }
                                }
                            });
                } else {
                    Log.e(TAG, "No se encontró producto general para: " + productoPedido.getCodigo());
                    if (productosCompletados.incrementAndGet() == totalProductos) {
                        finalizarCancelacionPedido(pedidoId, todosProductosDevueltos.get(), callback);
                    }
                }
            }
        } catch (Exception e) {
            callback.onError("Error al cancelar el pedido: " + e.getMessage());
        }
    }

    private double obtenerPrecioUnitarioPedido(int productoId, int pedidoId) {
        String query = "SELECT " + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRECIO_UNITARIO + " " +
                "FROM " + DatabaseHelper.TABLE_PEDIDO_PRODUCTOS + " " +
                "WHERE " + DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRODUCTO_ID + " = ? AND " +
                DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID + " = ?";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(productoId), String.valueOf(pedidoId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRECIO_UNITARIO));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener precio unitario: " + e.getMessage());
        }
        return 0;
    }

    private void finalizarCancelacionPedido(int pedidoId, boolean exitoso, OperacionCallback callback) {
        try {
            Pedido pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
            if (pedido != null) {
                pedido.setEstado("CANCELADO");
                boolean resultado = pedidoDAO.actualizarPedido(pedido);
                if (resultado) {
                    if (exitoso) {
                        callback.onSuccess();
                    } else {
                        callback.onError("El pedido se canceló pero hubo errores al devolver algunos productos al stock");
                    }
                } else {
                    callback.onError("Error al actualizar el estado del pedido a CANCELADO");
                }
            } else {
                callback.onError("Pedido no encontrado al finalizar cancelación");
            }
        } catch (Exception e) {
            callback.onError("Error al finalizar cancelación: " + e.getMessage());
        }
    }

    public boolean actualizarEstado(int pedidoId, String nuevoEstado) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PEDIDO_ESTADO, nuevoEstado);
            if ("ENTREGADO".equals(nuevoEstado)) {
                values.put(DatabaseHelper.COLUMN_PEDIDO_HORA_ENTREGA, System.currentTimeMillis());
                values.put(DatabaseHelper.COLUMN_PEDIDO_CONFIRMADO, 1);
            }
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PEDIDOS,
                    values,
                    DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                    new String[]{String.valueOf(pedidoId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar estado del pedido", e);
            return false;
        }
    }

    private boolean esTransicionValida(String estadoActual, String nuevoEstado) {
        switch (estadoActual) {
            case "PENDIENTE":
                return "ASIGNADO".equals(nuevoEstado);
            case "ASIGNADO":
                return "EN_RUTA".equals(nuevoEstado);
            case "EN_RUTA":
                return "ENTREGADO".equals(nuevoEstado);
            default:
                return false;
        }
    }

    public List<Incidencia> obtenerIncidenciasPorUsuario(int usuarioId) {
        return incidenciaDAO.obtenerIncidenciasPorUsuario(usuarioId);
    }

    public interface VerificacionStockCallback {
        void onStockVerificado(boolean stockSuficiente, String mensaje);
    }

    public interface PedidosCallback {
        void onSuccess(List<Pedido> pedidos);
        void onError(String message);
    }

    public interface PedidoDetalleCallback {
        void onSuccess(Pedido pedido, List<Producto> productos);
        void onError(String message);
    }

    public interface OperacionCallback {
        void onSuccess();
        void onError(String message);
    }
}