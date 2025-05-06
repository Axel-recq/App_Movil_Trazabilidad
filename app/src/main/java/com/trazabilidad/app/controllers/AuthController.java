package com.trazabilidad.app.controllers;

import android.content.Context;

import com.trazabilidad.app.database.UsuarioDAO;
import com.trazabilidad.app.models.Usuario;

public class AuthController {

    private final UsuarioDAO usuarioDAO;

    public AuthController(Context context) {
        usuarioDAO = new UsuarioDAO(context);
    }

    public void login(String email, String password, AuthCallback callback) {
        try {
            Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
            if (usuario == null) {
                callback.onError("Usuario no encontrado");
                return;
            }
            if (!usuario.getPassword().equals(password)) {
                callback.onError("Contraseña incorrecta");
                return;
            }
            if (!usuario.isActivo()) {
                callback.onError("Usuario inactivo. Contacte al administrador");
                return;
            }
            callback.onSuccess(usuario);
        } catch (Exception e) {
            callback.onError("Error durante el login: " + e.getMessage());
        }
    }

    public void cambiarPassword(int usuarioId, String passwordActual, String passwordNuevo, OperacionCallback callback) {
        try {
            Usuario usuario = usuarioDAO.obtenerUsuarioPorId(usuarioId);
            if (usuario == null) {
                callback.onError("Usuario no encontrado");
                return;
            }
            if (!usuario.getPassword().equals(passwordActual)) {
                callback.onError("Contraseña actual incorrecta");
                return;
            }
            usuario.setPassword(passwordNuevo);
            boolean resultado = usuarioDAO.actualizarUsuario(usuario);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al actualizar la contraseña");
            }
        } catch (Exception e) {
            callback.onError("Error al cambiar la contraseña: " + e.getMessage());
        }
    }

    public interface AuthCallback {
        void onSuccess(Usuario usuario);
        void onError(String message);
    }

    public interface OperacionCallback {
        void onSuccess();
        void onError(String message);
    }
}