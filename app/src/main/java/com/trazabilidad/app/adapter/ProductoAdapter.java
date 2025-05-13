package com.trazabilidad.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Producto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private final List<Producto> listaProductos;
    private OnProductoClickListener listener;
    private final NumberFormat formatoMoneda;
    private final Context context;
    private boolean mostrarBotonEliminar = false;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
        void onDeleteClick(Producto producto);
    }

    // Constructor original que requiere listener
    public ProductoAdapter(List<Producto> listaProductos, OnProductoClickListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
        this.context = null;
        this.formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        this.mostrarBotonEliminar = true;
    }

    // Constructor nuevo para DetallePedidoActivity (sin callback de eliminación)
    public ProductoAdapter(Context context, List<Producto> listaProductos) {
        this.context = context;
        this.listaProductos = listaProductos;
        this.listener = null;
        this.formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        this.mostrarBotonEliminar = false;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.bind(producto);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCodigo;
        TextView textViewNombre;
        TextView textViewPrecio;
        TextView textViewCantidad;
        TextView textViewSubtotal;
        ImageButton buttonDelete;

        ProductoViewHolder(View itemView) {
            super(itemView);
            textViewCodigo = itemView.findViewById(R.id.textViewCodigo);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewPrecio = itemView.findViewById(R.id.textViewPrecio);
            textViewCantidad = itemView.findViewById(R.id.textViewCantidad);
            textViewSubtotal = itemView.findViewById(R.id.textViewSubtotal);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(final Producto producto) {
            textViewCodigo.setText(producto.getCodigo());
            textViewNombre.setText(producto.getNombre());
            textViewPrecio.setText(formatoMoneda.format(producto.getPrecio()));
            textViewCantidad.setText(String.valueOf(producto.getCantidad()));

            // Calcular subtotal
            double subtotal = producto.getPrecio() * producto.getCantidad();
            textViewSubtotal.setText(formatoMoneda.format(subtotal));

            // Mostrar u ocultar el botón de eliminar según corresponda
            buttonDelete.setVisibility(mostrarBotonEliminar ? View.VISIBLE : View.GONE);

            // Configurar listener para clic en el ítem solo si hay un listener configurado
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onProductoClick(producto));

                if (mostrarBotonEliminar) {
                    buttonDelete.setOnClickListener(v -> listener.onDeleteClick(producto));
                }
            }
        }
    }
}