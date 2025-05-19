package com.trazabilidad.app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.trazabilidad.app.R;
import com.trazabilidad.app.activities.ListaProductosActivity;
import com.trazabilidad.app.controllers.ProductoController;
import com.trazabilidad.app.models.Producto;

import java.util.List;

public class StockService {

    private final Context context;
    private final ProductoController productoController;
    private static final int STOCK_BAJO_LIMITE = 10;
    private static final String CHANNEL_ID = "stock_channel";
    private static final String CHANNEL_NAME = "Alertas de Stock";
    private static final String CHANNEL_DESC = "Canal para alertas de bajo stock";

    public StockService(Context context) {
        this.context = context;
        this.productoController = new ProductoController(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Reduce el stock de un producto y emite alertas si es necesario
     * @param productoId ID del producto
     * @param cantidad Cantidad a descontar
     * @param callback Callback para notificar el resultado
     */
    public void descontarStock(int productoId, int cantidad, OperacionStockCallback callback) {
        if (cantidad <= 0) {
            callback.onError("La cantidad a descontar debe ser mayor que cero");
            return;
        }

        productoController.obtenerProductoPorId(productoId, new ProductoController.ProductoCallback() {
            @Override
            public void onSuccess(Producto producto) {
                // Verificar si hay suficiente stock
                if (producto.getCantidad() < cantidad) {
                    callback.onError("No hay suficiente stock disponible");
                    return;
                }

                // Verificar si el producto está activo
                if (!producto.isActivo()) {
                    callback.onError("No se puede usar un producto inactivo");
                    return;
                }

                // Calcular nuevo stock
                int nuevoStock = producto.getCantidad() - cantidad;
                producto.setCantidad(nuevoStock);

                // Actualizar producto en la base de datos
                productoController.actualizarProducto(producto, new ProductoController.OperacionCallback() {
                    @Override
                    public void onSuccess() {
                        // Verificar si hay que alertar por bajo stock
                        boolean bajoStock = nuevoStock < STOCK_BAJO_LIMITE;
                        callback.onSuccess(nuevoStock, bajoStock);

                        // Mostrar alerta si el stock es bajo
                        if (bajoStock) {
                            mostrarAlertaStockBajo(producto);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("Error al actualizar el stock: " + message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError("Error al obtener el producto: " + message);
            }
        });
    }

    /**
     * Muestra una alerta de stock bajo mediante notificación y Toast
     * @param producto Producto con stock bajo
     */
    private void mostrarAlertaStockBajo(Producto producto) {
        // Mostrar alerta en Toast
        Toast.makeText(context,
                "¡Alerta! El producto " + producto.getNombre() +
                        " tiene bajo stock (" + producto.getCantidad() + ")",
                Toast.LENGTH_LONG).show();

        // Crear intent que llevará a la lista de productos
        Intent intent = new Intent(context, ListaProductosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        // Crear y mostrar la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning) // Asegúrate de tener este icono en tus recursos
                .setContentTitle("Alerta de Stock Bajo")
                .setContentText("El producto " + producto.getNombre() + " tiene solo " +
                        producto.getCantidad() + " unidades disponibles")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(producto.getId(), builder.build());
    }

    public void aumentarStock(int productoId, int cantidad, OperacionStockCallback callback) {
        if (cantidad <= 0) {
            callback.onError("La cantidad a añadir debe ser mayor que cero");
            return;
        }

        productoController.obtenerProductoPorId(productoId, new ProductoController.ProductoCallback() {
            @Override
            public void onSuccess(Producto producto) {
                // Verificar si el producto está activo
                if (!producto.isActivo()) {
                    callback.onError("No se puede aumentar stock de un producto inactivo");
                    return;
                }

                // Calcular nuevo stock
                int nuevoStock = producto.getCantidad() + cantidad;
                producto.setCantidad(nuevoStock);

                // Actualizar producto en la base de datos
                productoController.actualizarProducto(producto, new ProductoController.OperacionCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(nuevoStock, nuevoStock < STOCK_BAJO_LIMITE);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("Error al actualizar el stock: " + message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError("Error al obtener el producto: " + message);
            }
        });
    }

    /**
     * Verifica todos los productos y notifica si hay alguno con stock bajo
     */
    public void verificarStockBajo() {
        productoController.obtenerProductosBajoStock(new ProductoController.ProductosCallback() {
            @Override
            public void onSuccess(List<Producto> productos) {
                if (!productos.isEmpty()) {
                    // Mostrar alerta si hay productos con stock bajo
                    for (Producto producto : productos) {
                        mostrarAlertaStockBajo(producto);
                    }
                }
            }

            @Override
            public void onError(String message) {
                // Log error pero no mostrar al usuario
                android.util.Log.e("StockService", "Error al verificar stock bajo: " + message);
            }
        });
    }

    // Interfaz para notificar resultados de operaciones de stock
    public interface OperacionStockCallback {
        void onSuccess(int nuevoStock, boolean bajoStock);
        void onError(String message);
    }
}