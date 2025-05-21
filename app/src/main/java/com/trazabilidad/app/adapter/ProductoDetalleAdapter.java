package com.trazabilidad.app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Producto;

import java.util.List;

public class ProductoDetalleAdapter extends RecyclerView.Adapter<ProductoDetalleAdapter.ProductoViewHolder> {

    private static final String TAG = "ProductoDetalleAdapter";
    private Context context;
    private List<Producto> productos;
    private OnProductoClickListener listener;
    private int pedidoId;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
        void onDevolverProducto(int pedidoId, int productoId);
    }

    public ProductoDetalleAdapter(Context context, List<Producto> productos, int pedidoId, OnProductoClickListener listener) {
        this.context = context;
        this.productos = productos;
        this.pedidoId = pedidoId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto_detalle, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.tvNombre.setText(producto.getNombre());
        holder.tvCodigo.setText(context.getString(R.string.codigo, producto.getCodigo()));
        holder.tvCantidad.setText(context.getString(R.string.cantidad, producto.getCantidad()));

        // Handle price as double
        try {
            double precio = producto.getPrecio();
            Log.d(TAG, "Product ID " + producto.getId() + " Price: " + precio);
            if (Double.isNaN(precio) || Double.isInfinite(precio) || precio < 0) {
                Log.e(TAG, "Invalid price for product ID " + producto.getId() + ": " + precio);
                holder.tvPrecio.setText(context.getString(R.string.precio, 0.00));
            } else {
                // Format price directly with context.getString
                holder.tvPrecio.setText(context.getString(R.string.precio, precio));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting price for product ID " + producto.getId() + ": " + e.getMessage(), e);
            holder.tvPrecio.setText(context.getString(R.string.precio, 0.00));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductoClick(producto);
            }
        });

        holder.btnDevolver.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDevolverProducto(pedidoId, producto.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return productos != null ? productos.size() : 0;
    }

    public void updateProductos(List<Producto> nuevosProductos) {
        this.productos = nuevosProductos;
        notifyDataSetChanged();
    }

    public List<Producto> getProductos() {
        return productos;
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCodigo, tvCantidad, tvPrecio;
        MaterialButton btnDevolver;

        ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvCodigo = itemView.findViewById(R.id.tvProductoCodigo);
            tvCantidad = itemView.findViewById(R.id.tvProductoCantidad);
            tvPrecio = itemView.findViewById(R.id.tvProductoPrecio);
            btnDevolver = itemView.findViewById(R.id.btnDevolver);
        }
    }
}