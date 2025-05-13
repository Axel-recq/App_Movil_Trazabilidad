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
    private int pedidoId;

    // Constructor normal
    public Producto() {}

    public Producto(int id, String codigo, String nombre, String descripcion,
                    double precio, int cantidad, int pedidoId) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.pedidoId = pedidoId;
    }

    // Constructor desde Parcel
    protected Producto(Parcel in) {
        id = in.readInt();
        codigo = in.readString();
        nombre = in.readString();
        descripcion = in.readString();
        precio = in.readDouble();
        cantidad = in.readInt();
        pedidoId = in.readInt();
    }

    // Método requerido por Parcelable
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

    // Método que escribe los campos en un Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(codigo);
        dest.writeString(nombre);
        dest.writeString(descripcion);
        dest.writeDouble(precio);
        dest.writeInt(cantidad);
        dest.writeInt(pedidoId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.precio = precio;
    }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        this.cantidad = cantidad;
    }
    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }

    public double calcularSubtotal() {
        return precio * cantidad;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", cantidad=" + cantidad +
                ", pedidoId=" + pedidoId +
                '}';
    }
}