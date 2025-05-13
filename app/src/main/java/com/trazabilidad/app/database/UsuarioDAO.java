package com.trazabilidad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trazabilidad.app.models.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private final DatabaseHelper dbHelper;

    public UsuarioDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean insertarUsuario(Usuario usuario) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getUsuarioContentValues(usuario);
            long id = db.insert(DatabaseHelper.TABLE_USUARIOS, null, values);
            return id != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean actualizarUsuario(Usuario usuario) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = getUsuarioContentValues(usuario);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USUARIOS,
                    values,
                    DatabaseHelper.COLUMN_USUARIO_ID + " = ?",
                    new String[]{String.valueOf(usuario.getId())}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean eliminarUsuario(int id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USUARIO_ACTIVO, 0);
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USUARIOS,
                    values,
                    DatabaseHelper.COLUMN_USUARIO_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public Usuario obtenerUsuarioPorId(int id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_USUARIOS,
                     getUsuarioColumns(),
                     DatabaseHelper.COLUMN_USUARIO_ID + " = ?",
                     new String[]{String.valueOf(id)},
                     null,
                     null,
                     null
             )) {
            if (cursor.moveToFirst()) {
                return cursorToUsuario(cursor);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_USUARIOS,
                     getUsuarioColumns(),
                     DatabaseHelper.COLUMN_USUARIO_EMAIL + " = ?",
                     new String[]{email},
                     null,
                     null,
                     null
             )) {
            if (cursor.moveToFirst()) {
                return cursorToUsuario(cursor);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_USUARIOS,
                     getUsuarioColumns(),
                     null,
                     null,
                     null,
                     null,
                     DatabaseHelper.COLUMN_USUARIO_NOMBRE + " ASC"
             )) {
            while (cursor.moveToNext()) {
                usuarios.add(cursorToUsuario(cursor));
            }
        } catch (Exception e) {
        }
        return usuarios;
    }

    private ContentValues getUsuarioContentValues(Usuario usuario) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USUARIO_NOMBRE, usuario.getNombre());
        values.put(DatabaseHelper.COLUMN_USUARIO_EMAIL, usuario.getEmail());
        values.put(DatabaseHelper.COLUMN_USUARIO_PASSWORD, usuario.getPassword());
        values.put(DatabaseHelper.COLUMN_USUARIO_ROL, usuario.getRol());
        values.put(DatabaseHelper.COLUMN_USUARIO_TELEFONO, usuario.getTelefono());
        values.put(DatabaseHelper.COLUMN_USUARIO_ACTIVO, usuario.isActivo() ? 1 : 0);
        return values;
    }

    private String[] getUsuarioColumns() {
        return new String[]{
                DatabaseHelper.COLUMN_USUARIO_ID,
                DatabaseHelper.COLUMN_USUARIO_NOMBRE,
                DatabaseHelper.COLUMN_USUARIO_EMAIL,
                DatabaseHelper.COLUMN_USUARIO_PASSWORD,
                DatabaseHelper.COLUMN_USUARIO_ROL,
                DatabaseHelper.COLUMN_USUARIO_TELEFONO,
                DatabaseHelper.COLUMN_USUARIO_ACTIVO
        };
    }

    private Usuario cursorToUsuario(Cursor cursor) {
        Usuario usuario = new Usuario();
        usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_ID)));
        usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_NOMBRE)));
        usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_EMAIL)));
        usuario.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_PASSWORD)));
        usuario.setRol(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_ROL)));
        usuario.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_TELEFONO)));
        usuario.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_ACTIVO)) == 1);
        return usuario;
    }
}