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
import com.trazabilidad.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private static final String TAG = "NotificationAdapter";
    private final List<Notificacion> notificaciones;
    private final Context context;
    private final NotificacionDAO notificacionDAO;
    private final SessionManager sessionManager;

    public NotificationAdapter(Context context, List<Notificacion> notificaciones) {
        this.context = context;
        this.notificaciones = notificaciones;
        this.notificacionDAO = new NotificacionDAO(context);
        this.sessionManager = new SessionManager(context);
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvFecha.setText(sdf.format(notificacion.getFecha()));

        int iconRes;
        int colorRes;
        switch (notificacion.getTipo()) {
            case BAJO_STOCK:
                iconRes = R.drawable.ic_warning;
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
        holder.ivIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, colorRes));

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Notification clicked: tipo=" + notificacion.getTipo() + ", referenciaId=" + notificacion.getReferenciaId());
            notificacionDAO.marcarComoLeida(notificacion.getId());
            notifyItemChanged(position);

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
            }
            if (intent != null) {
                try {
                    Log.d(TAG, "Starting activity with intent: " + intent);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to start activity: " + e.getMessage(), e);
                    Toast.makeText(context, "Error al abrir la actividad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.w(TAG, "No intent created for notification tipo: " + notificacion.getTipo());
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewTypeIndicator;
        ImageView ivIcon;
        TextView tvMensaje;
        TextView tvFecha;
        ImageView ivAction;

        ViewHolder(View itemView) {
            super(itemView);
            viewTypeIndicator = itemView.findViewById(R.id.viewTypeIndicator);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvMensaje = itemView.findViewById(R.id.tvNotificationMessage);
            tvFecha = itemView.findViewById(R.id.tvNotificationDate);
            ivAction = itemView.findViewById(R.id.ivAction);
        }
    }
}