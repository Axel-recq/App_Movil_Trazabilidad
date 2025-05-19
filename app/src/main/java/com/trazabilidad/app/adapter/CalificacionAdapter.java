package com.trazabilidad.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Calificacion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalificacionAdapter extends RecyclerView.Adapter<CalificacionAdapter.ViewHolder> {

    private final Context context;
    private final List<Calificacion> calificaciones;
    private final SimpleDateFormat dateFormat;

    public CalificacionAdapter(Context context, List<Calificacion> calificaciones) {
        this.context = context;
        this.calificaciones = new ArrayList<>(calificaciones);
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void updateCalificaciones(List<Calificacion> newCalificaciones) {
        calificaciones.clear();
        calificaciones.addAll(newCalificaciones);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Calificacion calificacion = calificaciones.get(position);

        holder.tvPedidoNumero.setText(context.getString(R.string.pedido_numero, String.valueOf(calificacion.getPedidoId())));
        holder.tvCliente.setText(context.getString(R.string.cliente, calificacion.getNombreCliente() != null ? calificacion.getNombreCliente() : "Usuario ID: " + calificacion.getUsuarioId()));
        holder.ratingBar.setRating(calificacion.getValor());
        holder.tvCategoria.setText(context.getString(R.string.categoria, calificacion.getCategoria()));
        holder.tvComentario.setText(calificacion.getComentario() != null ? calificacion.getComentario() : "Sin comentario");
        holder.tvFecha.setText(dateFormat.format(calificacion.getFecha()));

        switch (calificacion.getCategoria()) {
            case "Muy bueno":
                holder.tvCategoria.setTextColor(context.getResources().getColor(R.color.green));
                break;
            case "Bueno":
                holder.tvCategoria.setTextColor(context.getResources().getColor(R.color.yellow));
                break;
            case "Malo":
                holder.tvCategoria.setTextColor(context.getResources().getColor(R.color.red));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return calificaciones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPedidoNumero;
        TextView tvCliente;
        RatingBar ratingBar;
        TextView tvCategoria;
        TextView tvComentario;
        TextView tvFecha;

        ViewHolder(View itemView) {
            super(itemView);
            tvPedidoNumero = itemView.findViewById(R.id.tvPedidoNumero);
            tvCliente = itemView.findViewById(R.id.tvCliente);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvComentario = itemView.findViewById(R.id.tvComentario);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}