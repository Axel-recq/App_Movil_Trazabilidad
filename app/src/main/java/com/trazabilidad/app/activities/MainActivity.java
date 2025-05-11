package com.trazabilidad.app.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.RecentOrdersAdapter;
import com.trazabilidad.app.database.DatabaseHelper;
import com.trazabilidad.app.database.IncidenciaDAO;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.SessionManager;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private TextView tvBienvenida;
    private TextView tvPendingIncidents;
    private TextView tvResolvedIncidents;
    private RecyclerView rvRecentOrders;
    private MaterialCardView cardNewOrder, cardReportIncident;
    private SessionManager sessionManager;
    private Usuario usuarioActual;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa DB Helper
        dbHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase trazDb = dbHelper.getWritableDatabase();

        // Inicializa Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Verificación de sesión
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }
        usuarioActual = sessionManager.getUsuarioDetails();

        // Inicializa los componentes de UI
        initializeUI();

        // Configura el NavigationDrawer
        setupNavigationDrawer(toolbar);

        // Actualiza el header del Navigation Drawer con los datos del usuario
        updateNavigationHeader();

        // Carga datos para los widgets
        loadRecentOrders();
        loadIncidentsData();

        // Botón flotante
        ExtendedFloatingActionButton fabNewOrder = findViewById(R.id.fabNewOrder);
        fabNewOrder.setOnClickListener(v -> goToNewOrder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentOrders();
        loadIncidentsData();
    }

    private void initializeUI() {
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvPendingIncidents = findViewById(R.id.tvPendingIncidents);
        tvResolvedIncidents = findViewById(R.id.tvResolvedIncidents);
        rvRecentOrders = findViewById(R.id.rvRecentOrders);
        cardReportIncident = findViewById(R.id.cardReportIncident);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        tvBienvenida.setText(getString(R.string.welcome_format, usuarioActual.getNombre()));

        // Set click listeners

        cardReportIncident.setOnClickListener(this);
        findViewById(R.id.btnViewAllOrders).setOnClickListener(this);
        findViewById(R.id.btnViewAllIncidents).setOnClickListener(this);
    }

    private void setupNavigationDrawer(Toolbar toolbar) {

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvNombre = headerView.findViewById(R.id.nav_header_nombre);
        TextView tvEmail = headerView.findViewById(R.id.nav_header_email);

        tvNombre.setText(usuarioActual.getNombre());
        tvEmail.setText(usuarioActual.getEmail());
    }

    private void loadRecentOrders() {
        PedidoDAO pedidoDao = new PedidoDAO(dbHelper);
        List<Pedido> recentOrders = pedidoDao.getRecentPedidos(3);
        Log.d("MainActivity", "Pedidos recientes: " + recentOrders.size());
        RecentOrdersAdapter adapter = new RecentOrdersAdapter(this, recentOrders);
        rvRecentOrders.setAdapter(adapter);
    }

    private void loadIncidentsData() {

        IncidenciaDAO incidenciaDao = new IncidenciaDAO(dbHelper);
        int pendingCount = incidenciaDao.getPendingIncidenciasCount();
        int resolvedCount = incidenciaDao.getResolvedIncidenciasCount();

        tvPendingIncidents.setText(String.valueOf(pendingCount));
        tvResolvedIncidents.setText(String.valueOf(resolvedCount));
    }

    private void goToNewOrder() {
        startActivity(new Intent(this, NuevoPedidoActivity.class));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if ( id == R.id.fabNewOrder) {
            goToNewOrder();
        } else if (id == R.id.cardReportIncident) {
            startActivity(new Intent(this, IncidenciaActivity.class));
        } else if (id == R.id.btnViewAllOrders) {
            startActivity(new Intent(this, ListaPedidosActivity.class));
        } else if (id == R.id.btnViewAllIncidents) {
            startActivity(new Intent(this, IncidenciaActivity.class));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_home) {
            // Estamos ya en Home, solo cerramos el drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_pedidos) {
            intent = new Intent(this, ListaPedidosActivity.class);
        } else if (id == R.id.nav_mapa) {
            intent = new Intent(this, MapaActivity.class);
        } else if (id == R.id.nav_reportes) {
            intent = new Intent(this, ReporteActivity.class);
        } else if (id == R.id.nav_incidencias) {
            intent = new Intent(this, IncidenciaActivity.class);
        } else if (id == R.id.nav_perfil) {
            intent = new Intent(this, PerfilActivity.class);
        } else if (id == R.id.nav_logout) {
            mostrarDialogoConfirmacionCerrarSesion();
        }

        // Cerramos el drawer
        drawerLayout.closeDrawer(GravityCompat.START);

        // Iniciamos la actividad si hay una intención
        if (intent != null) {
            startActivity(intent);
        }

        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void mostrarDialogoConfirmacionCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Está seguro que desea cerrar sesión?");
        builder.setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}