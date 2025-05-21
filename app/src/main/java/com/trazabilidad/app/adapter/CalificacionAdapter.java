package com.trazabilidad.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Calificacion;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CalificacionAdapter extends ListAdapter<Calificacion, CalificacionAdapter.ViewHolder> {

    private final Context context;
    private final SimpleDateFormat dateFormat;
    private OnCalificacionClickListener listener;

    public interface OnCalificacionClickListener {
        void onCalificacionClick(Calificacion calificacion);
    }

    public CalificacionAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void setOnCalificacionClickListener(OnCalificacionClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Calificacion> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Calificacion>() {
                @Override
                public boolean areItemsTheSame(@NonNull Calificacion oldItem, @NonNull Calificacion newItem) {
                    // Asumiendo que tienes un ID único en tu modelo
                    return oldItem.getPedidoId() == newItem.getPedidoId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Calificacion oldItem, @NonNull Calificacion newItem) {
                    // Compara los campos relevantes para determinar si el contenido es el mismo
                    return oldItem.getValor() == newItem.getValor() &&
                            oldItem.getCategoria().equals(newItem.getCategoria()) &&
                            ((oldItem.getComentario() == null && newItem.getComentario() == null) ||
                                    (oldItem.getComentario() != null && oldItem.getComentario().equals(newItem.getComentario())));
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Calificacion calificacion = getItem(position);

        // Configurar los datos en el ViewHolder
        holder.bind(calificacion, context, dateFormat);

        // Configurar el evento de clic si hay un listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCalificacionClick(calificacion);
            }
        });
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

        void bind(Calificacion calificacion, Context context, SimpleDateFormat dateFormat) {
            tvPedidoNumero.setText(context.getString(R.string.pedido_numero, String.valueOf(calificacion.getPedidoId())));
            tvCliente.setText(context.getString(R.string.cliente,
                    calificacion.getNombreCliente() != null ? calificacion.getNombreCliente() :
                            "Usuario ID: " + calificacion.getUsuarioId()));

            ratingBar.setRating(calificacion.getValor());
            tvCategoria.setText(context.getString(R.string.categoria, calificacion.getCategoria()));
            tvComentario.setText(calificacion.getComentario() != null ? calificacion.getComentario() : "Sin comentario");
            tvFecha.setText(dateFormat.format(calificacion.getFecha()));

            // Establecer color según la categoría
            int colorResId;
            switch (calificacion.getCategoria()) {
                case "Muy bueno":
                    colorResId = R.color.green;
                    break;
                case "Bueno":
                    colorResId = R.color.yellow;
                    break;
                case "Malo":
                    colorResId = R.color.red;
                    break;
                default:
                    colorResId = R.color.colorPrimary;
                    break;
            }
            tvCategoria.setTextColor(ContextCompat.getColor(context, colorResId));
        }
    }
}