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

import com.trazabilidad.app.R;
import com.trazabilidad.app.activities.ListaCalificacionesActivity;
import com.trazabilidad.app.activities.ListaDevolucionesActivity;
import com.trazabilidad.app.activities.ListaProductosActivity;
import com.trazabilidad.app.database.NotificacionDAO;
import com.trazabilidad.app.models.Notificacion;
import com.trazabilidad.app.utils.DateUtils;
import com.trazabilidad.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private static final String TAG = "NotificationAdapter";
    private List<Notificacion> notificaciones;
    private final List<Notificacion> notificacionesCompletas;
    private final Context context;
    private final NotificacionDAO notificacionDAO;
    private final SessionManager sessionManager;
    private OnNotificationReadListener readListener;

    public interface OnNotificationReadListener {
        void onNotificationRead(int position);
    }

    public NotificationAdapter(Context context, List<Notificacion> notificaciones) {
        this.context = context;
        this.notificaciones = new ArrayList<>(notificaciones);
        this.notificacionesCompletas = new ArrayList<>(notificaciones);
        this.notificacionDAO = new NotificacionDAO(context);
        this.sessionManager = new SessionManager(context);
    }

    public void setOnNotificationReadListener(OnNotificationReadListener listener) {
        this.readListener = listener;
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
        holder.ivIcon.setColorFilter(ContextCompat.getColor(context, colorRes));

        // Mostrar indicador de no leída
        holder.ivStatusIndicator.setVisibility(notificacion.isLeida() ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Notification clicked: tipo=" + notificacion.getTipo() + ", referenciaId=" + notificacion.getReferenciaId());

            // Marcar como leída
            boolean success = notificacionDAO.marcarComoLeida(notificacion.getId());
            if (success) {
                notificacion.setLeida(true);
                holder.ivStatusIndicator.setVisibility(View.GONE);

                if (readListener != null) {
                    readListener.onNotificationRead(position);
                }
            }

            // Navegar según el tipo de notificación
            Intent intent = null;
            switch (notificacion.getTipo()) {
                case BAJO_STOCK:
                    intent = new Intent(context, ListaProductosActivity.class);
                    intent.putExtra("productoId", notificacion.getProductoId());
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
                    Log.d(TAG, "Intentando navegar a ListaCalificacionesActivity con pedidoId: " + calificacionPedidoId);
                    if (calificacionPedidoId > 0) {
                        intent = new Intent(context, ListaCalificacionesActivity.class);
                        intent.putExtra("pedidoId", calificacionPedidoId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    } else {
                        Log.w(TAG, "ID de pedido inválido: " + calificacionPedidoId);
                        Toast.makeText(context, "ID de pedido inválido.", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

            if (intent != null) {
                try {
                    Log.d(TAG, "Iniciando actividad con intent: " + intent);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error al iniciar actividad: " + e.getMessage(), e);
                    Toast.makeText(context, "Error al abrir la actividad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.w(TAG, "No se creó intent para notificación tipo: " + notificacion.getTipo());
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    /**
     * Filtra las notificaciones por tipo
     * @param tipo Tipo de notificación a filtrar o null para mostrar todas
     */
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
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewTypeIndicator;
        ImageView ivIcon;
        TextView tvMensaje;
        TextView tvFecha;
        ImageView ivAction;
        View ivStatusIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            viewTypeIndicator = itemView.findViewById(R.id.viewTypeIndicator);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvMensaje = itemView.findViewById(R.id.tvNotificationMessage);
            tvFecha = itemView.findViewById(R.id.tvNotificationDate);
            ivAction = itemView.findViewById(R.id.ivAction);
            ivStatusIndicator = itemView.findViewById(R.id.ivStatusIndicator);
        }
    }
}