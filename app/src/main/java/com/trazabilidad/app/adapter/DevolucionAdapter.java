package com.trazabilidad.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Devolucion;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DevolucionAdapter extends RecyclerView.Adapter<DevolucionAdapter.ViewHolder> {

    private final Context context;
    private final List<Devolucion> devoluciones;
    private final SimpleDateFormat sdf;

    public DevolucionAdapter(Context context, List<Devolucion> devoluciones) {
        this.context = context;
        this.devoluciones = devoluciones;
        this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_devolucion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Devolucion devolucion = devoluciones.get(position);
        holder.tvMotivo.setText(devolucion.getMotivo());
        holder.tvCantidad.setText(context.getString(R.string.cantidad_devuelta, devolucion.getCantidad()));
        holder.tvFecha.setText(sdf.format(devolucion.getFecha()));
    }

    @Override
    public int getItemCount() {
        return devoluciones.size();
    }

    public void actualizarDevoluciones(List<Devolucion> nuevasDevoluciones) {
        devoluciones.clear();
        devoluciones.addAll(nuevasDevoluciones);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMotivo;
        TextView tvCantidad;
        TextView tvFecha;

        ViewHolder(View itemView) {
            super(itemView);
            tvMotivo = itemView.findViewById(R.id.tvMotivo);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}