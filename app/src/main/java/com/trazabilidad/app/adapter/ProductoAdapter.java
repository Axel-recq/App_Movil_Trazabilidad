package com.trazabilidad.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Cambiado de ImageButton a ImageView
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Producto;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private List<Producto> listaProductosCompleta; // Para la búsqueda
    private OnProductoClickListener listener;
    private final NumberFormat formatoMoneda;
    private final Context context;
    private static final int STOCK_BAJO_LIMITE = 10;
    private static final int TIPO_VISTA_NORMAL = 1;
    private static final int TIPO_VISTA_BAJO_STOCK = 2;
    private static final int TIPO_VISTA_INACTIVO = 3;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
        void onDeleteClick(Producto producto);
        void onActivarClick(Producto producto);
        void onEditarStockClick(Producto producto);
    }

    // Constructor para gestión de productos
    public ProductoAdapter(Context context, List<Producto> listaProductos, OnProductoClickListener listener) {
        this.context = context;
        this.listaProductos = listaProductos;
        this.listaProductosCompleta = new ArrayList<>(listaProductos);
        this.listener = listener;
        this.formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;

        switch (viewType) {
            case TIPO_VISTA_BAJO_STOCK:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_producto_bajo_stock, parent, false);
                break;
            case TIPO_VISTA_INACTIVO:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_producto_inactivo, parent, false);
                break;
            case TIPO_VISTA_NORMAL:
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_producto, parent, false);
                break;
        }

        return new ProductoViewHolder(itemView, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        Producto producto = listaProductos.get(position);

        if (!producto.isActivo()) {
            return TIPO_VISTA_INACTIVO;
        } else if (producto.getCantidad() < STOCK_BAJO_LIMITE) {
            return TIPO_VISTA_BAJO_STOCK;
        } else {
            return TIPO_VISTA_NORMAL;
        }
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

    // Método para filtrar por texto de búsqueda
    public void filtrarPorTexto(String texto) {
        listaProductos.clear();
        if (texto.isEmpty()) {
            listaProductos.addAll(listaProductosCompleta);
        } else {
            texto = texto.toLowerCase().trim();
            for (Producto producto : listaProductosCompleta) {
                if (producto.getNombre().toLowerCase().contains(texto) ||
                        producto.getCodigo().toLowerCase().contains(texto) ||
                        producto.getDescripcion().toLowerCase().contains(texto)) {
                    listaProductos.add(producto);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Método para filtrar por categoría
    public void filtrarPorCategoria(String categoria) {
        listaProductos.clear();

        switch (categoria) {
            case "Todos":
                listaProductos.addAll(listaProductosCompleta);
                break;
            case "Activos":
                for (Producto producto : listaProductosCompleta) {
                    if (producto.isActivo()) {
                        listaProductos.add(producto);
                    }
                }
                break;
            case "Inactivos":
                for (Producto producto : listaProductosCompleta) {
                    if (!producto.isActivo()) {
                        listaProductos.add(producto);
                    }
                }
                break;
            case "Bajo Stock":
                for (Producto producto : listaProductosCompleta) {
                    if (producto.getCantidad() < STOCK_BAJO_LIMITE && producto.isActivo()) {
                        listaProductos.add(producto);
                    }
                }
                break;
        }
        notifyDataSetChanged();
    }

    // Método para actualizar la lista completa de productos
    public void actualizarLista(List<Producto> nuevaLista) {
        this.listaProductosCompleta = new ArrayList<>(nuevaLista);
        this.listaProductos.clear();
        this.listaProductos.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCodigo;
        TextView textViewNombre;
        TextView textViewPrecio;
        TextView textViewCantidad;
        TextView textViewDescripcion;
        TextView textViewEstado;
        ImageView buttonDelete; // Cambiado a ImageView
        MaterialButton buttonActivar;
        ImageView buttonEditarStock; // Cambiado a ImageView
        CardView cardViewProducto;
        int viewType;

        ProductoViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            // Elementos comunes a todos los layouts
            textViewCodigo = itemView.findViewById(R.id.textViewCodigo);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewPrecio = itemView.findViewById(R.id.textViewPrecio);
            textViewCantidad = itemView.findViewById(R.id.textViewCantidad);
            textViewEstado = itemView.findViewById(R.id.textViewEstado);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            cardViewProducto = itemView.findViewById(R.id.cardViewProducto);

            // Elementos específicos
            buttonActivar = itemView.findViewById(R.id.buttonActivar);
            buttonEditarStock = itemView.findViewById(R.id.buttonEditarStock);
            textViewDescripcion = itemView.findViewById(R.id.textViewDescripcion);
        }

        @SuppressLint("SetTextI18n")
        void bind(final Producto producto) {
            // Verificar que los TextView no sean null antes de usarlos
            if (textViewCodigo != null) {
                textViewCodigo.setText(producto.getCodigo());
            }
            if (textViewNombre != null) {
                textViewNombre.setText(producto.getNombre());
            }
            if (textViewPrecio != null) {
                textViewPrecio.setText(formatoMoneda.format(producto.getPrecio()));
            }
            if (textViewCantidad != null) {
                textViewCantidad.setText("Stock: " + producto.getCantidad());
            }

            // Si hay descripción disponible en el layout
            if (textViewDescripcion != null) {
                if (producto.getDescripcion() != null && !producto.getDescripcion().isEmpty()) {
                    textViewDescripcion.setText(producto.getDescripcion());
                    textViewDescripcion.setVisibility(View.VISIBLE);
                } else {
                    textViewDescripcion.setVisibility(View.GONE);
                }
            }

            // Configurar según tipo de vista
            switch (viewType) {
                case TIPO_VISTA_BAJO_STOCK:
                    if (textViewEstado != null) {
                        textViewEstado.setText("¡BAJO STOCK!");
                        textViewEstado.setTextColor(Color.parseColor("#FFA500")); // Naranja
                    }
                    if (textViewCantidad != null) {
                        textViewCantidad.setTextColor(Color.parseColor("#FFA500")); // Naranja
                    }
                    if (buttonEditarStock != null) {
                        buttonEditarStock.setVisibility(View.VISIBLE);
                        buttonEditarStock.setOnClickListener(v -> {
                            if (listener != null) listener.onEditarStockClick(producto);
                        });
                    }
                    break;

                case TIPO_VISTA_INACTIVO:
                    if (textViewEstado != null) {
                        textViewEstado.setText("INACTIVO");
                        textViewEstado.setTextColor(Color.RED);
                    }
                    if (cardViewProducto != null) {
                        cardViewProducto.setCardBackgroundColor(Color.parseColor("#FFEEEE"));
                    }
                    if (buttonActivar != null) {
                        buttonActivar.setVisibility(View.VISIBLE);
                        buttonActivar.setOnClickListener(v -> {
                            if (listener != null) listener.onActivarClick(producto);
                        });
                    }
                    break;

                case TIPO_VISTA_NORMAL:
                default:
                    if (textViewEstado != null) {
                        textViewEstado.setText("Activo");
                        textViewEstado.setTextColor(Color.parseColor("#4CAF50")); // Verde
                    }
                    if (textViewCantidad != null) {
                        textViewCantidad.setTextColor(Color.BLACK);
                    }
                    if (cardViewProducto != null) {
                        cardViewProducto.setCardBackgroundColor(Color.WHITE);
                    }
                    break;
            }

            // Configurar botón eliminar
            if (buttonDelete != null) {
                buttonDelete.setVisibility(listener != null ? View.VISIBLE : View.GONE);
                buttonDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(producto);
                });
            }

            // Configurar click en el item
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onProductoClick(producto));
            }
        }
    }
}