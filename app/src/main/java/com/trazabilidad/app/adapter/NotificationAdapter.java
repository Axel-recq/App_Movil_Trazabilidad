package com.trazabilidad.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.trazabilidad.app.R;
import com.trazabilidad.app.activities.ListaCalificacionesActivity;
import com.trazabilidad.app.activities.ListaDevolucionesActivity;
import com.trazabilidad.app.activities.ListaProductosActivity;
import com.trazabilidad.app.activities.DetallePedidoActivity;
import com.trazabilidad.app.database.NotificacionDAO;
import com.trazabilidad.app.models.Notificacion;
import com.trazabilidad.app.utils.DateUtils;
import com.trazabilidad.app.utils.NotificationManager;
import com.trazabilidad.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private static final String TAG = "NotificationAdapter";
    private List<Notificacion> notificaciones;
    private final List<Notificacion> notificacionesCompletas;
    private final Context context;
    private final NotificacionDAO notificacionDAO;
    private final SessionManager sessionManager;
    private final NotificationManager notificationManager;
    private OnNotificationActionListener actionListener;

    public interface OnNotificationActionListener {
        void onNotificationRead(int position);
        void onNotificationDeleted(int position);
        void onEmptyState(boolean isEmpty);
    }

    public NotificationAdapter(Context context, List<Notificacion> notificaciones) {
        this.context = context;
        this.notificaciones = new ArrayList<>(notificaciones);
        this.notificacionesCompletas = new ArrayList<>(notificaciones);
        this.notificacionDAO = new NotificacionDAO(context);
        this.sessionManager = new SessionManager(context);
        this.notificationManager = new NotificationManager(context);
    }

    public void setOnNotificationActionListener(OnNotificationActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion notificacion = notificaciones.get(position);
        holder.tvMensaje.setText(notificacion.getMensaje());

        // Formatear fecha relativa (hoy, ayer, o fecha completa)
        holder.tvFecha.setText(DateUtils.getRelativeDateTimeString(context, notificacion.getFecha()));

        // Establecer ícono según tipo de notificación
        int iconRes;
        int colorRes;
        switch (notificacion.getTipo()) {
            case BAJO_STOCK:
                iconRes = R.drawable.ic_inventory;
                colorRes = R.color.warning_color;
                break;
            case DEVOLUCION:
                iconRes = R.drawable.ic_refresh;
                colorRes = R.color.devolution_color;
                break;
            case CALIFICACION:
                iconRes = R.drawable.ic_star;
                colorRes = R.color.calification_color;
                break;
            default:
                iconRes = R.drawable.ic_notification;
                colorRes = R.color.notification_default;
                break;
        }

        holder.ivIcon.setImageResource(iconRes);
        holder.viewTypeIndicator.setBackgroundColor(ContextCompat.getColor(context, colorRes));
        holder.ivIcon.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_notification_background));

        // Marcar visualmente notificaciones leídas vs no leídas
        if (notificacion.isLeida()) {
            holder.cardNotification.setAlpha(0.7f);
            holder.ivUnread.setVisibility(View.GONE);
        } else {
            holder.cardNotification.setAlpha(1.0f);
            holder.ivUnread.setVisibility(View.VISIBLE);
        }

        // Configurar botón de eliminar
        holder.ivDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                eliminarNotificacion(adapterPosition);
            }
        });

        // Configurar clic para abrir detalle
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                abrirDetalleNotificacion(notificaciones.get(adapterPosition));
                marcarComoLeida(adapterPosition);
            }
        });
    }

    private void abrirDetalleNotificacion(Notificacion notificacion) {
        Intent intent = null;

        switch (notificacion.getTipo()) {
            case BAJO_STOCK:
                intent = new Intent(context, ListaProductosActivity.class);
                if (notificacion.getProductoId() != null) {
                    intent.putExtra("producto_id", notificacion.getProductoId());
                }
                break;

            case DEVOLUCION:
                if (sessionManager.isLoggedIn()) {
                    String rol = sessionManager.getUsuarioDetails().getRol();
                    if (rol.equals("ADMINISTRADOR") || rol.equals("REPARTIDOR")) {
                        intent = new Intent(context, ListaDevolucionesActivity.class);
                        intent.putExtra("pedidoId", notificacion.getReferenciaId());
                    } else {
                        Toast.makeText(context, "Acceso restringido. Solo para administradores y repartidores.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, "Debe iniciar sesión.", Toast.LENGTH_LONG).show();
                }
                break;

            case CALIFICACION:
                int calificacionPedidoId = notificacion.getReferenciaId();
                Log.d(TAG, "Attempting navigation to ListaCalificacionesActivity with pedidoId: " + calificacionPedidoId);
                if (calificacionPedidoId > 0) {
                    intent = new Intent(context, ListaCalificacionesActivity.class);
                    intent.putExtra("pedidoId", calificacionPedidoId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                } else {
                    Log.w(TAG, "Invalid pedidoId: " + calificacionPedidoId);
                    Toast.makeText(context, "ID de pedido inválido.", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                // Para otros tipos, por ahora no hacemos nada
                Toast.makeText(context, "Notificación: " + notificacion.getMensaje(), Toast.LENGTH_SHORT).show();
                break;
        }

        if (intent != null) {
            context.startActivity(intent);
        }
    }

    private void marcarComoLeida(int position) {
        Notificacion notificacion = notificaciones.get(position);

        if (!notificacion.isLeida()) {
            // Marcar como leída en la base de datos
            boolean result = notificacionDAO.marcarComoLeida(notificacion.getId());

            if (result) {
                // Actualizar el modelo local
                notificacion.setLeida(true);
                notifyItemChanged(position);

                // Actualizar en la lista completa también
                for (Notificacion n : notificacionesCompletas) {
                    if (n.getId() == notificacion.getId()) {
                        n.setLeida(true);
                        break;
                    }
                }

                // Notificar al listener
                if (actionListener != null) {
                    actionListener.onNotificationRead(position);
                }
            } else {
                Log.e(TAG, "Error al marcar como leída la notificación: " + notificacion.getId());
            }
        }
    }

    private void eliminarNotificacion(int position) {
        final Notificacion notificacion = notificaciones.get(position);

        notificationManager.eliminarNotificacion(notificacion.getId(), new NotificationManager.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                // Eliminar de ambas listas
                notificaciones.remove(position);
                notificacionesCompletas.remove(notificacion);

                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());

                // Verificar si quedó vacía la lista
                if (notificaciones.isEmpty() && actionListener != null) {
                    actionListener.onEmptyState(true);
                }

                // Notificar al listener
                if (actionListener != null) {
                    actionListener.onNotificationDeleted(position);
                }

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void filtrarPorTipo(Notificacion.Tipo tipo) {
        if (tipo == null) {
            // Mostrar todas las notificaciones
            notificaciones = new ArrayList<>(notificacionesCompletas);
        } else {
            // Filtrar por tipo
            notificaciones = new ArrayList<>();
            for (Notificacion notificacion : notificacionesCompletas) {
                if (notificacion.getTipo() == tipo) {
                    notificaciones.add(notificacion);
                }
            }
        }

        // Notificar cambio en el dataset
        notifyDataSetChanged();

        // Notificar si quedó vacía la lista después del filtrado
        if (actionListener != null) {
            actionListener.onEmptyState(notificaciones.isEmpty());
        }
    }

    public void marcarTodasComoLeidas() {
        boolean hayNoLeidas = false;

        // Verificar si hay alguna sin leer
        for (Notificacion notificacion : notificaciones) {
            if (!notificacion.isLeida()) {
                hayNoLeidas = true;
                break;
            }
        }

        if (!hayNoLeidas) {
            return; // No hay nada que marcar
        }

        // Marcar todas como leídas
        notificationManager.marcarComoLeidas();

        // Actualizar modelos locales
        for (Notificacion notificacion : notificaciones) {
            notificacion.setLeida(true);
        }

        for (Notificacion notificacion : notificacionesCompletas) {
            notificacion.setLeida(true);
        }

        // Notificar cambios en la UI
        notifyDataSetChanged();

        // Notificar al listener
        if (actionListener != null) {
            actionListener.onNotificationRead(-1); // -1 indica que son todas
        }
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje;
        TextView tvFecha;
        ImageView ivIcon;
        ImageView ivDelete;
        ImageView ivUnread;
        View viewTypeIndicator;
        MaterialCardView cardNotification;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivUnread = itemView.findViewById(R.id.ivUnread);
            viewTypeIndicator = itemView.findViewById(R.id.viewTypeIndicator);
            cardNotification = itemView.findViewById(R.id.cardNotification);
        }
    }
}