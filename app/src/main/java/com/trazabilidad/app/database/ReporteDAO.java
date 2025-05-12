package com.trazabilidad.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ReporteDAO {

    private final DatabaseHelper dbHelper;

    // Constructor que inicializa el DatabaseHelper
    public ReporteDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public double obtenerTiempoPromedioEntrega() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT AVG(" + DatabaseHelper.COLUMN_PEDIDO_HORA_ENTREGA + " - " +
                             DatabaseHelper.COLUMN_PEDIDO_HORA_SALIDA + ") FROM " +
                             DatabaseHelper.TABLE_PEDIDOS + " WHERE " +
                             DatabaseHelper.COLUMN_PEDIDO_HORA_ENTREGA + " > 0", null)) {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0) / 1000 / 60; // Minutos
            }
            return 0;
        }
    }
    public double obtenerPorcentajeEntregasExitosas() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT COUNT(*) * 100.0 / (SELECT COUNT(*) FROM " +
                             DatabaseHelper.TABLE_PEDIDOS + ") FROM " +
                             DatabaseHelper.TABLE_PEDIDOS + " WHERE " +
                             DatabaseHelper.COLUMN_PEDIDO_ESTADO + " = 'ENTREGADO'", null)) {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
            return 0;
        }
    }
    // Método para obtener el ratio de entregas por hora
    public double obtenerRatioEntregasPorHora() {
        double ratio = 0.0;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT COUNT(*) * 1.0 / (MAX(hora_entrega) - MIN(hora_salida)) * 3600000 FROM view_reportes WHERE entrega_exitosa = 1",
                     null
             )) {
            if (cursor.moveToFirst()) {
                ratio = cursor.getDouble(0);
            }
        } catch (Exception e) {
            // Manejar error
        }
        return ratio;
    }


}