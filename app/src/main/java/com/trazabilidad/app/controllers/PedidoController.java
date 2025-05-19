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
        gpsController = new GPSController(context,0);
        apiService = new APIService(context);
        stockService = new StockService(context);
    }

    private boolean isNetworkAvailable() {
        return apiService.isNetworkAvailable();
    }

    /**
     * Registra un nuevo pedido, validando primero el stock disponible y
     * descontándolo si el pedido se crea correctamente
     *
     * @param pedido Pedido a registrar
     * @param productos Lista de productos del pedido
     * @param callback Callback para notificar resultado
     */
    public void registrarPedido(Pedido pedido, List<Producto> productos, OperacionCallback callback) {
        try {
            // Verificar que haya productos
            if (productos == null || productos.isEmpty()) {
                callback.onError("No se puede crear un pedido sin productos");
                return;
            }

            // PASO 1: Verificar stock disponible para todos los productos
            verificarStockDisponible(productos, new VerificacionStockCallback() {
                @Override
                public void onStockVerificado(boolean stockSuficiente, String mensaje) {
                    if (!stockSuficiente) {
                        callback.onError(mensaje);
                        return;
                    }

                    // PASO 2: Insertar el pedido
                    boolean pedidoInsertado = pedidoDAO.insertarPedido(pedido);
                    if (!pedidoInsertado) {
                        callback.onError("Error al insertar el pedido en la base de datos");
                        return;
                    }

                    // PASO 3: Obtener el ID del pedido recién insertado
                    int pedidoId = obtenerUltimoPedidoId();
                    if (pedidoId == -1) {
                        callback.onError("No se pudo obtener el ID del pedido recién creado");
                        return;
                    }

                    // PASO 4: Insertar los productos asociados al pedido en la tabla pedido_productos
                    final AtomicBoolean todosProductosInsertados = new AtomicBoolean(true);
                    final AtomicInteger productosCompletados = new AtomicInteger(0);
                    final int totalProductos = productos.size();

                    for (Producto producto : productos) {
                        // Verificar si el producto ya existe en la base de datos por su código
                        int productoId = productoDAO.obtenerIdProductoPorCodigo(producto.getCodigo());
                        if (productoId == -1) {
                            // Insertar nuevo producto si no existe
                            boolean productoInsertado = productoDAO.insertarProducto(producto);
                            if (!productoInsertado) {
                                todosProductosInsertados.set(false);
                                Log.e(TAG, "Error al insertar el producto: " + producto.getNombre());
                                callback.onError("Error al insertar el producto: " + producto.getNombre());
                                return;
                            }
                            productoId = productoDAO.obtenerUltimoProductoId();
                        }

                        // Insertar la relación en la tabla pedido_productos
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PEDIDO_ID, pedidoId);
                        values.put(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_PRODUCTO_ID, productoId);
                        values.put(DatabaseHelper.COLUMN_PEDIDO_PRODUCTO_CANTIDAD, producto.getCantidad());

                        // Obtener el precio unitario del producto
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

                        // Actualizar el total del pedido
                        double subtotal = productoDB.getPrecio() * producto.getCantidad();
                        actualizarTotalPedido(pedidoId, subtotal);

                        // PASO 5: Descontar stock para este producto
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

                    // Si no hay productos que procesar, llamar al callback directamente
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
            // Obtener el total actual
            String query = "SELECT " + DatabaseHelper.COLUMN_PEDIDO_TOTAL + " FROM " + DatabaseHelper.TABLE_PEDIDOS +
                    " WHERE " + DatabaseHelper.COLUMN_PEDIDO_ID + " = ?";
            double currentTotal = 0;
            try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(pedidoId)})) {
                if (cursor.moveToFirst()) {
                    currentTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_TOTAL));
                }
            }

            // Sumar el nuevo subtotal
            double newTotal = currentTotal + subtotalToAdd;

            // Actualizar el total en la tabla pedidos
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
    /**
     * Verifica si hay stock suficiente para todos los productos de un pedido
     */
    private void verificarStockDisponible(List<Producto> productos, VerificacionStockCallback callback) {
        if (productos == null || productos.isEmpty()) {
            callback.onStockVerificado(true, "");
            return;
        }

        final AtomicInteger productosVerificados = new AtomicInteger(0);
        final int totalProductos = productos.size();
        final List<String> productosSinStock = new ArrayList<>();

        for (Producto productoPedido : productos) {
            // Obtenemos el ID del producto general (inventario) basado en el código
            int productoGeneralId = obtenerIdProductoGeneral(productoPedido.getCodigo());

            if (productoGeneralId <= 0) {
                String error = "No se encontró el producto con código: " + productoPedido.getCodigo();
                productosSinStock.add(error);
                Log.e(TAG, error);

                // Verificar si ya procesamos todos los productos
                if (productosVerificados.incrementAndGet() == totalProductos) {
                    finalizarVerificacion(productosSinStock, callback);
                }
                continue;
            }

            // Consultar el stock actual del producto en el inventario
            ProductoController productoController = new ProductoController(this.context);
            productoController.obtenerProductoPorId(productoGeneralId, new ProductoController.ProductoCallback() {
                @Override
                public void onSuccess(Producto productoGeneral) {
                    // Verificar si el producto está activo
                    if (!productoGeneral.isActivo()) {
                        productosSinStock.add("El producto " + productoGeneral.getNombre() + " está inactivo");
                    }
                    // Verificar si hay suficiente stock
                    else if (productoGeneral.getCantidad() < productoPedido.getCantidad()) {
                        productosSinStock.add("Stock insuficiente para " + productoGeneral.getNombre() +
                                ". Disponible: " + productoGeneral.getCantidad() +
                                ", Requerido: " + productoPedido.getCantidad());
                    }

                    // Verificar si ya procesamos todos los productos
                    if (productosVerificados.incrementAndGet() == totalProductos) {
                        finalizarVerificacion(productosSinStock, callback);
                    }
                }

                @Override
                public void onError(String message) {
                    productosSinStock.add("Error al verificar stock: " + message);

                    // Verificar si ya procesamos todos los productos
                    if (productosVerificados.incrementAndGet() == totalProductos) {
                        finalizarVerificacion(productosSinStock, callback);
                    }
                }
            });
        }
    }

    /**
     * Finaliza el proceso de verificación de stock
     */
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

    /**
     * Obtiene el ID del producto en el inventario general basado en su código
     */
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
            return -1; // No encontrado
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
                        // Guardar ubicación en la base de datos local
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
                        callback.onSuccess(); // Continuar aunque no se obtenga la ubicación
                    }
                });
            } else {
                callback.onError("Error al actualizar el estado del pedido");
            }
        } catch (Exception e) {
            callback.onError("Error al marcar pedido como entregado: " + e.getMessage());
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
                            callback.onSuccess(); // Éxito local, pero pendiente de sincronización
                        }
                    });
                } else {
                    callback.onSuccess(); // Solo local, sincronizar después
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

    // Método auxiliar para obtener el ID del último pedido insertado
    private int obtenerUltimoPedidoId() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT MAX(" + DatabaseHelper.COLUMN_PEDIDO_ID + ") FROM " + DatabaseHelper.TABLE_PEDIDOS, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return -1; // Indica un error si no se encuentra
        } catch (Exception e) {
            return -1; // Indica un error en caso de excepción
        }
    }

    /**
     * Métodos para cancelar un pedido y devolver los productos al stock
     */
    public void cancelarPedido(int pedidoId, OperacionCallback callback) {
        try {
            // Obtener el pedido
            Pedido pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
            if (pedido == null) {
                callback.onError("Pedido no encontrado");
                return;
            }

            // Verificar que el pedido no esté ya entregado
            if ("ENTREGADO".equals(pedido.getEstado())) {
                callback.onError("No se puede cancelar un pedido ya entregado");
                return;
            }

            // Obtener los productos del pedido
            List<Producto> productosPedido = productoDAO.obtenerProductosPorPedido(pedidoId);
            if (productosPedido.isEmpty()) {
                // Cambiar estado y terminar
                pedido.setEstado("CANCELADO");
                boolean resultado = pedidoDAO.actualizarPedido(pedido);
                if (resultado) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al cancelar el pedido");
                }
                return;
            }

            // Devolver stock de cada producto
            final AtomicInteger productosCompletados = new AtomicInteger(0);
            final int totalProductos = productosPedido.size();
            final AtomicBoolean todosProductosDevueltos = new AtomicBoolean(true);

            for (Producto productoPedido : productosPedido) {
                int productoGeneralId = obtenerIdProductoGeneral(productoPedido.getCodigo());
                if (productoGeneralId > 0) {
                    // Obtener precio_unitario de pedido_productos
                    double precioUnitario = obtenerPrecioUnitarioPedido(productoGeneralId, pedidoId);
                    double subtotal = precioUnitario * productoPedido.getCantidad();

                    stockService.aumentarStock(productoGeneralId, productoPedido.getCantidad(),
                            new StockService.OperacionStockCallback() {
                                @Override
                                public void onSuccess(int nuevoStock, boolean bajoStock) {
                                    // Restar el subtotal del total del pedido
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

    public List<Incidencia> obtenerIncidenciasPorUsuario(int usuarioId) {
        return incidenciaDAO.obtenerIncidenciasPorUsuario(usuarioId);
    }

    /**
     * Interface para la verificación de stock
     */
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