package com.trazabilidad.app.controllers;

import android.content.Context;

import com.trazabilidad.app.database.CalificacionDAO;
import com.trazabilidad.app.models.Calificacion;

import java.util.Date;
import java.util.List;

public class CalificacionController {

    private final CalificacionDAO calificacionDAO;

    public CalificacionController(Context context) {
        calificacionDAO = new CalificacionDAO(context);
    }

    /**
     * Inserta una calificación tras validar los datos.
     * @param calificacion Objeto Calificacion a insertar
     * @param callback Callback para notificar éxito o error
     */
    public void insertarCalificacion(Calificacion calificacion, OperacionCallback callback) {
        try {
            // Validaciones mínimas (el modelo ya valida la mayoría)
            if (calificacion.getFecha() == null) {
                calificacion.setFecha(new Date());
            }
            if (calificacionDAO.existeCalificacion(calificacion.getPedidoId())) {
                callback.onError("Este pedido ya ha sido calificado");
                return;
            }

            boolean resultado = calificacionDAO.insertarCalificacion(calificacion);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al insertar la calificación en la base de datos");
            }
        } catch (IllegalArgumentException e) {
            callback.onError(e.getMessage());
        } catch (Exception e) {
            callback.onError("Error al insertar la calificación: " + e.getMessage());
        }
    }

    /**
     * Actualiza una calificación existente.
     * @param calificacion Objeto Calificacion con datos actualizados
     * @param callback Callback para notificar éxito o error
     */
    public void actualizarCalificacion(Calificacion calificacion, OperacionCallback callback) {
        try {
            // Validaciones mínimas
            if (calificacion.getId() <= 0) {
                callback.onError("ID de calificación inválido");
                return;
            }
            if (calificacion.getFecha() == null) {
                calificacion.setFecha(new Date());
            }

            boolean resultado = calificacionDAO.actualizarCalificacion(calificacion);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al actualizar la calificación en la base de datos");
            }
        } catch (IllegalArgumentException e) {
            callback.onError(e.getMessage());
        } catch (Exception e) {
            callback.onError("Error al actualizar la calificación: " + e.getMessage());
        }
    }

    /**
     * Obtiene una calificación por ID de pedido.
     * @param pedidoId ID del pedido asociado
     * @param callback Callback para notificar éxito o error
     */
    public void obtenerCalificacionPorPedido(int pedidoId, CalificacionCallback callback) {
        try {
            if (pedidoId <= 0) {
                callback.onError("ID de pedido inválido");
                return;
            }
            Calificacion calificacion = calificacionDAO.obtenerCalificacionPorPedido(pedidoId);
            if (calificacion != null) {
                callback.onSuccess(calificacion);
            } else {
                callback.onError("Calificación no encontrada para el pedido");
            }
        } catch (Exception e) {
            callback.onError("Error al obtener la calificación: " + e.getMessage());
        }
    }

    /**
     * Obtiene todas las calificaciones.
     * @param callback Callback para notificar éxito o error
     */
    public void obtenerTodasCalificaciones(CalificacionesCallback callback) {
        try {
            List<Calificacion> calificaciones = calificacionDAO.obtenerTodasCalificaciones();
            if (calificaciones.isEmpty()) {
                callback.onError("No hay calificaciones disponibles");
            } else {
                callback.onSuccess(calificaciones);
            }
        } catch (Exception e) {
            callback.onError("Error al obtener las calificaciones: " + e.getMessage());
        }
    }

    public interface OperacionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface CalificacionCallback {
        void onSuccess(Calificacion calificacion);
        void onError(String message);
    }

    public interface CalificacionesCallback {
        void onSuccess(List<Calificacion> calificaciones);
        void onError(String message);
    }
}