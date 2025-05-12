package com.trazabilidad.app.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.trazabilidad.app.R;
import com.trazabilidad.app.activities.IncidenciaDetailActivity;
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

        // Abrir detalles al hacer clic en el item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), IncidenciaDetailActivity.class);
            intent.putExtra("INCIDENCIA", incidencia);
            v.getContext().startActivity(intent);
        });
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
        private final TextView tvFecha;
        private final TextView tvPedidoId;
        private final MaterialButton btnCambiarEstado;
        private final Chip chipEstado;

        IncidenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            placeholderFoto = itemView.findViewById(R.id.placeholderFoto);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvPedidoId = itemView.findViewById(R.id.tvPedidoId);
            btnCambiarEstado = itemView.findViewById(R.id.btnCambiarEstado);
            chipEstado = itemView.findViewById(R.id.chipEstado);
        }

        void bind(Incidencia incidencia) {
            // Configurar datos principales
            tvDescripcion.setText(incidencia.getDescripcion());

            // Configurar detalles con iconos
            configureTextWithDrawable(tvTipo, "Tipo: " + incidencia.getTipo(),
                    R.drawable.ic_reports);
            configureTextWithDrawable(tvPedidoId,
                    incidencia.getPedidoId() > 0 ? "Pedido: " + incidencia.getPedidoId() : "Sin pedido asociado",
                    R.drawable.ic_empty_orders);

            // Configurar fecha
            tvFecha.setText("Fecha: " + dateFormat.format(incidencia.getFecha()));

            // Configurar estado y su chip
            configureEstadoChip(incidencia.getEstado());

            // Mostrar foto o placeholder
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
            String currentStatus = incidencia.getEstado();
            String nextStatus = currentStatus.equals(Incidencia.ESTADO_PENDIENTE) ?
                    Incidencia.ESTADO_RESUELTO : Incidencia.ESTADO_PENDIENTE;

            // Texto del botón según el estado actual
            String buttonText = currentStatus.equals(Incidencia.ESTADO_PENDIENTE) ?
                    "Marcar como resuelto" : "Reabrir incidencia";
            btnCambiarEstado.setText(buttonText);

            // Icono del botón según el estado actual
            int iconResId = currentStatus.equals(Incidencia.ESTADO_PENDIENTE) ?
                    android.R.drawable.ic_menu_edit : android.R.drawable.ic_menu_revert;
            btnCambiarEstado.setIconResource(iconResId);

            // Listener del botón
            btnCambiarEstado.setOnClickListener(v ->
                    estadoClickListener.onEstadoClick(incidencia, nextStatus));
        }

        private void configureTextWithDrawable(TextView textView, String text, int drawableResId) {
            textView.setText(text);

            // Configurar drawable al inicio del texto
            Drawable drawable = ContextCompat.getDrawable(textView.getContext(), drawableResId);
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                textView.setCompoundDrawablesRelative(drawable, null, null, null);
                textView.setCompoundDrawablePadding(8);
            }
        }

        private void configureEstadoChip(String estado) {
            chipEstado.setText(estado);

            int bgColorResId;

            // Asignar color según estado
            switch (estado) {
                case Incidencia.ESTADO_RESUELTO:
                    bgColorResId = R.color.success;
                    break;
                case Incidencia.ESTADO_PENDIENTE:
                default:
                    bgColorResId = R.color.warning;
                    break;
            }

            chipEstado.setChipBackgroundColorResource(bgColorResId);
        }
    }
}