package com.trazabilidad.app.models;

import java.io.Serializable;
import java.util.Date;

public class Calificacion implements Serializable {

    private int id;
    private int pedidoId;
    private int usuarioId;
    private int valor;
    private String comentario;
    private Date fecha;
    private String nombreCliente;

    public Calificacion() {
    }

    public Calificacion(int id, int pedidoId, int usuarioId, int valor, String comentario, Date fecha, String nombreCliente) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.valor = valor;
        this.comentario = comentario;
        this.fecha = fecha;
        this.nombreCliente = nombreCliente;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(int pedidoId) {
        if (pedidoId <= 0) {
            throw new IllegalArgumentException("El ID del pedido debe ser mayor a 0");
        }
        this.pedidoId = pedidoId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser mayor a 0");
        }
        this.usuarioId = usuarioId;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        if (valor < 1 || valor > 5) {
            throw new IllegalArgumentException("El valor debe estar entre 1 y 5");
        }
        this.valor = valor;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        if (comentario != null && comentario.length() > 500) {
            throw new IllegalArgumentException("El comentario no puede exceder los 500 caracteres");
        }
        this.comentario = comentario;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        this.fecha = fecha;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    /**
     * Obtiene la categoría de la calificación basada en su valor.
     * @return Categoría como String ("Muy buenas", "Buenas", "Malas")
     */
    public String getCategoria() {
        if (valor == 5) {
            return "Muy buenas";
        } else if (valor >= 3) {
            return "Buenas";
        } else {
            return "Malas";
        }
    }

    @Override
    public String toString() {
        return "Calificacion{" +
                "id=" + id +
                ", pedidoId=" + pedidoId +
                ", usuarioId=" + usuarioId +
                ", valor=" + valor +
                ", comentario='" + comentario + '\'' +
                ", fecha=" + fecha +
                ", nombreCliente='" + nombreCliente + '\'' +
                '}';
    }
}