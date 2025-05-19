package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.CalificacionController;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.models.Calificacion;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalificarPedidoActivity extends AppCompatActivity {

    private static final String TAG = "CalificarPedidoActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvPedidoNumero;
    private TextView tvCliente;
    private TextView tvDireccion;
    private TextView tvFecha;
    private RatingBar ratingBar;
    private EditText etComentario;
    private Button btnEnviarCalificacion;
    private View rootView;

    // Data controllers
    private SessionManager sessionManager;
    private PedidoDAO pedidoDAO;
    private CalificacionController calificacionController;
    private Pedido pedido;
    private int pedidoId;

    // Formatter
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificar_pedido);

        inicializarUI();
        configurarToolbar();
        inicializarControladores();

        // Obtener pedidoId del Intent
        pedidoId = getIntent().getIntExtra("pedidoId", -1);
        if (pedidoId == -1) {
            mostrarError("ID de pedido inválido.");
            return;
        }

        // Cargar datos del pedido
        cargarDatosPedido();
    }

    private void inicializarUI() {
        rootView = findViewById(android.R.id.content);
        toolbar = findViewById(R.id.toolbar);
        tvPedidoNumero = findViewById(R.id.tvPedidoNumero);
        tvCliente = findViewById(R.id.tvCliente);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvFecha = findViewById(R.id.tvFecha);
        ratingBar = findViewById(R.id.ratingBar);
        etComentario = findViewById(R.id.etComentario);
        btnEnviarCalificacion = findViewById(R.id.btnEnviarCalificacion);

        // Configurar animación del RatingBar
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                // Pequeña animación visual al seleccionar rating
                ratingBar.setScaleX(0.8f);
                ratingBar.setScaleY(0.8f);
                ratingBar.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        });
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.calificar_pedido);
        }
    }

    private void inicializarControladores() {
        sessionManager = new SessionManager(this);
        pedidoDAO = new PedidoDAO(this);
        calificacionController = new CalificacionController(this);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private void cargarDatosPedido() {
        // Validar sesión
        if (!sessionManager.isLoggedIn()) {
            mostrarError("Sesión expirada. Por favor, inicia sesión.");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // TEMPORAL: Permitir que administradores y repartidores califiquen pedidos para pruebas
        // TODO: Restaurar la validación `sessionManager.isCliente()` y la comprobación del cliente
        // cuando se desee restringir nuevamente a clientes.
        /*
        if (!sessionManager.isCliente()) {
            mostrarError("Solo los clientes pueden calificar pedidos.");
            finish();
            return;
        }
        */

        // Obtener pedido
        pedido = pedidoDAO.obtenerPedidoPorId(pedidoId);
        if (pedido == null) {
            mostrarError("Pedido no encontrado.");
            return;
        }

        // Validar estado del pedido
        if (!"ENTREGADO".equalsIgnoreCase(pedido.getEstado())) {
            mostrarError("Solo se pueden calificar pedidos entregados.");
            return;
        }

        // TEMPORAL: Omitir validación de que el cliente del pedido coincida con el usuario
        // para permitir que administradores y repartidores califiquen cualquier pedido.
        /*
        String clienteNombre = sessionManager.getUsuarioDetails().getNombre();
        if (!pedido.getCliente().equalsIgnoreCase(clienteNombre)) {
            mostrarError("No tienes permiso para calificar este pedido.");
            return;
        }
        */

        // Validar calificación previa
        calificacionController.obtenerCalificacionPorPedido(pedidoId, new CalificacionController.CalificacionCallback() {
            @Override
            public void onSuccess(Calificacion calificacion) {
                mostrarMensaje("Este pedido ya ha sido calificado.");
                // Mostrar la calificación anterior
                ratingBar.setRating(calificacion.getValor());
                if (calificacion.getComentario() != null) {
                    etComentario.setText(calificacion.getComentario());
                }
                // Deshabilitar controles
                ratingBar.setIsIndicator(true);
                etComentario.setEnabled(false);
                btnEnviarCalificacion.setEnabled(false);

                // Mostrar datos del pedido de todas formas
                mostrarDatosPedido();
            }

            @Override
            public void onError(String message) {
                // No hay calificación previa, continuar
                mostrarDatosPedido();
            }
        });
    }

    private void mostrarDatosPedido() {
        // Mostrar datos del pedido
        tvPedidoNumero.setText(getString(R.string.pedido_numero, pedido.getNumero()));
        tvCliente.setText(getString(R.string.cliente, pedido.getCliente()));
        tvDireccion.setText(getString(R.string.direccion, pedido.getDireccion()));
        tvFecha.setText(getString(R.string.fecha, dateFormat.format(pedido.getFecha())));

        // Configurar botón de enviar
        btnEnviarCalificacion.setOnClickListener(v -> enviarCalificacion());
    }

    private void enviarCalificacion() {
        float valor = ratingBar.getRating();
        if (valor < 1) {
            mostrarMensaje("Por favor, selecciona una puntuación.");
            // Aplicar un pequeño efecto de sacudida al RatingBar
            ratingBar.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                    this, R.anim.shake));
            return;
        }

        String comentario = etComentario.getText().toString().trim();
        if (comentario.isEmpty()) {
            comentario = null; // Permitir comentario nulo
        }

        // Deshabilitar botón para prevenir múltiples envíos
        btnEnviarCalificacion.setEnabled(false);
        btnEnviarCalificacion.setText("Enviando...");

        Calificacion calificacion = new Calificacion();
        calificacion.setPedidoId(pedidoId);
        calificacion.setUsuarioId(sessionManager.getUsuarioDetails().getId());
        calificacion.setValor((int) valor);
        calificacion.setComentario(comentario);
        calificacion.setFecha(new Date());

        calificacionController.insertarCalificacion(calificacion, new CalificacionController.OperacionCallback() {
            @Override
            public void onSuccess() {
                // Mostrar animación de éxito
                Snackbar.make(rootView, getString(R.string.gracias_por_calificar),
                        Snackbar.LENGTH_LONG).show();

                // Dar tiempo para que el usuario vea el mensaje
                rootView.postDelayed(() -> {
                    setResult(RESULT_OK);
                    finish();
                }, 1500);
            }

            @Override
            public void onError(String message) {
                // Volver a habilitar el botón en caso de error
                btnEnviarCalificacion.setEnabled(true);
                btnEnviarCalificacion.setText(R.string.enviar_calificacion);
                mostrarError(message);
            }
        });
    }

    private void mostrarMensaje(String mensaje) {
        Snackbar.make(rootView, mensaje, Snackbar.LENGTH_SHORT).show();
    }

    private void mostrarError(String mensaje) {
        Log.e(TAG, mensaje);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        if (!"Este pedido ya ha sido calificado.".equals(mensaje)) {
            // Si es un error crítico, salir después de un breve retraso
            rootView.postDelayed(this::finish, 2000);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}