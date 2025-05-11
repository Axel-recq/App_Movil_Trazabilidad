package com.trazabilidad.app.database;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            SQLiteDatabase trazDb = DatabaseHelper.getInstance(this).getWritableDatabase();
            Log.d("App", "Base de datos abierta correctamente");
        } catch (Exception e) {
            Log.e("App", "Error al abrir la base de datos", e);
        }

    }
}