package com.trazabilidad.app.models;

import java.io.Serializable;
import java.util.Date;

public class Incidencia implements Serializable {

    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_RESUELTO = "resuelto";

    private int id;
    private String tipo;
    private String descripcion;
    private Date fecha;
    private String fotoUri;
    private int usuarioId;
    private Integer pedidoId; // Cambiado a Integer
    private String estado;

    public Incidencia() {
    }

    public Incidencia(int id, String tipo, String descripcion, Date fecha,
                      String fotoUri, int usuarioId, Integer pedidoId, String estado) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.fotoUri = fotoUri;
        this.usuarioId = usuarioId;
        this.pedidoId = pedidoId;
        this.estado = estado;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo no puede ser nulo o vacío");
        }
        this.tipo = tipo;
    }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede ser nula o vacía");
        }
        this.descripcion = descripcion;
    }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        this.fecha = fecha;
    }
    public String getFotoUri() { return fotoUri; }
    public void setFotoUri(String fotoUri) { this.fotoUri = fotoUri; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("El usuarioId debe ser mayor que 0");
        }
        this.usuarioId = usuarioId;
    }
    public Integer getPedidoId() { return pedidoId; }
    public void setPedidoId(Integer pedidoId) { this.pedidoId = pedidoId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado no puede ser nulo o vacío");
        }
        this.estado = estado;
    }

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