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

    // Método para obtener el tiempo promedio de entrega en minutos
    public double obtenerTiempoPromedioEntrega() {
        double promedio = 0.0;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT AVG(tiempo_entrega_minutos) FROM view_reportes WHERE entrega_exitosa = 1",
                     null
             )) {
            if (cursor.moveToFirst()) {
                promedio = cursor.getDouble(0);
            }
        } catch (Exception e) {
            // Manejar error
        }
        return promedio;
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

    // Método para obtener el porcentaje de entregas exitosas
    public double obtenerPorcentajeEntregasExitosas() {
        double porcentaje = 0.0;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT (SUM(CASE WHEN entrega_exitosa = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) FROM view_reportes",
                     null
             )) {
            if (cursor.moveToFirst()) {
                porcentaje = cursor.getDouble(0);
            }
        } catch (Exception e) {
            // Manejar error
        }
        return porcentaje;
    }
}