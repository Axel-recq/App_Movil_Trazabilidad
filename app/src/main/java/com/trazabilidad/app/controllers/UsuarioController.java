package com.trazabilidad.app.controllers;

import android.content.Context;

import com.trazabilidad.app.database.UsuarioDAO;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.SessionManager;

import java.util.List;

public class UsuarioController {

    private final UsuarioDAO usuarioDAO;
    private final SessionManager sessionManager;

    public UsuarioController(Context context) {
        usuarioDAO = new UsuarioDAO(context);
        sessionManager = new SessionManager(context);
    }

    public void registrarUsuario(Usuario usuario, OperacionCallback callback) {
        try {
            // Validar datos básicos
            if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
                callback.onError("El nombre es requerido");
                return;
            }
            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                callback.onError("El email es requerido");
                return;
            }
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                callback.onError("La contraseña es requerida");
                return;
            }
            if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
                callback.onError("El rol es requerido");
                return;
            }

            // Verificar si el email ya está registrado
            Usuario existingUser = usuarioDAO.obtenerUsuarioPorEmail(usuario.getEmail());
            if (existingUser != null) {
                callback.onError("El email ya está registrado");
                return;
            }

            // Insertar usuario
            boolean resultado = usuarioDAO.insertarUsuario(usuario);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al insertar el usuario en la base de datos");
            }
        } catch (Exception e) {
            callback.onError("Error al registrar el usuario: " + e.getMessage());
        }
    }


    public void actualizarUsuario(Usuario usuario, OperacionCallback callback) {
        try {
            // Validar datos básicos
            if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
                callback.onError("El nombre es requerido");
                return;
            }
            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                callback.onError("El email es requerido");
                return;
            }

            // Verificar si el email ya está en uso por otro usuario
            Usuario existingUser = usuarioDAO.obtenerUsuarioPorEmail(usuario.getEmail());
            if (existingUser != null && existingUser.getId() != usuario.getId()) {
                callback.onError("El email ya está en uso por otro usuario");
                return;
            }

            // Actualizar usuario
            boolean resultado = usuarioDAO.actualizarUsuario(usuario);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al actualizar el usuario");
            }
        } catch (Exception e) {
            callback.onError("Error al actualizar el usuario: " + e.getMessage());
        }
    }

    public void eliminarUsuario(int usuarioId, OperacionCallback callback) {
        try {
            boolean resultado = usuarioDAO.eliminarUsuario(usuarioId);
            if (resultado) {
                callback.onSuccess();
            } else {
                callback.onError("Error al eliminar el usuario");
            }
        } catch (Exception e) {
            callback.onError("Error al eliminar el usuario: " + e.getMessage());
        }
    }

    public void obtenerUsuarioPorId(int usuarioId, UsuarioCallback callback) {
        try {
            Usuario usuario = usuarioDAO.obtenerUsuarioPorId(usuarioId);
            if (usuario != null) {
                callback.onSuccess(usuario);
            } else {
                callback.onError("Usuario no encontrado");
            }
        } catch (Exception e) {
            callback.onError("Error al obtener el usuario: " + e.getMessage());
        }
    }

    public void obtenerTodosLosUsuarios(UsuariosCallback callback) {
        try {
            List<Usuario> usuarios = usuarioDAO.obtenerTodosLosUsuarios();
            callback.onSuccess(usuarios);
        } catch (Exception e) {
            callback.onError("Error al obtener los usuarios: " + e.getMessage());
        }
    }

    public interface OperacionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface AuthCallback {
        void onSuccess(Usuario usuario);
        void onError(String message);
    }

    public interface UsuarioCallback {
        void onSuccess(Usuario usuario);
        void onError(String message);
    }

    public interface UsuariosCallback {
        void onSuccess(List<Usuario> usuarios);
        void onError(String message);
    }
}