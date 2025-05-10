package com.trazabilidad.app.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
        View viewStatusIndicator;
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
            holder.viewStatusIndicator = convertView.findViewById(R.id.viewStatusIndicator);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Pedido pedido = pedidos.get(position);
        holder.tvNumeroPedido.setText(context.getString(R.string.pedido_numero, pedido.getNumero()));
        holder.tvCliente.setText(context.getString(R.string.cliente, pedido.getCliente()));
        holder.tvEstado.setText(context.getString(R.string.estado, pedido.getEstado()));

        // Ajustar colores según el estado del pedido
        setColorSegunEstado(pedido.getEstado(), holder);

        return convertView;
    }

    private void setColorSegunEstado(String estado, ViewHolder holder) {
        int colorIndicador;  // Color del indicador de estado
        int colorFondo;      // Color del fondo del TextView
        int colorTexto;      // Color del texto del estado

        switch (estado.toLowerCase()) {
            case "entregado":
                colorIndicador = R.color.success;      // #4CAF50 (verde)
                colorFondo = R.color.custom_50;        // #FEFEFF (blanco claro)
                colorTexto = R.color.success;          // #4CAF50 (verde)
                break;
            case "en camino":
                colorIndicador = R.color.info;         // #2196F3 (azul)
                colorFondo = R.color.custom_100;       // #D8D0E7 (acento claro)
                colorTexto = R.color.info;             // #2196F3 (azul)
                break;
            case "pendiente":
                colorIndicador = R.color.warning;      // #FFC107 (amarillo)
                colorFondo = R.color.custom_50;        // #FEFEFF (blanco claro)
                colorTexto = R.color.warning;          // #FFC107 (amarillo)
                break;
            case "incidencia":
                colorIndicador = R.color.error;        // #F44336 (rojo)
                colorFondo = R.color.custom_50;        // #FEFEFF (blanco claro)
                colorTexto = R.color.error;            // #F44336 (rojo)
                break;
            default:
                colorIndicador = R.color.colorPrimary; // #312A94 (principal oscuro)
                colorFondo = R.color.custom_50;        // #FEFEFF (blanco claro)
                colorTexto = R.color.colorPrimary;     // #312A94 (principal oscuro)
                break;
        }
        // Aplicar colores al indicador de estado
        holder.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, colorIndicador));

        // Aplicar colores al fondo del TextView de estado
        GradientDrawable estadoBackground = (GradientDrawable) holder.tvEstado.getBackground();
        estadoBackground.setColor(ContextCompat.getColor(context, colorFondo));
        estadoBackground.setStroke(1, ContextCompat.getColor(context, colorIndicador));

        // Aplicar color al texto de estado
        holder.tvEstado.setTextColor(ContextCompat.getColor(context, colorTexto));
    }
}