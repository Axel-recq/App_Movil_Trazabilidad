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

    // Estados de incidencia
    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_RESUELTO = "resuelto";

    public IncidenciaDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context); // Usar singleton
    }

    public IncidenciaDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Obtiene el número de incidencias pendientes
     * @return El número de incidencias con estado pendiente
     */
    public int getPendingIncidenciasCount() {
        return getIncidenciaCountByEstado(ESTADO_PENDIENTE);
    }

    /**
     * Obtiene el número de incidencias resueltas
     * @return El número de incidencias con estado resuelto
     */
    public int getResolvedIncidenciasCount() {
        return getIncidenciaCountByEstado(ESTADO_RESUELTO);
    }

    /**
     * Método auxiliar para contar incidencias por estado
     * @param estado El estado a filtrar
     * @return El número de incidencias en el estado especificado
     */
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
            // Manejar error (podrías agregar un log aquí si es necesario)
        }
        return count;
    }

    // Método para insertar una nueva incidencia
    public boolean insertarIncidencia(Incidencia incidencia) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getIncidenciaContentValues(incidencia);
            long id = db.insert(DatabaseHelper.TABLE_INCIDENCIAS, null, values);
            return id != -1;
        } catch (Exception e) {
            return false;
        }
    }

    // Método para obtener incidencias por usuario
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
                     null)) {
            while (cursor.moveToNext()) {
                incidencias.add(cursorToIncidencia(cursor));
            }
        } catch (Exception e) {
            // Manejar error si es necesario
        }
        return incidencias;
    }

    // Método para actualizar una incidencia existente
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

    // Método para eliminar una incidencia
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

    // Método para obtener una incidencia por su ID
    public Incidencia obtenerIncidenciaPorId(int id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_INCIDENCIAS,
                     getIncidenciaColumns(),
                     DatabaseHelper.COLUMN_INCIDENCIA_ID + " = ?",
                     new String[]{String.valueOf(id)},
                     null,
                     null,
                     null)) {
            if (cursor.moveToFirst()) {
                return cursorToIncidencia(cursor);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // Método para generar los valores para ContentValues
    private ContentValues getIncidenciaContentValues(Incidencia incidencia) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_TIPO, incidencia.getTipo());
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_DESCRIPCION, incidencia.getDescripcion());
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_FECHA, incidencia.getFecha().getTime());
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_FOTO_URI, incidencia.getFotoUri());
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_USUARIO_ID, incidencia.getUsuarioId());
        values.put(DatabaseHelper.COLUMN_INCIDENCIA_ESTADO, incidencia.getEstado() != null ?
                incidencia.getEstado() : ESTADO_PENDIENTE);  // Valor por defecto: pendiente

        if (incidencia.getPedidoId() > 0) {
            values.put(DatabaseHelper.COLUMN_INCIDENCIA_PEDIDO_ID, incidencia.getPedidoId());
        }
        return values;
    }

    // Método para obtener los nombres de columnas
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

    // Método para convertir cursor a objeto Incidencia
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

        int estadoIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_INCIDENCIA_ESTADO);
        if (estadoIndex != -1 && !cursor.isNull(estadoIndex)) {
            incidencia.setEstado(cursor.getString(estadoIndex));
        } else {
            incidencia.setEstado(ESTADO_PENDIENTE);
        }

        return incidencia;
    }
}