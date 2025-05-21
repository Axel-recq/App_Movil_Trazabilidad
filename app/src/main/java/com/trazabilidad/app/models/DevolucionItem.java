package com.trazabilidad.app.models;

public class DevolucionItem {
    private Producto producto;
    private boolean selected;
    private int cantidad;
    private String motivo;

    public DevolucionItem(Producto producto) {
        this.producto = producto;
        this.selected = false;
        this.cantidad = 0;
        this.motivo = "";
    }

    public Producto getProducto() {
        return producto;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}