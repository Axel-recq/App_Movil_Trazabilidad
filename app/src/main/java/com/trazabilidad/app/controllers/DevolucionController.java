package com.trazabilidad.app.controllers;

import android.content.Context;

import com.trazabilidad.app.database.DevolucionDAO;
import com.trazabilidad.app.models.Devolucion;

import java.util.List;

public class DevolucionController {

    private final DevolucionDAO devolucionDAO;

    public DevolucionController(Context context) {
        devolucionDAO = new DevolucionDAO(context);
    }

    public void registrarDevolucion(Devolucion devolucion, OperacionCallback callback) {
        try {
            // Validar datos básicos
            if (devolucion.getPedidoId() <= 0) {
                callback.onError("ID de pedido inválido");
                return;
            }
            if (devolucion.getProductoId() <= 0) {
                callback.onError("ID de producto inválido");
                return;
            }
            if (devolucion.getCantidad() <= 0) {
                callback.onError("La cantidad debe ser mayor que cero");
                return;
            }
            if (devolucion.getMotivo() == null || devolucion.getMotivo().isEmpty()) {
                callback.onError("El motivo es requerido");
                return;
            }
            if (devolucion.getUsuarioId() <= 0) {
                callback.onError("ID de usuario inválido");
                return;
            }

            // Insertar devolución
            boolean resultado = devolucionDAO.insertarDevolucion(devolucion);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al insertar la devolución en la base de datos");
            }
        } catch (Exception e) {
            callback.onError("Error al registrar la devolución: " + e.getMessage());
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

    public interface OperacionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface DevolucionesCallback {
        void onSuccess(List<Devolucion> devoluciones);
        void onError(String message);
    }
}