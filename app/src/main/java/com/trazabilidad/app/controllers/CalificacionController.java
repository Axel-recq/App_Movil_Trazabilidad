package com.trazabilidad.app.controllers;

import android.content.Context;

import com.trazabilidad.app.database.CalificacionDAO;
import com.trazabilidad.app.models.Calificacion;

import java.util.Date;

public class CalificacionController {

    private final CalificacionDAO calificacionDAO;

    public CalificacionController(Context context) {
        calificacionDAO = new CalificacionDAO(context);
    }

    /**
     * Inserta una calificación en la base de datos tras validar los datos.
     * @param calificacion Objeto Calificacion a insertar
     * @param callback Callback para notificar éxito o error
     */
    public void insertarCalificacion(Calificacion calificacion, OperacionCallback callback) {
        try {
            // Validaciones
            if (calificacion.getPedidoId() <= 0) {
                callback.onError("ID de pedido inválido");
                return;
            }
            if (calificacion.getValor() < 1 || calificacion.getValor() > 5) {
                callback.onError("La calificación debe estar entre 1 y 5");
                return;
            }
            if (calificacion.getComentario() != null && calificacion.getComentario().length() > 500) {
                callback.onError("El comentario no puede exceder los 500 caracteres");
                return;
            }
            if (calificacion.getFecha() == null) {
                calificacion.setFecha(new Date()); // Fecha actual si no se proporciona
            }

            // Llamada al DAO para insertar
            boolean resultado = calificacionDAO.insertarCalificacion(calificacion);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al insertar la calificación en la base de datos");
            }
        } catch (Exception e) {
            callback.onError("Error al insertar la calificación: " + e.getMessage());
        }
    }

    /**
     * Obtiene una calificación por ID de pedido.
     * @param pedidoId ID del pedido asociado a la calificación
     * @param callback Callback para notificar éxito o error con la calificación encontrada
     */
    public void obtenerCalificacionPorPedido(int pedidoId, CalificacionCallback callback) {
        try {
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

    // Interfaz para notificar éxito o error en operaciones sin retorno de datos
    public interface OperacionCallback {
        void onSuccess();
        void onError(String message);
    }

    // Interfaz para notificar éxito o error en operaciones que devuelven una calificación
    public interface CalificacionCallback {
        void onSuccess(Calificacion calificacion);
        void onError(String message);
    }
}