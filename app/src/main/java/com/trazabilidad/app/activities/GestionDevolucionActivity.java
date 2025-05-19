package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.DevolucionController;
import com.trazabilidad.app.models.Devolucion;
import com.trazabilidad.app.utils.SessionManager;

import java.util.Date;

public class GestionDevolucionActivity extends AppCompatActivity {
    private EditText editTextPedidoId, editTextProductoId, editTextCantidad, editTextMotivo;
    private Button buttonRegistrar;
    private DevolucionController devolucionController;
    private SessionManager sessionManager;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_devolucion);

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Sesión expirada. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Obtener usuarioId desde la sesión
        usuarioId = sessionManager.getUsuarioDetails().getId();
        if (usuarioId == 0) {
            Toast.makeText(this, "Error: No se pudo obtener el ID del usuario.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inicializar vistas
        editTextPedidoId = findViewById(R.id.editTextPedidoId);
        editTextProductoId = findViewById(R.id.editTextProductoId);
        editTextCantidad = findViewById(R.id.editTextCantidad);
        editTextMotivo = findViewById(R.id.editTextMotivo);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);

        // Inicializar controlador
        devolucionController = new DevolucionController(this);

        // Rellenar campos desde Intent (si aplica)
        Intent intent = getIntent();
        if (intent.hasExtra("pedidoId")) {
            editTextPedidoId.setText(String.valueOf(intent.getIntExtra("pedidoId", 0)));
        }
        if (intent.hasExtra("productoId")) {
            editTextProductoId.setText(String.valueOf(intent.getIntExtra("productoId", 0)));
        }

        // Configurar el botón
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarDevolucion();
            }
        });
    }

    private void registrarDevolucion() {
        try {
            // Obtener datos del formulario
            int pedidoId = Integer.parseInt(editTextPedidoId.getText().toString().trim());
            int productoId = Integer.parseInt(editTextProductoId.getText().toString().trim());
            int cantidad = Integer.parseInt(editTextCantidad.getText().toString().trim());
            String motivo = editTextMotivo.getText().toString().trim();

            // Crear objeto Devolucion
            Devolucion devolucion = new Devolucion();
            devolucion.setPedidoId(pedidoId);
            devolucion.setProductoId(productoId);
            devolucion.setCantidad(cantidad);
            devolucion.setMotivo(motivo);
            devolucion.setFecha(new Date());
            devolucion.setUsuarioId(usuarioId);

            // Registrar devolución
            devolucionController.registrarDevolucion(devolucion, new DevolucionController.OperacionCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(GestionDevolucionActivity.this, "Devolución registrada correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(GestionDevolucionActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
        }
    }
}