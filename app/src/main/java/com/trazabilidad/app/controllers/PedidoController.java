package com.trazabilidad.app.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.database.DatabaseHelper;
import com.trazabilidad.app.database.IncidenciaDAO;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.database.ProductoDAO;
import com.trazabilidad.app.models.Incidencia;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Producto;
import com.trazabilidad.app.models.Ubicacion;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PedidoController {

    private PedidoDAO pedidoDAO;
    private ProductoDAO productoDAO;
    private IncidenciaDAO incidenciaDAO;
    private GPSController gpsController;
    private DatabaseHelper dbHelper;

    public PedidoController(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        pedidoDAO = new PedidoDAO(context);
        productoDAO = new ProductoDAO(context);
        incidenciaDAO = new IncidenciaDAO(context);
        gpsController = new GPSController(context, 0); // Usar 0 como usuarioId predeterminado
    }

    public void registrarPedido(Pedido pedido, OperacionCallback callback) {
        try {
            boolean resultado = pedidoDAO.insertarPedido(pedido);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al insertar el pedido en la base de datos");
            }
        } catch (Exception e) {
            callback.onError("Error al registrar el pedido: " + e.getMessage());
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
            // Validar estado
            if (!Arrays.asList(Incidencia.ESTADO_PENDIENTE, Incidencia.ESTADO_RESUELTO).contains(incidencia.getEstado())) {
                callback.onError("Estado no válido: " + incidencia.getEstado());
                return;
            }

            // Insertar incidencia localmente
            boolean resultado = incidenciaDAO.insertarIncidencia(incidencia);
            if (resultado) {
                // Actualizar estado del pedido si aplica
                int pedidoId = incidencia.getPedidoId();
                if (pedidoId > 0) {
                    Pedido pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
                    if (pedido != null) {
                        if (Arrays.asList("PENDIENTE", "EN_PROCESO").contains(pedido.getEstado())) {
                            pedido.setEstado("INCIDENCIA");
                            pedidoDAO.actualizarPedido(pedido);
                        } else {
                            callback.onError("No se puede registrar incidencia para pedido en estado: " + pedido.getEstado());
                            return;
                        }
                    }
                }
                callback.onSuccess();
            } else {
                callback.onError("Error al registrar la incidencia en la base de datos");
            }
        } catch (Exception e) {
            callback.onError("Error al registrar incidencia: " + e.getMessage());
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