package com.trazabilidad.app.models;

import java.io.Serializable;
import java.util.Date;

public class Calificacion implements Serializable {

    private int id;
    private int pedidoId;
    private int valor; // Ejemplo: 1 a 5
    private String comentario;
    private Date fecha;

    public Calificacion() {
    }

    public Calificacion(int id, int pedidoId, int valor, String comentario, Date fecha) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }
    public int getValor() { return valor; }
    public void setValor(int valor) {
        if (valor < 1 || valor > 5) throw new IllegalArgumentException("El valor debe estar entre 1 y 5");
        this.valor = valor;
    }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    @Override
    public String toString() {
        return "Calificacion{" +
                "id=" + id +
                ", pedidoId=" + pedidoId +
                ", valor=" + valor +
                ", comentario='" + comentario + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}