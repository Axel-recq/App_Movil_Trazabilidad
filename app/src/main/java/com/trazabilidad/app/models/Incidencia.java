package com.trazabilidad.app.models;

import java.io.Serializable;
import java.util.Date;

public class Incidencia implements Serializable {

    private int id;
    private String tipo;
    private String descripcion;
    private Date fecha;
    private String fotoUri;
    private int usuarioId;
    private int pedidoId;
    private String estado;
    public Incidencia() {
    }

    public Incidencia(int id, String tipo, String descripcion, Date fecha,
                      String fotoUri, int usuarioId, int pedidoId, String estado) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.fotoUri = fotoUri;
        this.usuarioId = usuarioId;
        this.pedidoId = pedidoId;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getFotoUri() {
        return fotoUri;
    }

    public void setFotoUri(String fotoUri) {
        this.fotoUri = fotoUri;
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
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    @Override
    public String toString() {
        return "Incidencia{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fecha=" + fecha +
                ", fotoUri='" + fotoUri + '\'' +
                ", usuarioId=" + usuarioId +
                ", pedidoId=" + pedidoId +
                ", estado='" + estado + '\'' +
                '}';
    }
}