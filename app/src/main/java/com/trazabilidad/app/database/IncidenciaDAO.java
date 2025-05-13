package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trazabilidad.app.models.Incidencia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class IncidenciaDAO {

    private static final String TAG = "IncidenciaDAO";

    private final DatabaseHelper dbHelper;

    // Estados de incidencia
    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_RESUELTO = "resuelto";
    private static final List<String> ESTADOS_VALIDOS = Arrays.asList(ESTADO_PENDIENTE, ESTADO_RESUELTO);

    public IncidenciaDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public IncidenciaDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public int getPendingIncidenciasCount() {
        return getIncidenciaCountByEstado(ESTADO_PENDIENTE);
    }

    public int getResolvedIncidenciasCount() {
        return getIncidenciaCountByEstado(ESTADO_RESUELTO);
    }

    private int getIncidenciaCountByEstado(String estado) {
        int count = 0;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_INCIDENCIAS +
                    " WHERE " + DatabaseHelper.COLUMN_INCIDENCIA_ESTADO + " = ?";
            try (Cursor cursor = db.rawQuery(query, new String[]{estado})) {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al contar incidencias con estado " + estado, e);
        }
        return count;
    }

    public boolean insertarIncidencia(Incidencia incidencia) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getIncidenciaContentValues(incidencia);
            long id = db.insert(DatabaseHelper.TABLE_INCIDENCIAS, null, values);
            if (id != -1) {
                incidencia.setId((int) id); // Actualizar el ID en el objeto
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar incidencia", e);
            return false;
        }
    }

    public boolean actualizarIncidencia(Incidencia incidencia) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getIncidenciaContentValues(incidencia);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_INCIDENCIAS,
                    values,
                    DatabaseHelper.COLUMN_INCIDENCIA_ID + " = ?",
                    new String[]{String.valueOf(incidencia.getId())}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar incidencia con ID " + incidencia.getId(), e);
            return false;
        }
    }

    public boolean actualizarEstadoIncidencia(int incidenciaId, String nuevoEstado) {
        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            Log.e(TAG, "Estado no válido: " + nuevoEstado);
            return false;
        }
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_INCIDENCIA_ESTADO, nuevoEstado);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_INCIDENCIAS,
                    values,
                    DatabaseHelper.COLUMN_INCIDENCIA_ID + " = ?",
                    new String[]{String.valueOf(incidenciaId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar estado de incidencia con ID " + incidenciaId, e);
            return false;
        }
    }

    public boolean eliminarIncidencia(int id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            int rowsAffected = db.delete(
                    DatabaseHelper.TABLE_INCIDENCIAS,
                    DatabaseHelper.COLUMN_INCIDENCIA_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar incidencia con ID " + id, e);
            return false;
        }
    }

    public Incidencia obtenerIncidenciaPorId(int id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_INCIDENCIAS,
                     getIncidenciaColumns(),
                     DatabaseHelper.COLUMN_INCIDENCIA_ID + " = ?",
                     new String[]{String.valueOf(id)},
                     null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursorToIncidencia(cursor);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener incidencia con ID " + id, e);
            return null;
        }
    }

    public List<Incidencia> obtenerIncidenciasPorUsuario(int usuarioId) {
        List<Incidencia> incidencias = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_INCIDENCIAS,
                     null,
                     DatabaseHelper.COLUMN_INCIDENCIA_USUARIO_ID + " = ?",
                     new String[]{String.valueOf(usuarioId)},
                     null, null, DatabaseHelper.COLUMN_INCIDENCIA_FECHA + " DESC")) {
            while (cursor.moveToNext()) {
                Incidencia incidencia = new Incidencia();
                incidencia.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_ID)));
                incidencia.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_TIPO)));
                incidencia.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_DESCRIPCION)));
                incidencia.setFecha(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_FECHA))));
                incidencia.setFotoUri(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_FOTO_URI)));
                incidencia.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_USUARIO_ID)));
                int pedidoIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_INCIDENCIA_PEDIDO_ID);
                if (!cursor.isNull(pedidoIdIndex)) {
                    incidencia.setPedidoId(cursor.getInt(pedidoIdIndex));
                } else {
                    incidencia.setPedidoId(0); // Cambiar a null si usas Integer
                }
                incidencia.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_ESTADO)));
                incidencias.add(incidencia);
            }
        }
        return incidencias;
    }

    public List<Incidencia> obtenerIncidenciasPorPedido(int pedidoId) {
        List<Incidencia> incidencias = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_INCIDENCIAS,
                     getIncidenciaColumns(),
                     DatabaseHelper.COLUMN_INCIDENCIA_PEDIDO_ID + " = ?",
                     new String[]{String.valueOf(pedidoId)},
                     null, null,
                     DatabaseHelper.COLUMN_INCIDENCIA_FECHA + " DESC")) {
            while (cursor.moveToNext()) {
                incidencias.add(cursorToIncidencia(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener incidencias para pedido " + pedidoId, e);
        }
        return incidencias;
    }

    private ContentValues getIncidenciaContentValues(Incidencia incidencia) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_TIPO, incidencia.getTipo());
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_DESCRIPCION, incidencia.getDescripcion());
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_FECHA, incidencia.getFecha().getTime());
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_FOTO_URI, incidencia.getFotoUri());
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_USUARIO_ID, incidencia.getUsuarioId());
        String estado = incidencia.getEstado() != null ? incidencia.getEstado() : ESTADO_PENDIENTE;
        if (!ESTADOS_VALIDOS.contains(estado)) {
            Log.w(TAG, "Estado no válido: " + estado + ", usando " + ESTADO_PENDIENTE);
            estado = ESTADO_PENDIENTE;
        }
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_ESTADO, estado);
        // Manejar pedidoId explícitamente
        if (incidencia.getPedidoId() > 0) {
            values.put(DatabaseHelper.COLUMN_INCIDENCIA_PEDIDO_ID, incidencia.getPedidoId());
        } else {
            values.putNull(DatabaseHelper.COLUMN_INCIDENCIA_PEDIDO_ID);
        }
        return values;
    }

    private String[] getIncidenciaColumns() {
        return new String[]{
                DatabaseHelper.COLUMN_INCIDENCIA_ID,
                DatabaseHelper.COLUMN_INCIDENCIA_TIPO,
                DatabaseHelper.COLUMN_INCIDENCIA_DESCRIPCION,
                DatabaseHelper.COLUMN_INCIDENCIA_FECHA,
                DatabaseHelper.COLUMN_INCIDENCIA_FOTO_URI,
                DatabaseHelper.COLUMN_INCIDENCIA_USUARIO_ID,
                DatabaseHelper.COLUMN_INCIDENCIA_PEDIDO_ID,
                DatabaseHelper.COLUMN_INCIDENCIA_ESTADO
        };
    }

    private Incidencia cursorToIncidencia(Cursor cursor) {
        Incidencia incidencia = new Incidencia();
        incidencia.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_ID)));
        incidencia.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_TIPO)));
        incidencia.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_DESCRIPCION)));
        long fechaMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_FECHA));
        incidencia.setFecha(new Date(fechaMillis));
        incidencia.setFotoUri(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_FOTO_URI)));
        incidencia.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INCIDENCIA_USUARIO_ID)));
        int pedidoIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_INCIDENCIA_PEDIDO_ID);
        if (pedidoIdIndex != -1 && !cursor.isNull(pedidoIdIndex)) {
            incidencia.setPedidoId(cursor.getInt(pedidoIdIndex));
        } else {
            incidencia.setPedidoId(0); // Compatible con int primitivo
        }
        int estadoIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_INCIDENCIA_ESTADO);
        String estado = estadoIndex != -1 && !cursor.isNull(estadoIndex) ?
                cursor.getString(estadoIndex) : ESTADO_PENDIENTE;
        if (!ESTADOS_VALIDOS.contains(estado)) {
            Log.w(TAG, "Estado no válido en BD: " + estado + ", usando " + ESTADO_PENDIENTE);
            estado = ESTADO_PENDIENTE;
        }
        incidencia.setEstado(estado);
        return incidencia;
    }
}