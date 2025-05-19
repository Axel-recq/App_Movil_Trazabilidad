package com.trazabilidad.app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Producto implements Parcelable {
    private int id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private int cantidad;
    private boolean activo;

    // Constructor por defecto
    public Producto() {
        this.activo = true;
    }

    // Constructor con parámetros principales
    public Producto(String codigo, String nombre, String descripcion, double precio, int cantidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.activo = true;
    }

    // Constructor con todos los parámetros
    public Producto(int id, String codigo, String nombre, String descripcion, double precio, int cantidad, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.activo = activo;
    }

    // Constructor para leer desde un Parcel
    protected Producto(Parcel in) {
        id = in.readInt();
        codigo = in.readString();
        nombre = in.readString();
        descripcion = in.readString();
        precio = in.readDouble();
        cantidad = in.readInt();
        activo = in.readByte() != 0;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.precio = precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Implementación de Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(codigo);
        dest.writeString(nombre);
        dest.writeString(descripcion);
        dest.writeDouble(precio);
        dest.writeInt(cantidad);
        dest.writeByte((byte) (activo ? 1 : 0));
    }

    public static final Creator<Producto> CREATOR = new Creator<Producto>() {
        @Override
        public Producto createFromParcel(Parcel in) {
            return new Producto(in);
        }

        @Override
        public Producto[] newArray(int size) {
            return new Producto[size];
        }
    };

    @Override
    public String toString() {
        return nombre;
    }
}