package com.trazabilidad.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.trazabilidad.app.models.Usuario;

public class SessionManager {

    private static final String PREF_NAME = "TrazabilidadSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROL = "userRol";
    private static final String KEY_USER_TELEFONO = "userTelefono";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(Usuario usuario) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, usuario.getId());
        editor.putString(KEY_USER_NAME, usuario.getNombre());
        editor.putString(KEY_USER_EMAIL, usuario.getEmail());
        editor.putString(KEY_USER_ROL, usuario.getRol());
        editor.putString(KEY_USER_TELEFONO, usuario.getTelefono());
        editor.commit();
    }

    public Usuario getUsuarioDetails() {
        Usuario usuario = new Usuario();
        usuario.setId(pref.getInt(KEY_USER_ID, 0));
        usuario.setNombre(pref.getString(KEY_USER_NAME, ""));
        usuario.setEmail(pref.getString(KEY_USER_EMAIL, ""));
        usuario.setRol(pref.getString(KEY_USER_ROL, ""));
        usuario.setTelefono(pref.getString(KEY_USER_TELEFONO, ""));
        usuario.setActivo(true);
        return usuario;
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }
}
