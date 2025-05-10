package com.trazabilidad.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TipoIncidenciaDAO {

    private DatabaseHelper dbHelper;

    public TipoIncidenciaDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<String> obtenerTiposIncidencias() {
        List<String> tipos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_TIPO_NOMBRE + " FROM " + DatabaseHelper.TABLE_TIPOS_INCIDENCIAS, null);
        if (cursor.moveToFirst()) {
            do {
                tipos.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tipos;
    }
}