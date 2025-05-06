package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.models.Incidencia;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IncidenciaDAO {

    private final DatabaseHelper dbHelper;

    public IncidenciaDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean insertarIncidencia(Incidencia incidencia) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getIncidenciaContentValues(incidencia);
            long id = db.insert(DatabaseHelper.TABLE_INCIDENCIAS, null, values);
            return id != -1;
        } catch (Exception e) {

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
                     null,
                     null,
                     null
             )) {
            if (cursor.moveToFirst()) {
                return cursorToIncidencia(cursor);
            }
            return null;
        } catch (Exception e) {

            return null;
        }
    }


    public List<Incidencia> obtenerIncidenciasPorUsuario(int usuarioId) {
        List<Incidencia> incidencias = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_INCIDENCIAS,
                     getIncidenciaColumns(),
                     DatabaseHelper.COLUMN_INCIDENCIA_USUARIO_ID + " = ?",
                     new String[]{String.valueOf(usuarioId)},
                     null,
                     null,
                     DatabaseHelper.COLUMN_INCIDENCIA_FECHA + " DESC"
             )) {
            while (cursor.moveToNext()) {
                incidencias.add(cursorToIncidencia(cursor));
            }
        } catch (Exception e) {

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
                     null,
                     null,
                     DatabaseHelper.COLUMN_INCIDENCIA_FECHA + " DESC"
             )) {
            while (cursor.moveToNext()) {
                incidencias.add(cursorToIncidencia(cursor));
            }
        } catch (Exception e) {
            // Loggear el error o manejarlo según sea necesario
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
        if (incidencia.getPedidoId() > 0) {
            values.put(DatabaseHelper.COLUMN_INCIDENCIA_PEDIDO_ID, incidencia.getPedidoId());
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
                DatabaseHelper.COLUMN_INCIDENCIA_PEDIDO_ID
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
        }
        return incidencia;
    }
}