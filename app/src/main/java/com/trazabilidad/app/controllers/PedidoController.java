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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PedidoController {
    private static final String TAG = "PedidoController";
    private PedidoDAO pedidoDAO;
    private ProductoDAO productoDAO;
    private IncidenciaDAO incidenciaDAO;
    private GPSController gpsController;
    private DatabaseHelper dbHelper;
    private APIService apiService;

    public PedidoController(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        pedidoDAO = new PedidoDAO(context);
        productoDAO = new ProductoDAO(context);
        incidenciaDAO = new IncidenciaDAO(context);
        gpsController = new GPSController(context,0);
        apiService = new APIService(context);
    }
    private boolean isNetworkAvailable() {
        return apiService.isNetworkAvailable();
    }
    public void registrarPedido(Pedido pedido, List<Producto> productos, OperacionCallback callback) {
        try {
            // Insertar el pedido
            boolean pedidoInsertado = pedidoDAO.insertarPedido(pedido);
            if (!pedidoInsertado) {
                callback.onError("Error al insertar el pedido en la base de datos");
                return;
            }

            // Obtener el ID del pedido recién insertado
            int pedidoId = obtenerUltimoPedidoId();
            if (pedidoId == -1) {
                callback.onError("No se pudo obtener el ID del pedido recién creado");
                return;
            }

            // Insertar los productos asociados al pedido (si hay productos en la lista)
            if (productos != null && !productos.isEmpty()) {
                for (Producto producto : productos) {
                    producto.setPedidoId(pedidoId); // Asociar el producto al pedido
                    boolean productoInsertado = productoDAO.insertarProducto(producto);
                    if (!productoInsertado) {
                        callback.onError("Error al insertar el producto: " + producto.getNombre());
                        return;
                    }
                }
            }

            callback.onSuccess();
        } catch (Exception e) {
            callback.onError("Error al registrar el pedido y productos: " + e.getMessage());
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
    public List<Incidencia> obtenerIncidenciasPorUsuario(int usuarioId) {
        return incidenciaDAO.obtenerIncidenciasPorUsuario(usuarioId);
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