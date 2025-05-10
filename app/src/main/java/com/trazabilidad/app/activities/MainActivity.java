package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.trazabilidad.app.R;
import com.trazabilidad.app.database.IncidenciaDAO;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.SessionManager;
import com.trazabilidad.app.database.DatabaseHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private Usuario usuarioActual;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private DatabaseHelper dbHelper;
    private PedidoDAO pedidoDao;
    private IncidenciaDAO incidenciaDao;

    // UI Components
    private MaterialCardView cardNewOrder, cardReportIncident;

    public MainActivity() {
        // Constructor vacío: no inicializar dbHelper ni DAOs aquí
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar DatabaseHelper y DAOs aquí
        dbHelper = DatabaseHelper.getInstance(this);
        pedidoDao = new PedidoDAO(dbHelper);
        incidenciaDao = new IncidenciaDAO(dbHelper);

        // Verificación de sesión
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        usuarioActual = sessionManager.getUsuarioDetails();

        // Inicialización de UI
        initializeUI();
        setupNavigationDrawer();
        updateNavigationHeader();
        loadInitialData();
    }

    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        cardNewOrder = findViewById(R.id.cardNewOrder);
        cardReportIncident = findViewById(R.id.cardReportIncident);

        // Listeners
        cardNewOrder.setOnClickListener(this);
        cardReportIncident.setOnClickListener(this);
        findViewById(R.id.btnViewAllOrders).setOnClickListener(this);
        findViewById(R.id.btnViewAllIncidents).setOnClickListener(this);
        findViewById(R.id.fabNewOrder).setOnClickListener(v -> goToNewOrder());
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvNombre = headerView.findViewById(R.id.tvNombre);
        TextView tvEmail = headerView.findViewById(R.id.tvEmail);
        if (tvNombre != null) {
            tvNombre.setText(usuarioActual.getNombre());
        }
        if (tvEmail != null) {
            tvEmail.setText(usuarioActual.getEmail());
        }
    }

    private void loadInitialData() {
        loadRecentOrders();
        loadIncidentsData();
    }

    private void loadRecentOrders() {
        List<Pedido> recentOrders = pedidoDao.getRecentPedidos(3);
        RecyclerView rvRecentOrders = findViewById(R.id.rvRecentOrders);
        rvRecentOrders.setAdapter(new com.trazabilidad.app.adapter.RecentOrdersAdapter(this, recentOrders));
    }

    private void loadIncidentsData() {
        TextView tvPending = findViewById(R.id.tvPendingIncidents);
        TextView tvResolved = findViewById(R.id.tvResolvedIncidents);
        tvPending.setText(String.valueOf(incidenciaDao.getPendingIncidenciasCount()));
        tvResolved.setText(String.valueOf(incidenciaDao.getResolvedIncidenciasCount()));
    }

    private void goToNewOrder() {
        startActivity(new Intent(this, NuevoPedidoActivity.class));
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cardNewOrder || id == R.id.fabNewOrder) {
            goToNewOrder();
        } else if (id == R.id.cardReportIncident) {
            startActivity(new Intent(this, NuevaIncidenciaActivity.class));
        } else if (id == R.id.btnViewAllOrders) {
            startActivity(new Intent(this, ListaPedidosActivity.class));
        } else if (id == R.id.btnViewAllIncidents) {
            startActivity(new Intent(this, IncidenciaActivity.class));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Class<?> activityClass = null;

        if (itemId == R.id.nav_pedidos) {
            activityClass = ListaPedidosActivity.class;
        } else if (itemId == R.id.nav_mapa) {
            activityClass = MapaActivity.class;
        } else if (itemId == R.id.nav_reportes) {
            activityClass = ReporteActivity.class;
        } else if (itemId == R.id.nav_incidencias) {
            activityClass = IncidenciaActivity.class;
        } else if (itemId == R.id.nav_perfil) {
            activityClass = PerfilActivity.class;
        } else if (itemId == R.id.nav_logout) {
            mostrarDialogoConfirmacionCerrarSesion();
        }

        if (activityClass != null) {
            startActivity(new Intent(this, activityClass));
        }
        drawerLayout.closeDrawer(GravityCompat.START);
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.logout_confirm, (dialog, which) -> {
                    SessionManager sessionManager = new SessionManager(this);
                    sessionManager.logout();
                    navigateToLogin();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}