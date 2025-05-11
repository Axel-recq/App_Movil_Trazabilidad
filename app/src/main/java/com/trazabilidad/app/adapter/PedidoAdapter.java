package com.trazabilidad.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Pedido;

import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    public interface OnPedidoClickListener {
        void onPedidoClick(int pedidoId);
    }

    private final Context context;
    private final List<Pedido> pedidosList;
    private final OnPedidoClickListener clickListener;

    public PedidoAdapter(Context context, List<Pedido> pedidosList, OnPedidoClickListener clickListener) {
        this.context = context;
        this.pedidosList = pedidosList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidosList.get(position);
        holder.bind(pedido);
    }

    @Override
    public int getItemCount() {
        return pedidosList.size();
    }

    public class PedidoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPedidoNumero;
        private final TextView tvCliente;
        private final TextView tvDireccion;
        private final TextView tvFecha;
        private final Chip chipEstado;
        private final View viewStatusIndicator;
        private final MaterialButton btnVerMapa;
        private final MaterialButton btnEntregado;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPedidoNumero = itemView.findViewById(R.id.tvPedidoNumero);
            tvCliente = itemView.findViewById(R.id.tvCliente);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
            btnVerMapa = itemView.findViewById(R.id.btnVerMapa);
            btnEntregado = itemView.findViewById(R.id.btnEntregado);

            // Configuramos el click listener para todo el item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onPedidoClick(pedidosList.get(position).getId());
                }
            });

            // Click listeners para los botones de acción
            configurarBotones();
        }

        private void configurarBotones() {
            btnVerMapa.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Implementar navegación al mapa
                    // Por ejemplo: abrirMapa(pedidosList.get(position));
                }
            });

            btnEntregado.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Implementar marcado como entregado
                    // Por ejemplo: marcarComoEntregado(pedidosList.get(position).getId());
                }
            });
        }

        public void bind(Pedido pedido) {
            tvPedidoNumero.setText(context.getString(R.string.pedido_numero, String.valueOf(pedido.getId())));
            tvCliente.setText(context.getString(R.string.cliente, pedido.getCliente()));
            tvDireccion.setText(context.getString(R.string.direccion, pedido.getDireccion()));
            tvFecha.setText(context.getString(R.string.fecha, pedido.getFecha()));

            // Configuramos el chip de estado y el indicador visual
            configurarEstado(pedido.getEstado());
        }

        private void configurarEstado(String estado) {
            int colorEstado;

            // Ajustamos color y visibilidad según el estado
            switch (estado.toLowerCase()) {
                case "pendiente":
                    colorEstado = R.color.status_pending;
                    break;
                case "entregando":
                case "en camino":
                    colorEstado = R.color.status_delivering;
                    break;
                case "entregado":
                    colorEstado = R.color.status_delivered;
                    btnEntregado.setVisibility(View.GONE);
                    break;
                case "cancelado":
                    colorEstado = R.color.status_canceled;
                    btnEntregado.setVisibility(View.GONE);
                    break;
                case "incidencia":
                    colorEstado = R.color.status_incident;
                    break;
                default:
                    colorEstado = R.color.gray_500;
                    break;
            }

            chipEstado.setText(estado);
            chipEstado.setChipBackgroundColorResource(colorEstado);
            viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, colorEstado));
        }
    }
}