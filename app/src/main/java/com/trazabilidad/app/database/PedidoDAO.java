package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trazabilidad.app.models.Pedido;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PedidoDAO {
    private static final String TAG = "PedidoDAO";
    private final DatabaseHelper dbHelper;

    public PedidoDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public PedidoDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Pedido> getRecentPedidos(int limit) {
        List<Pedido> pedidos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PEDIDOS,
                     getPedidoColumns(),
                     null,
                     null,
                     null,
                     null,
                     DatabaseHelper.COLUMN_PEDIDO_FECHA + " DESC",
                     String.valueOf(limit))) {
            while (cursor.moveToNext()) {
                pedidos.add(cursorToPedido(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener pedidos recientes", e);
        }
        return pedidos;
    }

    public int getPedidosCount() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PEDIDOS, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener conteo de pedidos", e);
            return 0;
        }
    }

    public boolean insertarPedido(Pedido pedido) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getPedidoContentValues(pedido);
            long id = db.insert(DatabaseHelper.TABLE_PEDIDOS, null, values);
            if (id != -1) {
                pedido.setId((int) id);
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar pedido", e);
            return false;
        }
    }

    public boolean actualizarPedido(Pedido pedido) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getPedidoContentValues(pedido);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PEDIDOS,
                    values,
                    DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                    new String[]{String.valueOf(pedido.getId())}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar pedido", e);
            return false;
        }
    }

    public boolean eliminarPedido(int id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            int rowsAffected = db.delete(
                    DatabaseHelper.TABLE_PEDIDOS,
                    DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar pedido", e);
            return false;
        }
    }

    public Pedido obtenerPedidoPorId(int id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PEDIDOS,
                     getPedidoColumns(),
                     DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                     new String[]{String.valueOf(id)},
                     null,
                     null,
                     null
             )) {
            if (cursor.moveToFirst()) {
                return cursorToPedido(cursor);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener pedido por ID", e);
            return null;
        }
    }

    public List<Pedido> obtenerPedidosPorUsuario(Integer usuarioId) {
        List<Pedido> pedidos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PEDIDOS,
                     getPedidoColumns(),
                     usuarioId != null ? DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID + " = ?" : null,
                     usuarioId != null ? new String[]{String.valueOf(usuarioId)} : null,
                     null,
                     null,
                     DatabaseHelper.COLUMN_PEDIDO_FECHA + " DESC"
             )) {
            while (cursor.moveToNext()) {
                pedidos.add(cursorToPedido(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener pedidos por usuario", e);
        }
        return pedidos;
    }

    public List<Pedido> obtenerPedidosPorCliente(String cliente) {
        List<Pedido> pedidos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PEDIDOS,
                     getPedidoColumns(),
                     DatabaseHelper.COLUMN_PEDIDO_CLIENTE + " LIKE ?",
                     new String[]{"%" + cliente + "%"},
                     null,
                     null,
                     DatabaseHelper.COLUMN_PEDIDO_FECHA + " DESC"
             )) {
            while (cursor.moveToNext()) {
                pedidos.add(cursorToPedido(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener pedidos por cliente", e);
        }
        return pedidos;
    }

    public boolean confirmarEntrega(int pedidoId) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PEDIDO_ESTADO, "ENTREGADO");
            values.put(DatabaseHelper.COLUMN_PEDIDO_HORA_ENTREGA, System.currentTimeMillis());
            values.put(DatabaseHelper.COLUMN_PEDIDO_CONFIRMADO, 1);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PEDIDOS,
                    values,
                    DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                    new String[]{String.valueOf(pedidoId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al confirmar entrega", e);
            return false;
        }
    }
    public boolean actualizarEstado(int pedidoId, String nuevoEstado) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PEDIDO_ESTADO, nuevoEstado);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PEDIDOS,
                    values,
                    DatabaseHelper.COLUMN_PEDIDO_ID + " = ?",
                    new String[]{String.valueOf(pedidoId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar estado del pedido", e);
            return false;
        }
    }
    public void verificarDemora(Pedido pedido) {
        if (pedido == null || pedido.getHoraEstimada() == null) {
            return;
        }
        long horaActual = System.currentTimeMillis();
        if (pedido.getHoraEstimada() < horaActual && (pedido.getHoraEntrega() == null || pedido.getHoraEntrega() == 0)) {
            pedido.setAlertaDemora(true);
            if (pedido.getMotivoDemora() == null) {
                pedido.setMotivoDemora("Retraso detectado automáticamente");
            }
            actualizarPedido(pedido);
            // TODO: Implementar envío de notificación
            // Notificacion notificacion = new Notificacion();
            // notificacion.setTipo(Notificacion.Tipo.DEMORA.name());
            // notificacion.setMensaje("Demora en pedido #" + pedido.getNumero());
            // notificacion.setFecha(new Date());
            // notificacion.setReferenciaId(pedido.getId());
            // NotificationManager.getInstance(context).insertarNotificacion(notificacion);
        }
    }

    public int getActivePedidosCount() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PEDIDOS +
                             " WHERE " + DatabaseHelper.COLUMN_PEDIDO_ESTADO +
                             " IN ('ASIGNADO', 'EN_RUTA', 'EN_PREPARACION', 'PENDIENTE')",
                     null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error en getActivePedidosCount", e);
            return 0;
        }
    }

    public int getPedidosCountByDate(Date date) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            long startOfDay = date.getTime();
            long endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1;
            String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PEDIDOS +
                    " WHERE " + DatabaseHelper.COLUMN_PEDIDO_FECHA + " BETWEEN ? AND ?";
            try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay)})) {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0);
                }
                return 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en getPedidosCountByDate", e);
            return 0;
        }
    }

    private ContentValues getPedidoContentValues(Pedido pedido) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PEDIDO_NUMERO, pedido.getNumero());
        values.put(DatabaseHelper.COLUMN_PEDIDO_CLIENTE, pedido.getCliente());
        values.put(DatabaseHelper.COLUMN_PEDIDO_DIRECCION, pedido.getDireccion());
        values.put(DatabaseHelper.COLUMN_PEDIDO_FECHA, pedido.getFecha() != null ? pedido.getFecha().getTime() : null);
        values.put(DatabaseHelper.COLUMN_PEDIDO_ESTADO, pedido.getEstado());
        values.put(DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID, pedido.getUsuarioId());
        values.put(DatabaseHelper.COLUMN_PEDIDO_LATITUD, pedido.getLatitud());
        values.put(DatabaseHelper.COLUMN_PEDIDO_LONGITUD, pedido.getLongitud());
        values.put(DatabaseHelper.COLUMN_PEDIDO_OBSERVACIONES, pedido.getObservaciones());
        values.put(DatabaseHelper.COLUMN_PEDIDO_TOTAL, pedido.getTotal());
        values.put(DatabaseHelper.COLUMN_PEDIDO_HORA_SALIDA, pedido.getHoraSalida());
        values.put(DatabaseHelper.COLUMN_PEDIDO_HORA_ESTIMADA, pedido.getHoraEstimada());
        values.put(DatabaseHelper.COLUMN_PEDIDO_HORA_ENTREGA, pedido.getHoraEntrega());
        values.put(DatabaseHelper.COLUMN_PEDIDO_ALERTA_DEMORA, pedido.isAlertaDemora() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PEDIDO_MOTIVO_DEMORA, pedido.getMotivoDemora());
        values.put(DatabaseHelper.COLUMN_PEDIDO_CONFIRMADO, pedido.isConfirmado() ? 1 : 0);
        return values;
    }

    private String[] getPedidoColumns() {
        return new String[]{
                DatabaseHelper.COLUMN_PEDIDO_ID,
                DatabaseHelper.COLUMN_PEDIDO_NUMERO,
                DatabaseHelper.COLUMN_PEDIDO_CLIENTE,
                DatabaseHelper.COLUMN_PEDIDO_DIRECCION,
                DatabaseHelper.COLUMN_PEDIDO_FECHA,
                DatabaseHelper.COLUMN_PEDIDO_ESTADO,
                DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID,
                DatabaseHelper.COLUMN_PEDIDO_LATITUD,
                DatabaseHelper.COLUMN_PEDIDO_LONGITUD,
                DatabaseHelper.COLUMN_PEDIDO_OBSERVACIONES,
                DatabaseHelper.COLUMN_PEDIDO_HORA_SALIDA,
                DatabaseHelper.COLUMN_PEDIDO_HORA_ESTIMADA,
                DatabaseHelper.COLUMN_PEDIDO_HORA_ENTREGA,
                DatabaseHelper.COLUMN_PEDIDO_ALERTA_DEMORA,
                DatabaseHelper.COLUMN_PEDIDO_MOTIVO_DEMORA,
                DatabaseHelper.COLUMN_PEDIDO_CONFIRMADO,
                DatabaseHelper.COLUMN_PEDIDO_TOTAL
        };
    }

    private Pedido cursorToPedido(Cursor cursor) {
        Pedido pedido = new Pedido();
        pedido.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_ID)));
        pedido.setNumero(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_NUMERO)));
        pedido.setCliente(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_CLIENTE)));
        pedido.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_DIRECCION)));
        long fechaMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_FECHA));
        pedido.setFecha(new Date(fechaMillis));
        pedido.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_ESTADO)));

        int usuarioIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID);
        if (!cursor.isNull(usuarioIdIndex)) {
            pedido.setUsuarioId(cursor.getInt(usuarioIdIndex));
        }

        int latitudIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_LATITUD);
        if (!cursor.isNull(latitudIndex)) {
            pedido.setLatitud(cursor.getDouble(latitudIndex));
        }

        int longitudIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_LONGITUD);
        if (!cursor.isNull(longitudIndex)) {
            pedido.setLongitud(cursor.getDouble(longitudIndex));
        }

        int observacionesIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_OBSERVACIONES);
        if (!cursor.isNull(observacionesIndex)) {
            pedido.setObservaciones(cursor.getString(observacionesIndex));
        }

        int totalIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_TOTAL);
        if (!cursor.isNull(totalIndex)) {
            pedido.setTotal(cursor.getDouble(totalIndex));
        }

        int horaSalidaIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_HORA_SALIDA);
        if (!cursor.isNull(horaSalidaIndex)) {
            pedido.setHoraSalida(cursor.getLong(horaSalidaIndex));
        }

        int horaEstimadaIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_HORA_ESTIMADA);
        if (!cursor.isNull(horaEstimadaIndex)) {
            pedido.setHoraEstimada(cursor.getLong(horaEstimadaIndex));
        }

        int horaEntregaIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_HORA_ENTREGA);
        if (!cursor.isNull(horaEntregaIndex)) {
            pedido.setHoraEntrega(cursor.getLong(horaEntregaIndex));
        }

        pedido.setAlertaDemora(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_ALERTA_DEMORA)) == 1);

        int motivoDemoraIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_MOTIVO_DEMORA);
        if (!cursor.isNull(motivoDemoraIndex)) {
            pedido.setMotivoDemora(cursor.getString(motivoDemoraIndex));
        }

        pedido.setConfirmado(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_CONFIRMADO)) == 1);

        return pedido;
    }
}