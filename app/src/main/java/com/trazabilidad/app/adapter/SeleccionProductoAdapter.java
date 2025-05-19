package com.trazabilidad.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Producto;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SeleccionProductoAdapter extends RecyclerView.Adapter<SeleccionProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private List<Producto> productosSeleccionados;
    private OnProductoSeleccionadoListener listener;

    public interface OnProductoSeleccionadoListener {
        void onProductoSeleccionado(Producto producto, boolean isChecked);
    }

    public SeleccionProductoAdapter(List<Producto> listaProductos, OnProductoSeleccionadoListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
        this.productosSeleccionados = new ArrayList<>();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seleccion_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        holder.textViewCodigo.setText(producto.getCodigo());
        holder.textViewNombre.setText(producto.getNombre());

        // Formatear precio con formato de moneda
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        holder.textViewPrecio.setText(formatoMoneda.format(producto.getPrecio()));

        // Mostrar cantidad disponible
        holder.textViewCantidad.setText("Stock: " + producto.getCantidad());

        // Estado del checkbox
        holder.checkBoxSeleccionar.setChecked(productosSeleccionados.contains(producto));

        // Listener para el checkbox
        holder.checkBoxSeleccionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = holder.checkBoxSeleccionar.isChecked();
                if (isChecked) {
                    productosSeleccionados.add(producto);
                } else {
                    productosSeleccionados.remove(producto);
                }
                listener.onProductoSeleccionado(producto, isChecked);
            }
        });

        // Listener para el item completo
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean nuevoEstado = !holder.checkBoxSeleccionar.isChecked();
                holder.checkBoxSeleccionar.setChecked(nuevoEstado);

                if (nuevoEstado) {
                    if (!productosSeleccionados.contains(producto)) {
                        productosSeleccionados.add(producto);
                    }
                } else {
                    productosSeleccionados.remove(producto);
                }

                listener.onProductoSeleccionado(producto, nuevoEstado);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public void setProductoSeleccionado(Producto producto) {
        if (!productosSeleccionados.contains(producto)) {
            productosSeleccionados.add(producto);
            notifyDataSetChanged();
        }
    }

    public List<Producto> getProductosSeleccionados() {
        return productosSeleccionados;
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCodigo;
        TextView textViewNombre;
        TextView textViewPrecio;
        TextView textViewCantidad;
        CheckBox checkBoxSeleccionar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCodigo = itemView.findViewById(R.id.textViewCodigo);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewPrecio = itemView.findViewById(R.id.textViewPrecio);
            textViewCantidad = itemView.findViewById(R.id.textViewCantidad);
            checkBoxSeleccionar = itemView.findViewById(R.id.checkBoxSeleccionar);
        }
    }
}