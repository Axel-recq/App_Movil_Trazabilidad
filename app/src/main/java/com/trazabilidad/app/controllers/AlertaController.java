package com.trazabilidad.app.controllers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.trazabilidad.app.R;
import com.trazabilidad.app.activities.MainActivity;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.models.Pedido;

import java.util.List;

public class AlertaController {

    private static final String CHANNEL_ID = "AlertaDemoraChannel";
    private static final int NOTIFICATION_ID = 100;

    private final Context context;
    private final PedidoDAO pedidoDAO;
    private final NotificationManager notificationManager;

    public AlertaController(Context context) {
        this.context = context;
        this.pedidoDAO = new PedidoDAO(context);
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    public void verificarDemoras(int usuarioId, AlertaCallback callback) {
        try {
            List<Pedido> pedidos = pedidoDAO.obtenerPedidosPorUsuario(usuarioId);
            boolean demoraDetectada = false;

            for (Pedido pedido : pedidos) {
                // Verificar si el pedido no está entregado y tiene una hora estimada válida
                if (pedido.getHoraEntrega() == 0 && pedido.getHoraEstimada() != 0) {
                    long horaActual = System.currentTimeMillis();
                    long horaEstimada = pedido.getHoraEstimada();

                    // Detectar demora si la hora actual supera la hora estimada
                    if (horaActual > horaEstimada && !pedido.isAlertaDemora()) {
                        pedido.setAlertaDemora(true);
                        pedido.setMotivoDemora("Retraso detectado: la hora estimada de entrega ha sido superada");
                        boolean actualizado = pedidoDAO.actualizarPedido(pedido);
                        if (actualizado) {
                            generarNotificacion(pedido);
                            demoraDetectada = true;
                        }
                    }
                }
            }

            if (demoraDetectada) {
                callback.onDemoraDetectada("Se han detectado demoras en uno o más pedidos");
            } else {
                callback.onSuccess("No se detectaron demoras");
            }
        } catch (Exception e) {
            callback.onError("Error al verificar demoras: " + e.getMessage());
        }
    }

    public void limpiarAlerta(int pedidoId, OperacionCallback callback) {
        try {
            Pedido pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
            if (pedido == null) {
                callback.onError("Pedido no encontrado");
                return;
            }
            if (!pedido.isAlertaDemora()) {
                callback.onSuccess(); // No hay alerta para limpiar
                return;
            }
            pedido.setAlertaDemora(false);
            pedido.setMotivoDemora(null);
            boolean actualizado = pedidoDAO.actualizarPedido(pedido);
            if (actualizado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al limpiar la alerta");
            }
        } catch (Exception e) {
            callback.onError("Error al limpiar la alerta: " + e.getMessage());
        }
    }

    private void generarNotificacion(Pedido pedido) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("pedidoId", pedido.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener este recurso
                .setContentTitle("Alerta de Demora")
                .setContentText("El pedido #" + pedido.getId() + " está retrasado: " + pedido.getMotivoDemora())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID + pedido.getId(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alertas de Demora",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de demoras en pedidos");
            notificationManager.createNotificationChannel(channel);
        }
    }

    public interface AlertaCallback {
        void onSuccess(String message);
        void onDemoraDetectada(String message);
        void onError(String message);
    }

    public interface OperacionCallback {
        void onSuccess();
        void onError(String message);
    }
}