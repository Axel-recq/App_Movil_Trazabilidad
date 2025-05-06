package com.trazabilidad.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Pedido;

import java.util.List;

public class PedidoAdapter extends ArrayAdapter<Pedido> {

    private Context context;
    private List<Pedido> pedidos;

    public PedidoAdapter(Context context, List<Pedido> pedidos) {
        super(context, R.layout.item_pedido, pedidos);
        this.context = context;
        this.pedidos = pedidos;
    }
    static class ViewHolder {
        TextView tvNumeroPedido;
        TextView tvCliente;
        TextView tvEstado;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pedido, parent, false);
            holder = new ViewHolder();
            holder.tvNumeroPedido = convertView.findViewById(R.id.tvNumeroPedido);
            holder.tvCliente = convertView.findViewById(R.id.tvCliente);
            holder.tvEstado = convertView.findViewById(R.id.tvEstado);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Pedido pedido = pedidos.get(position);
        holder.tvNumeroPedido.setText(context.getString(R.string.pedido_numero, pedido.getNumero()));
        holder.tvCliente.setText(context.getString(R.string.cliente, pedido.getCliente()));
        holder.tvEstado.setText(context.getString(R.string.estado, pedido.getEstado()));

        return convertView;
    }
}
