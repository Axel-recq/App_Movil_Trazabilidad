package com.trazabilidad.app.models;

import java.io.Serializable;

public class TipoIncidencia implements Serializable {

    private int id;
    private String nombre;

    public TipoIncidencia() {
    }

    public TipoIncidencia(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return "TipoIncidencia{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}