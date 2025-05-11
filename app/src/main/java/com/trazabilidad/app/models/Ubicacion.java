package com.trazabilidad.app.models;

import java.io.Serializable;
import java.util.Date;

public class Ubicacion implements Serializable {

    private int id;
    private double latitud;
    private double longitud;
    private Date fecha;
    private int usuarioId;
    private int pedidoId;

    public Ubicacion() {
    }

    public Ubicacion(int id, double latitud, double longitud, Date fecha, int usuarioId, int pedidoId) {
        this.id = id;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fecha = fecha;
        this.usuarioId = usuarioId;
        this.pedidoId = pedidoId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(int pedidoId) {
        this.pedidoId = pedidoId;
    }

    @Override
    public String toString() {
        return "Ubicacion{" +
                "id=" + id +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", fecha=" + fecha +
                ", usuarioId=" + usuarioId +
                ", pedidoId=" + pedidoId +
                '}';
    }
}