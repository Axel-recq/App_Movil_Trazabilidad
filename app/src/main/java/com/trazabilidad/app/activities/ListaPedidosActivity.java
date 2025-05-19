package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.PedidoAdapter;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ListaPedidosActivity extends AppCompatActivity {

    private static final String TAG = "ListaPedidosActivity";

    // UI Components
    private RecyclerView recyclerViewPedidos;
    private CircularProgressIndicator progressBar;
    private ConstraintLayout emptyStateLayout;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabAction;

    // Data controllers
    private PedidoDAO pedidoDAO;
    private SessionManager sessionManager;
    private List<Pedido> pedidosList;
    private PedidoAdapter pedidoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_pedidos);

        // Inicializar SessionManager primero
        sessionManager = new SessionManager(this);

        // Verificar sesión
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Sesión expirada. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        inicializarUI();
        configurarToolbar();
        configurarRecyclerView();
        configurarEventos();

        pedidoDAO = new PedidoDAO(this);
        pedidosList = new ArrayList<>();

        cargarPedidos();
    }

    private void inicializarUI() {
        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        toolbar = findViewById(R.id.toolbar);
        fabAction = findViewById(R.id.fabAction);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.nav_pedidos);
        }
    }

    private void configurarRecyclerView() {
        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPedidos.setHasFixedSize(true);
        recyclerViewPedidos.setLayoutAnimation(
                android.view.animation.AnimationUtils.loadLayoutAnimation(
                        this, R.anim.layout_animation_fall_down));
    }

    private void configurarEventos() {
        swipeRefreshLayout.setOnRefreshListener(this::cargarPedidos);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimaryVariant,
                R.color.colorSecondary);

        // Configurar FAB solo para administradores
        if (sessionManager.isRepartidorOrAdmin()) {
            fabAction.setVisibility(View.VISIBLE);
            fabAction.setOnClickListener(v -> {
                Intent intent = new Intent(ListaPedidosActivity.this, NuevoPedidoActivity.class);
                startActivity(intent);
            });
        } else {
            fabAction.setVisibility(View.GONE);
        }
    }

    private void cargarPedidos() {
        mostrarCargando(true);

        if (sessionManager.isCliente()) {
            String clienteNombre = sessionManager.getUsuarioDetails().getNombre();
            Log.d(TAG, "Cargando pedidos para cliente: " + clienteNombre);
            pedidosList = pedidoDAO.obtenerPedidosPorCliente(clienteNombre);
            procesarPedidos();
        } else {
            int usuarioId = sessionManager.getUsuarioDetails().getId();
            Log.d(TAG, "Cargando pedidos para usuario ID: " + usuarioId);
            pedidosList = pedidoDAO.obtenerPedidosPorUsuario(usuarioId);
            procesarPedidos();
        }
    }

    private void procesarPedidos() {
        mostrarCargando(false);

        if (pedidosList.isEmpty()) {
            mostrarEstadoVacio(true);
            tvEmpty.setText(sessionManager.isCliente() ? R.string.no_pedidos_cliente : R.string.no_pedidos_repartidor);
        } else {
            mostrarEstadoVacio(false);
            actualizarListaPedidos(pedidosList);
        }
    }

    private void actualizarListaPedidos(List<Pedido> pedidos) {
        pedidoAdapter = new PedidoAdapter(this, pedidos, new PedidoAdapter.OnItemClickListener() {
            @Override
            public void onVerDetalles(int pedidoId) {
                Intent intent = new Intent(ListaPedidosActivity.this, DetallePedidoActivity.class);
                intent.putExtra("PEDIDO_ID", pedidoId);
                startActivity(intent);
                Log.d(TAG, "Abriendo DetallePedidoActivity con ID: " + pedidoId);
            }

            @Override
            public void onCalificar(int pedidoId) {
                Intent intent = new Intent(ListaPedidosActivity.this, CalificarPedidoActivity.class);
                intent.putExtra("pedidoId", pedidoId);
                startActivity(intent);
                Log.d(TAG, "Abriendo CalificarPedidoActivity con ID: " + pedidoId);
            }

            @Override
            public void onMarcarEntregado(int pedidoId) {
                if (pedidoDAO.confirmarEntrega(pedidoId)) {
                    Toast.makeText(ListaPedidosActivity.this, "Pedido marcado como entregado.", Toast.LENGTH_SHORT).show();
                    cargarPedidos();
                } else {
                    Toast.makeText(ListaPedidosActivity.this, "Error al marcar como entregado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onVerMapa(int pedidoId) {
                Intent intent = new Intent(ListaPedidosActivity.this, MapaActivity.class);
                intent.putExtra("PEDIDO_ID", pedidoId);
                startActivity(intent);
                Log.d(TAG, "Abriendo MapaActivity con ID: " + pedidoId);
            }

            @Override
            public void onDevolver(int pedidoId) {
                Intent intent = new Intent(ListaPedidosActivity.this, GestionDevolucionActivity.class);
                intent.putExtra("pedidoId", pedidoId);
                startActivity(intent);
                Log.d(TAG, "Abriendo GestionDevolucionActivity con ID: " + pedidoId);
            }
        }, sessionManager);

        recyclerViewPedidos.setAdapter(pedidoAdapter);
        recyclerViewPedidos.scheduleLayoutAnimation();
    }

    private void mostrarCargando(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void mostrarEstadoVacio(boolean mostrar) {
        emptyStateLayout.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        recyclerViewPedidos.setVisibility(mostrar ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPedidos();
    }
}