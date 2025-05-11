package com.trazabilidad.app.models;

import java.io.Serializable;
import java.util.Date;

public class Pedido implements Serializable {

    private int id;
    private String numero;
    private String cliente;
    private String direccion;
    private Date fecha;
    private String estado;
    private int usuarioId;
    private double latitud;
    private double longitud;
    private String observaciones;
    // Campos adicionales
    private long horaSalida;        // Nueva columna
    private long horaEstimada;      // Nueva columna
    private long horaEntrega;       // Nueva columna
    private boolean alertaDemora;   // Nueva columna
    private String motivoDemora;    // Nueva columna
    private boolean confirmado;     // Nueva columna
    public Pedido() {
    }

    public Pedido(int id, String numero, String cliente, String direccion, Date fecha, String estado, int usuarioId, double latitud, double longitud, String observaciones) {
        this.id = id;
        this.numero = numero;
        this.cliente = cliente;
        this.direccion = direccion;
        this.fecha = fecha;
        this.estado = estado;
        this.usuarioId = usuarioId;
        this.latitud = latitud;
        this.longitud = longitud;
        this.observaciones = observaciones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    public long getHoraSalida() { return horaSalida; }
    public void setHoraSalida(long horaSalida) { this.horaSalida = horaSalida; }

    public long getHoraEstimada() { return horaEstimada; }
    public void setHoraEstimada(long horaEstimada) { this.horaEstimada = horaEstimada; }

    public long getHoraEntrega() { return horaEntrega; }
    public void setHoraEntrega(long horaEntrega) { this.horaEntrega = horaEntrega; }

    public boolean isAlertaDemora() { return alertaDemora; }
    public void setAlertaDemora(boolean alertaDemora) { this.alertaDemora = alertaDemora; }

    public String getMotivoDemora() { return motivoDemora; }
    public void setMotivoDemora(String motivoDemora) { this.motivoDemora = motivoDemora; }

    public boolean isConfirmado() { return confirmado; }
    public void setConfirmado(boolean confirmado) { this.confirmado = confirmado; }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", cliente='" + cliente + '\'' +
                ", direccion='" + direccion + '\'' +
                ", fecha=" + fecha +
                ", estado='" + estado + '\'' +
                ", usuarioId=" + usuarioId +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}