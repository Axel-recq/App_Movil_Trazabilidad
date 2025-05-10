package com.trazabilidad.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.PedidoAdapter;
import com.trazabilidad.app.controllers.PedidoController;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ListaPedidosActivity extends AppCompatActivity {

    private ListView listViewPedidos;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;

    private PedidoController pedidoController;
    private SessionManager sessionManager;
    private List<Pedido> pedidosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_pedidos);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mis Pedidos");
        }

        inicializarUI();
        configurarEventos();

        sessionManager = new SessionManager(this);
        pedidoController = new PedidoController(this);
        pedidosList = new ArrayList<>();

        cargarPedidos();
    }

    private void inicializarUI() {
        listViewPedidos = findViewById(R.id.listViewPedidos);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void configurarEventos() {
        swipeRefreshLayout.setOnRefreshListener(this::cargarPedidos);

        listViewPedidos.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Pedido pedidoSeleccionado = pedidosList.get(position);
                int pedidoId = pedidoSeleccionado.getId();

                // Añadir log para verificar el ID del pedido
                Log.d("ListaPedidos", "ID del pedido seleccionado: " + pedidoId);

                // Verificar que el ID sea válido antes de pasar a la siguiente actividad
                if (pedidoId <= 0) {
                    Toast.makeText(this, "ID de pedido no válido: " + pedidoId, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(ListaPedidosActivity.this, DetallePedidoActivity.class);
                intent.putExtra("PEDIDO_ID", pedidoId);
                startActivity(intent);

                // Añadir log para confirmar que se lanzó el intent
                Log.d("ListaPedidos", "Intent lanzado para abrir DetallePedidoActivity con ID: " + pedidoId);
            } catch (Exception e) {
                Log.e("ListaPedidos", "Error al abrir detalle: " + e.getMessage(), e);
                Toast.makeText(this, "Error al abrir el detalle del pedido: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPedidos() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        int usuarioId = sessionManager.getUsuarioDetails().getId();

        pedidoController.obtenerPedidosAsignados(usuarioId, new PedidoController.PedidosCallback() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                progressBar.setVisibility(View.GONE);
                pedidosList = pedidos;

                if (pedidos.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No tienes pedidos asignados");
                } else {
                    PedidoAdapter adapter = new PedidoAdapter(ListaPedidosActivity.this, pedidosList);
                    listViewPedidos.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListaPedidosActivity.this, message, Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error al cargar pedidos");
            }
        });
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