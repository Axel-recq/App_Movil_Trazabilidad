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
import com.trazabilidad.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    public interface OnItemClickListener {
        void onVerDetalles(int pedidoId);
        void onCalificar(int pedidoId);
        void onMarcarEntregado(int pedidoId);
        void onVerMapa(int pedidoId);
        void onDevolver(int pedidoId);
    }

    private final Context context;
    private final List<Pedido> pedidosList;
    private final OnItemClickListener clickListener;
    private final SessionManager sessionManager;
    private final SimpleDateFormat dateFormat;

    public PedidoAdapter(Context context, List<Pedido> pedidosList, OnItemClickListener clickListener, SessionManager sessionManager) {
        this.context = context;
        this.pedidosList = pedidosList;
        this.clickListener = clickListener;
        this.sessionManager = sessionManager;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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
        private final MaterialButton btnCalificar;
        private final MaterialButton btnDevolver;

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
            btnCalificar = itemView.findViewById(R.id.btnCalificar);
            btnDevolver = itemView.findViewById(R.id.btnDevolver);

            configurarBotones();
        }

        private void configurarBotones() {
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onVerDetalles(pedidosList.get(position).getId());
                }
            });

            btnVerMapa.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onVerMapa(pedidosList.get(position).getId());
                }
            });

            btnEntregado.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onMarcarEntregado(pedidosList.get(position).getId());
                }
            });

            btnCalificar.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onCalificar(pedidosList.get(position).getId());
                }
            });

            btnDevolver.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onDevolver(pedidosList.get(position).getId());
                }
            });
        }

        public void bind(Pedido pedido) {
            tvPedidoNumero.setText(context.getString(R.string.pedido_numero, pedido.getNumero()));
            tvCliente.setText(context.getString(R.string.cliente, pedido.getCliente()));
            tvDireccion.setText(context.getString(R.string.direccion, pedido.getDireccion()));
            tvFecha.setText(context.getString(R.string.fecha, dateFormat.format(pedido.getFecha())));


            if (sessionManager.isCliente()) {
                btnEntregado.setVisibility(View.GONE);
                btnVerMapa.setVisibility(View.GONE);
                btnCalificar.setVisibility("ENTREGADO".equals(pedido.getEstado()) ? View.VISIBLE : View.GONE);
                btnDevolver.setVisibility("ENTREGADO".equals(pedido.getEstado()) ? View.VISIBLE : View.GONE);
            } else if (sessionManager.isRepartidor()) {
                btnCalificar.setVisibility(View.GONE);
                btnDevolver.setVisibility("ENTREGADO".equals(pedido.getEstado()) ? View.VISIBLE : View.GONE);
                btnVerMapa.setVisibility(pedido.getLatitud() != null && pedido.getLongitud() != null ? View.VISIBLE : View.GONE);
                btnEntregado.setVisibility(
                        !"ENTREGADO".equals(pedido.getEstado()) &&
                                !"CANCELADO".equals(pedido.getEstado()) &&
                                !"RECHAZADO".equals(pedido.getEstado()) ? View.VISIBLE : View.GONE);
            }

            configurarEstado(pedido.getEstado());
        }

        private void configurarEstado(String estado) {
            int colorEstado;
            String estadoDisplay = estado != null ? estado : "DESCONOCIDO";

            switch (estadoDisplay.toUpperCase()) {
                case "PENDIENTE":
                    colorEstado = R.color.status_pending;
                    break;
                case "EN_PREPARACION":
                    colorEstado = R.color.status_preparing;
                    break;
                case "ASIGNADO":
                    colorEstado = R.color.status_assigned;
                    break;
                case "EN_RUTA":
                    colorEstado = R.color.status_delivering;
                    break;
                case "ENTREGADO":
                    colorEstado = R.color.status_delivered;
                    break;
                case "CANCELADO":
                    colorEstado = R.color.status_canceled;
                    break;
                case "RECHAZADO":
                    colorEstado = R.color.status_rejected;
                    break;
                default:
                    colorEstado = R.color.gray_500;
                    estadoDisplay = "DESCONOCIDO";
                    break;
            }

            chipEstado.setText(estadoDisplay);
            chipEstado.setChipBackgroundColorResource(colorEstado);
            viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, colorEstado));
        }
    }
}