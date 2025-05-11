package com.trazabilidad.app.models;

import java.io.Serializable;
import java.util.Date;

public class Devolucion implements Serializable {

    private int id;
    private int pedidoId;
    private int productoId;
    private int cantidad;
    private String motivo;
    private Date fecha;
    private int usuarioId;

    public Devolucion() {
    }

    public Devolucion(int id, int pedidoId, int productoId, int cantidad, String motivo, Date fecha, int usuarioId) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fecha = fecha;
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    @Override
    public String toString() {
        return "Devolucion{" +
                "id=" + id +
                ", pedidoId=" + pedidoId +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", motivo='" + motivo + '\'' +
                ", fecha=" + fecha +
                ", usuarioId=" + usuarioId +
                '}';
    }
}