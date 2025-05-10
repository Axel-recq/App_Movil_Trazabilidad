package com.trazabilidad.app.database;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Abre trazabilidad.db para que el Database Inspector la detecte como "open"
        SQLiteDatabase trazDb = DatabaseHelper
                .getInstance(this)
                .getWritableDatabase();

    }
}