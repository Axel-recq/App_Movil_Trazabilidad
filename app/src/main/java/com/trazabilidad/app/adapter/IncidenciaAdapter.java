package com.trazabilidad.app.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Incidencia;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder> {

    private final List<Incidencia> incidencias;
    private final OnEstadoClickListener estadoClickListener;
    private final SimpleDateFormat dateFormat;

    public interface OnEstadoClickListener {
        void onEstadoClick(Incidencia incidencia, String nuevoEstado);
    }

    public IncidenciaAdapter(List<Incidencia> incidencias, OnEstadoClickListener listener) {
        this.incidencias = incidencias;
        this.estadoClickListener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public IncidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incidencia, parent, false);
        return new IncidenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenciaViewHolder holder, int position) {
        Incidencia incidencia = incidencias.get(position);
        holder.bind(incidencia);
    }

    @Override
    public int getItemCount() {
        return incidencias.size();
    }

    class IncidenciaViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivFoto;
        private final LinearLayout placeholderFoto;
        private final TextView tvDescripcion;
        private final TextView tvTipo;
        private final TextView tvEstado;
        private final TextView tvFecha;
        private final TextView tvPedidoId;
        private final MaterialButton btnCambiarEstado;

        IncidenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            placeholderFoto = itemView.findViewById(R.id.placeholderFoto);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvPedidoId = itemView.findViewById(R.id.tvPedidoId);
            btnCambiarEstado = itemView.findViewById(R.id.btnCambiarEstado);
        }

        void bind(Incidencia incidencia) {
            tvDescripcion.setText(incidencia.getDescripcion());
            tvTipo.setText("Tipo: " + incidencia.getTipo());
            tvEstado.setText("Estado: " + incidencia.getEstado());
            tvFecha.setText("Fecha: " + dateFormat.format(incidencia.getFecha()));
            int pedidoId = incidencia.getPedidoId();
            tvPedidoId.setText(pedidoId > 0 ? "Pedido: " + pedidoId : "Sin pedido asociado");

            // Mostrar foto
            String fotoUri = incidencia.getFotoUri();
            if (fotoUri != null && !fotoUri.isEmpty()) {
                ivFoto.setImageURI(Uri.parse(fotoUri));
                ivFoto.setVisibility(View.VISIBLE);
                placeholderFoto.setVisibility(View.GONE);
            } else {
                ivFoto.setVisibility(View.GONE);
                placeholderFoto.setVisibility(View.VISIBLE);
            }

            // Configurar botón para cambiar estado
            btnCambiarEstado.setOnClickListener(v -> {
                String nuevoEstado = incidencia.getEstado().equals(Incidencia.ESTADO_PENDIENTE) ?
                        Incidencia.ESTADO_RESUELTO : Incidencia.ESTADO_PENDIENTE;
                estadoClickListener.onEstadoClick(incidencia, nuevoEstado);
            });
        }
    }
}