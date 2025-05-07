package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.SessionManager;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private CardView cardPedidos, cardMapa, cardReportes, cardIncidencias;
    private TextView tvBienvenida;
    private SessionManager sessionManager;
    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Verificar si hay sesión activa
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Obtener datos del usuario actual
        usuarioActual = sessionManager.getUsuarioDetails();

        // Inicializar vistas
        tvBienvenida = findViewById(R.id.tvBienvenida);
        cardPedidos = findViewById(R.id.cardPedidos);
        cardMapa = findViewById(R.id.cardMapa);
        cardReportes = findViewById(R.id.cardReportes);
        cardIncidencias = findViewById(R.id.cardIncidencias);

        // Configurar mensaje de bienvenida
        tvBienvenida.setText("Bienvenido, " + usuarioActual.getNombre());

        // Configurar listeners
        cardPedidos.setOnClickListener(this);
        cardMapa.setOnClickListener(this);
        cardReportes.setOnClickListener(this);
        cardIncidencias.setOnClickListener(this);

        // Ocultar el botón de nueva orden por el momento
        ExtendedFloatingActionButton fabNewOrder = findViewById(R.id.fabNewOrder);
        fabNewOrder.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int id = v.getId();

        if (id == R.id.cardPedidos) {
            intent = new Intent(MainActivity.this, ListaPedidosActivity.class);
        } else if (id == R.id.cardMapa) {
            intent = new Intent(MainActivity.this, MapaActivity.class);
        } else if (id == R.id.cardReportes) {
            intent = new Intent(MainActivity.this, ReporteActivity.class);
        } else if (id == R.id.cardIncidencias) {
            intent = new Intent(MainActivity.this, IncidenciaActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem profileItem = menu.findItem(R.id.menu_perfil);
        MenuItem logoutItem = menu.findItem(R.id.menu_logout);

        int iconColor = ContextCompat.getColor(this, R.color.white);
        if (profileItem.getIcon() != null) {
            profileItem.getIcon().setTint(iconColor);
        }
        if (logoutItem.getIcon() != null) {
            logoutItem.getIcon().setTint(iconColor);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_perfil) {
            startActivity(new Intent(MainActivity.this, PerfilActivity.class));
            return true;
        } else if (id == R.id.menu_logout) {
            // Mostrar diálogo de confirmación antes de cerrar sesión
            mostrarDialogoConfirmacionCerrarSesion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void mostrarDialogoConfirmacionCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Está seguro que desea cerrar sesión?");

        // Botón Confirmar
        builder.setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
            // Cerrar sesión y redirigir al login
            sessionManager.logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        // Botón Cancelar
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}