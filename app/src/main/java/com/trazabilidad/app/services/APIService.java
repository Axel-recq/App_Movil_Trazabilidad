package com.trazabilidad.app.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import com.trazabilidad.app.models.Incidencia;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Ubicacion;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class APIService {

    private final Context context;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public APIService(Context context) {
        this.context = context;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public void login(String email, String password, APICallback<Usuario> callback) {
        if (!isNetworkAvailable()) {
            callback.onError("No hay conexión a Internet");
            return;
        }

        executor.execute(() -> {
            try {
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("email", email);
                jsonParams.put("password", password);

                APIResponse<Usuario> response = performPostRequest(Constants.API_URL + "/login", jsonParams, this::parseUsuario);
                postResult(response, callback);
            } catch (JSONException e) {
                postError("Error al preparar la solicitud: " + e.getMessage(), callback);
            }
        });
    }

    public void obtenerPedidosAsignados(int usuarioId, APICallback<List<Pedido>> callback) {
        if (!isNetworkAvailable()) {
            callback.onError("No hay conexión a Internet");
            return;
        }

        executor.execute(() -> {
            try {
                String url = Constants.API_URL + "/pedidos/usuario/" + usuarioId;
                APIResponse<List<Pedido>> response = performGetRequest(url, this::parsePedidos);
                postResult(response, callback);
            } catch (Exception e) {
                postError("Error inesperado: " + e.getMessage(), callback);
            }
        });
    }

    public void enviarUbicacion(Ubicacion ubicacion, APICallback<Void> callback) {
        if (!isNetworkAvailable()) {
            callback.onError("No hay conexión a Internet");
            return;
        }

        executor.execute(() -> {
            try {
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("latitud", ubicacion.getLatitud());
                jsonParams.put("longitud", ubicacion.getLongitud());
                jsonParams.put("fecha", ubicacion.getFecha().getTime());
                jsonParams.put("usuario_id", ubicacion.getUsuarioId());
                if (ubicacion.getPedidoId() > 0) jsonParams.put("pedido_id", ubicacion.getPedidoId());

                APIResponse<Void> response = performPostRequest(Constants.API_URL + "/ubicaciones", jsonParams, json -> null);
                postResult(response, callback);
            } catch (JSONException e) {
                postError("Error al preparar la solicitud: " + e.getMessage(), callback);
            }
        });
    }

    public void registrarIncidencia(Incidencia incidencia, APICallback<Void> callback) {
        if (!isNetworkAvailable()) {
            callback.onError("No hay conexión a Internet");
            return;
        }

        executor.execute(() -> {
            try {
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("tipo", incidencia.getTipo());
                jsonParams.put("descripcion", incidencia.getDescripcion());
                jsonParams.put("fecha", incidencia.getFecha().getTime());
                jsonParams.put("usuario_id", incidencia.getUsuarioId());
                if (incidencia.getPedidoId() > 0) jsonParams.put("pedido_id", incidencia.getPedidoId());
                if (incidencia.getFotoUri() != null) jsonParams.put("foto_uri", incidencia.getFotoUri());

                APIResponse<Void> response = performPostRequest(Constants.API_URL + "/incidencias", jsonParams, json -> null);
                postResult(response, callback);
            } catch (JSONException e) {
                postError("Error al preparar la solicitud: " + e.getMessage(), callback);
            }
        });
    }

    private <T> APIResponse<T> performPostRequest(String urlString, JSONObject jsonParams, ResponseParser<T> parser) {
        try {
            HttpURLConnection conn = createConnection(urlString, "POST");
            writeRequestBody(conn, jsonParams);
            return handleResponse(conn, parser);
        } catch (IOException e) {
            return new APIResponse<>(false, null, "Error de red: " + e.getMessage());
        } catch (JSONException e) {
            return new APIResponse<>(false, null, "Error de parseo: " + e.getMessage());
        }
    }

    private <T> APIResponse<T> performGetRequest(String urlString, ResponseParser<T> parser) {
        try {
            HttpURLConnection conn = createConnection(urlString, "GET");
            return handleResponse(conn, parser);
        } catch (IOException e) {
            return new APIResponse<>(false, null, "Error de red: " + e.getMessage());
        } catch (JSONException e) {
            return new APIResponse<>(false, null, "Error de parseo: " + e.getMessage());
        }
    }

    private <T> APIResponse<T> handleResponse(HttpURLConnection conn, ResponseParser<T> parser) throws IOException, JSONException {
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            String response = readResponse(conn);
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.getBoolean("success")) {
                T data = parser.parse(jsonResponse.getJSONObject("data"));
                return new APIResponse<>(true, data, null);
            }
            return new APIResponse<>(false, null, jsonResponse.getString("message"));
        }
        return new APIResponse<>(false, null, "Error en la solicitud: " + responseCode);
    }

    private HttpURLConnection createConnection(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        if ("POST".equals(method) || "PUT".equals(method)) {
            conn.setDoOutput(true);
        }
        return conn;
    }

    private void writeRequestBody(HttpURLConnection conn, JSONObject jsonParams) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonParams.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private Usuario parseUsuario(JSONObject json) throws JSONException {
        Usuario usuario = new Usuario();
        usuario.setId(json.getInt("id"));
        usuario.setNombre(json.getString("nombre"));
        usuario.setEmail(json.getString("email"));
        usuario.setRol(json.getString("rol"));
        usuario.setTelefono(json.optString("telefono", ""));
        usuario.setActivo(json.getBoolean("activo"));
        return usuario;
    }

    private List<Pedido> parsePedidos(JSONObject json) throws JSONException {
        JSONArray jsonPedidos = json.getJSONArray("data");
        List<Pedido> pedidos = new ArrayList<>();
        for (int i = 0; i < jsonPedidos.length(); i++) {
            JSONObject jsonPedido = jsonPedidos.getJSONObject(i);
            Pedido pedido = new Pedido();
            pedido.setId(jsonPedido.getInt("id"));
            pedido.setNumero(jsonPedido.getString("numero"));
            pedido.setCliente(jsonPedido.getString("cliente"));
            pedidos.add(pedido);
        }
        return pedidos;
    }

    private <T> void postResult(APIResponse<T> result, APICallback<T> callback) {
        handler.post(() -> {
            if (result.isSuccess()) {
                callback.onSuccess(result.getData());
            } else {
                callback.onError(result.getMessage());
            }
        });
    }

    private void postError(String message, APICallback<?> callback) {
        handler.post(() -> callback.onError(message));
    }

    public interface APICallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private interface ResponseParser<T> {
        T parse(JSONObject json) throws JSONException;
    }

    private static class APIResponse<T> {
        private final boolean success;
        private final T data;
        private final String message;

        APIResponse(boolean success, T data, String message) {
            this.success = success;
            this.data = data;
            this.message = message;
        }

        boolean isSuccess() {
            return success;
        }

        T getData() {
            return data;
        }

        String getMessage() {
            return message;
        }
    }
}