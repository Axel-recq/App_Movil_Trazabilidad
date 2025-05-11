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
import com.trazabilidad.app.controllers.PedidoController;
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
    private PedidoController pedidoController;
    private SessionManager sessionManager;
    private List<Pedido> pedidosList;
    private PedidoAdapter pedidoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_pedidos);

        inicializarUI();
        configurarToolbar();
        configurarRecyclerView();
        configurarEventos();

        sessionManager = new SessionManager(this);
        pedidoController = new PedidoController(this);
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

        // Añadimos una animación de entrada a los items
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

        // Configuración del FAB para abrir NuevoPedidoActivity
        fabAction.setOnClickListener(v -> {
            Intent intent = new Intent(ListaPedidosActivity.this, NuevoPedidoActivity.class);
            startActivity(intent);
        });
    }

    private void cargarPedidos() {
        mostrarCargando(true);

        int usuarioId = sessionManager.getUsuarioDetails().getId();
        Log.d(TAG, "Cargando pedidos para usuario ID: " + usuarioId);

        pedidoController.obtenerPedidosAsignados(usuarioId, new PedidoController.PedidosCallback() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                mostrarCargando(false);

                pedidosList = pedidos;

                if (pedidos.isEmpty()) {
                    mostrarEstadoVacio(true);
                } else {
                    mostrarEstadoVacio(false);
                    actualizarListaPedidos(pedidos);
                }
            }

            @Override
            public void onError(String message) {
                mostrarCargando(false);
                mostrarEstadoVacio(true);

                Log.e(TAG, "Error al cargar pedidos: " + message);
                Toast.makeText(ListaPedidosActivity.this, message, Toast.LENGTH_SHORT).show();
                tvEmpty.setText(R.string.error_pedido_no_encontrado);
            }
        });
    }

    private void actualizarListaPedidos(List<Pedido> pedidos) {
        pedidoAdapter = new PedidoAdapter(this, pedidos, pedidoId -> {
            // Interfaz para manejar el click en el item
            if (pedidoId <= 0) {
                Toast.makeText(this, getString(R.string.error_pedido_numero_invalido), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Intent intent = new Intent(ListaPedidosActivity.this, DetallePedidoActivity.class);
                intent.putExtra("PEDIDO_ID", pedidoId);
                startActivity(intent);
                Log.d(TAG, "Abriendo DetallePedidoActivity con ID: " + pedidoId);
            } catch (Exception e) {
                Log.e(TAG, "Error al abrir detalle: " + e.getMessage(), e);
                Toast.makeText(this, "Error al abrir el detalle del pedido: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewPedidos.setAdapter(pedidoAdapter);

        // Ejecutar la animación del RecyclerView
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
        // Recargar la lista cuando la actividad vuelve al frente
        cargarPedidos();
    }
}