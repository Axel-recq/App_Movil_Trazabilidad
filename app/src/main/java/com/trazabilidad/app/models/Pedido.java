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
    private Integer usuarioId;
    private Double latitud;
    private Double longitud;
    private String observaciones;
    private Double total;
    private Long horaSalida;
    private Long horaEstimada;
    private Long horaEntrega;
    private boolean alertaDemora;
    private String motivoDemora;
    private boolean confirmado;

    public Pedido() {
    }

    public Pedido(int id, String numero, String cliente, String direccion, Date fecha, String estado,
                  Integer usuarioId, Double latitud, Double longitud, String observaciones, Double total,
                  Long horaSalida, Long horaEstimada, Long horaEntrega, boolean alertaDemora,
                  String motivoDemora, boolean confirmado) {
        this.id = id;
        this.numero = numero;
        this.cliente = cliente;
        this.direccion = direccion;
        this.fecha = fecha;
        setEstado(estado); // Usa el setter para validación
        this.usuarioId = usuarioId;
        this.latitud = latitud;
        this.longitud = longitud;
        this.observaciones = observaciones;
        this.total = total;
        this.horaSalida = horaSalida;
        this.horaEstimada = horaEstimada;
        this.horaEntrega = horaEntrega;
        this.alertaDemora = alertaDemora;
        this.motivoDemora = motivoDemora;
        this.confirmado = confirmado;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) {
        if (estado != null && !estado.isEmpty()) {
            estado = estado.toUpperCase();
            if (!estado.equals("PENDIENTE") && !estado.equals("EN_PREPARACION") &&
                    !estado.equals("ASIGNADO") && !estado.equals("EN_RUTA") &&
                    !estado.equals("ENTREGADO") && !estado.equals("CANCELADO") &&
                    !estado.equals("RECHAZADO")) {
                throw new IllegalArgumentException("Estado inválido: " + estado);
            }
        }
        this.estado = estado;
    }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Long getHoraSalida() { return horaSalida; }
    public void setHoraSalida(Long horaSalida) { this.horaSalida = horaSalida; }

    public Long getHoraEstimada() { return horaEstimada; }
    public void setHoraEstimada(Long horaEstimada) { this.horaEstimada = horaEstimada; }

    public Long getHoraEntrega() { return horaEntrega; }
    public void setHoraEntrega(Long horaEntrega) { this.horaEntrega = horaEntrega; }

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
                ", total=" + total +
                ", horaSalida=" + horaSalida +
                ", horaEstimada=" + horaEstimada +
                ", horaEntrega=" + horaEntrega +
                ", alertaDemora=" + alertaDemora +
                ", motivoDemora='" + motivoDemora + '\'' +
                ", confirmado=" + confirmado +
                '}';
    }
}