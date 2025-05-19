package com.trazabilidad.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Producto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PedidoProductoAdapter extends RecyclerView.Adapter<PedidoProductoAdapter.PedidoProductoViewHolder> {

    private List<Producto> listaProductos;
    private OnProductoActionListener listener;

    public interface OnProductoActionListener {
        void onRemoveProducto(int position);
        void onEditCantidad(int position, Producto producto);
    }

    public PedidoProductoAdapter(List<Producto> listaProductos, OnProductoActionListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PedidoProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido_producto, parent, false);
        return new PedidoProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        holder.textViewNombre.setText(producto.getNombre());
        holder.textViewCodigo.setText("Código: " + producto.getCodigo());

        // Formatear precio
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        holder.textViewPrecio.setText(formatoMoneda.format(producto.getPrecio()));

        // Mostrar cantidad
        holder.textViewCantidad.setText("Cantidad: " + producto.getCantidad());

        // Calcular subtotal
        double subtotal = producto.getPrecio() * producto.getCantidad();
        holder.textViewSubtotal.setText("Subtotal: " + formatoMoneda.format(subtotal));

        // Configurar botones
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveProducto(holder.getAdapterPosition());
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditCantidad(holder.getAdapterPosition(), producto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public void actualizarProductos(List<Producto> nuevosProductos) {
        this.listaProductos = nuevosProductos;
        notifyDataSetChanged();
    }

    public double calcularTotal() {
        double total = 0;
        for (Producto producto : listaProductos) {
            total += producto.getPrecio() * producto.getCantidad();
        }
        return total;
    }

    static class PedidoProductoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombre;
        TextView textViewCodigo;
        TextView textViewPrecio;
        TextView textViewCantidad;
        TextView textViewSubtotal;
        MaterialButton btnEdit;
        MaterialButton btnRemove;

        public PedidoProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewCodigo = itemView.findViewById(R.id.textViewCodigo);
            textViewPrecio = itemView.findViewById(R.id.textViewPrecio);
            textViewCantidad = itemView.findViewById(R.id.textViewCantidad);
            textViewSubtotal = itemView.findViewById(R.id.textViewSubtotal);
            btnEdit = itemView.findViewById(R.id.btnEditCantidad);
            btnRemove = itemView.findViewById(R.id.btnRemoveProducto);
        }
    }
}