package com.trazabilidad.app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Pedido;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentOrdersAdapter extends RecyclerView.Adapter<RecentOrdersAdapter.ViewHolder> {
    private Context context;
    private List<Pedido> pedidos;

    public RecentOrdersAdapter(Context context, List<Pedido> pedidos) {
        this.context = context;
        this.pedidos = pedidos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        Log.d("RecentOrdersAdapter", "Vinculando pedido: " + pedido.getCliente());
        holder.tvCliente.setText(pedido.getCliente());
        holder.tvDireccion.setText(pedido.getDireccion());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvFecha.setText(sdf.format(pedido.getFecha()));
        holder.tvEstado.setText(pedido.getEstado());
    }

    @Override
    public int getItemCount() {
        return pedidos != null ? pedidos.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCliente, tvDireccion, tvFecha, tvEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCliente = itemView.findViewById(R.id.tvCliente); // Ajusta los IDs según tu item_recent_order.xml
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}