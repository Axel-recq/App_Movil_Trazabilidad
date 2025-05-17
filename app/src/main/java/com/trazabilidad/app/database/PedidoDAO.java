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
            Log.e("PedidoDAO", "Error al obtener pedidos recientes", e);
        }
        return pedidos;
    }

    private int getPedidosCount(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PEDIDOS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public boolean insertarPedido(Pedido pedido) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getPedidoContentValues(pedido);
            long id = db.insert(DatabaseHelper.TABLE_PEDIDOS, null, values);
            return id != -1;
        } catch (Exception e) {
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
            return null;
        }
    }

    public List<Pedido> obtenerPedidosPorUsuario(int usuarioId) {
        List<Pedido> pedidos = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_PEDIDOS,
                     getPedidoColumns(),
                     DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID + " = ?",
                     new String[]{String.valueOf(usuarioId)},
                     null,
                     null,
                     DatabaseHelper.COLUMN_PEDIDO_FECHA + " DESC"
             )) {
            while (cursor.moveToNext()) {
                pedidos.add(cursorToPedido(cursor));
            }
        } catch (Exception e) {
            // Loggear el error o manejarlo según sea necesario
        }
        return pedidos;
    }

    public boolean confirmarEntrega(int pedidoId) {
        Pedido pedido = obtenerPedidoPorId(pedidoId);
        if (pedido != null) {
            pedido.setConfirmado(true);
            return actualizarPedido(pedido);
        }
        return false;
    }

    public void verificarDemora(Pedido pedido) {
        long horaActual = System.currentTimeMillis();
        if (pedido.getHoraEstimada() < horaActual && pedido.getHoraEntrega() == 0) {
            pedido.setAlertaDemora(true);
            pedido.setMotivoDemora("Retraso detectado");
            actualizarPedido(pedido);
            // Enviar notificación aquí
        }
    }
    public int getActivePedidosCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PEDIDOS
                + " WHERE " + DatabaseHelper.COLUMN_PEDIDO_ESTADO
                + " IN ('ASIGNADO', 'EN_RUTA', 'EN_PREPARACION', 'PENDIENTE')"; // Estados activos

        try (Cursor cursor = db.rawQuery(query, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } catch (Exception e) {
            Log.e("PedidoDAO", "Error en getActivePedidosCount", e);
            return 0;
        }
    }

    public int getPedidosCountByDate(Date date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long fechaMillis = date.getTime();

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PEDIDOS
                + " WHERE " + DatabaseHelper.COLUMN_PEDIDO_FECHA + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(fechaMillis)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } catch (Exception e) {
            Log.e("PedidoDAO", "Error en getPedidosCountByDate", e);
            return 0;
        }
    }
    private ContentValues getPedidoContentValues(Pedido pedido) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PEDIDO_NUMERO, pedido.getNumero());
        values.put(DatabaseHelper.COLUMN_PEDIDO_CLIENTE, pedido.getCliente());
        values.put(DatabaseHelper.COLUMN_PEDIDO_DIRECCION, pedido.getDireccion());
        values.put(DatabaseHelper.COLUMN_PEDIDO_FECHA, pedido.getFecha().getTime());
        values.put(DatabaseHelper.COLUMN_PEDIDO_ESTADO, pedido.getEstado());
        values.put(DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID, pedido.getUsuarioId());
        values.put(DatabaseHelper.COLUMN_PEDIDO_LATITUD, pedido.getLatitud());
        values.put(DatabaseHelper.COLUMN_PEDIDO_LONGITUD, pedido.getLongitud());
        values.put(DatabaseHelper.COLUMN_PEDIDO_OBSERVACIONES, pedido.getObservaciones());
        values.put(DatabaseHelper.COLUMN_PEDIDO_TOTAL, pedido.getTotal()); //
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
        pedido.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_USUARIO_ID)));
        pedido.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_LATITUD)));
        pedido.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_LONGITUD)));
        pedido.setObservaciones(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_OBSERVACIONES)));

        // Campos adicionales
        pedido.setHoraSalida(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_HORA_SALIDA)));
        pedido.setHoraEstimada(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_HORA_ESTIMADA)));
        pedido.setHoraEntrega(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_HORA_ENTREGA)));
        pedido.setAlertaDemora(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_ALERTA_DEMORA)) == 1);
        pedido.setMotivoDemora(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_MOTIVO_DEMORA)));
        pedido.setConfirmado(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_CONFIRMADO)) == 1);
        pedido.setTotal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEDIDO_TOTAL)));

        return pedido;
    }
}