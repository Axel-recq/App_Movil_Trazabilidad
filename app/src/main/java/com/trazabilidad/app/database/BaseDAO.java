package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trazabilidad.app.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {
    private static final String TAG = "BaseDAO";

    protected final DatabaseHelper dbHelper;
    protected final String tableName;

    protected BaseDAO(@NonNull Context context, @NonNull String tableName) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.tableName = tableName;
    }

    protected abstract ContentValues getContentValues(@NonNull T model);

    protected abstract String[] getColumns();

    protected abstract T cursorToModel(@NonNull Cursor cursor);

    protected abstract String getIdColumnName();

    protected abstract long getIdFromModel(@NonNull T model);


    public long insert(@NonNull T model) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            return db.insert(tableName, null, getContentValues(model));
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar en " + tableName, e);
            return -1;
        }
    }


    public int update(@NonNull T model) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            return db.update(
                    tableName,
                    getContentValues(model),
                    getIdColumnName() + " = ?",
                    new String[]{String.valueOf(getIdFromModel(model))}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar en " + tableName, e);
            return 0;
        }
    }


    public int delete(long id) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            return db.delete(
                    tableName,
                    getIdColumnName() + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar de " + tableName, e);
            return 0;
        }
    }


    @Nullable
    public T findById(long id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query(
                    tableName,
                    getColumns(),
                    getIdColumnName() + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            return cursor.moveToFirst() ? cursorToModel(cursor) : null;
        } catch (Exception e) {
            Log.e(TAG, "Error al buscar por ID en " + tableName, e);
            return null;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    @NonNull
    public List<T> findAll() {
        return findAll(null, null, null);
    }

    @NonNull
    public List<T> findAll(String selection, String[] selectionArgs, String orderBy) {
        List<T> items = new ArrayList<>();
        Cursor cursor = null;

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query(
                    tableName,
                    getColumns(),
                    selection,
                    selectionArgs,
                    null,
                    null,
                    orderBy
            );

            while (cursor.moveToNext()) {
                items.add(cursorToModel(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al consultar " + tableName, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return items;
    }


    protected void beginTransaction() {
        dbHelper.getWritableDatabase().beginTransaction();
    }


    protected void setTransactionSuccessful() {
        dbHelper.getWritableDatabase().setTransactionSuccessful();
    }


    protected void endTransaction() {
        dbHelper.getWritableDatabase().endTransaction();
    }


    protected boolean executeBatch(List<BatchOperation<T>> operations) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (BatchOperation<T> operation : operations) {
                boolean success = false;
                switch (operation.getType()) {
                    case INSERT:
                        success = db.insert(tableName, null, getContentValues(operation.getModel())) != -1;
                        break;
                    case UPDATE:
                        success = db.update(tableName, getContentValues(operation.getModel()),
                                getIdColumnName() + " = ?",
                                new String[]{String.valueOf(getIdFromModel(operation.getModel()))}) > 0;
                        break;
                    case DELETE:
                        success = db.delete(tableName, getIdColumnName() + " = ?",
                                new String[]{String.valueOf(getIdFromModel(operation.getModel()))}) > 0;
                        break;
                }

                if (!success && operation.isRequired()) {
                    return false;
                }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar operaciones por lotes en " + tableName, e);
            return false;
        } finally {
            db.endTransaction();
        }
    }


    public static class BatchOperation<T> {
        public enum Type {
            INSERT, UPDATE, DELETE
        }

        private final T model;
        private final Type type;
        private final boolean required;

        public BatchOperation(T model, Type type, boolean required) {
            this.model = model;
            this.type = type;
            this.required = required;
        }

        public T getModel() {
            return model;
        }

        public Type getType() {
            return type;
        }

        public boolean isRequired() {
            return required;
        }
    }
}