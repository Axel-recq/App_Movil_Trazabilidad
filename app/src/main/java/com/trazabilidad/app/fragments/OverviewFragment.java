package com.trazabilidad.app.fragments;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.trazabilidad.app.R;
import com.trazabilidad.app.activities.IncidenciasListActivity;
import com.trazabilidad.app.activities.ListaPedidosActivity;
import com.trazabilidad.app.adapter.RecentOrdersAdapter;
import com.trazabilidad.app.database.DatabaseHelper;
import com.trazabilidad.app.database.IncidenciaDAO;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.models.Pedido;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OverviewFragment extends Fragment implements View.OnClickListener {

    private TextView tvPendingIncidents;
    private TextView tvResolvedIncidents;
    private TextView tvActivePedidos;
    private RecyclerView rvRecentOrders;
    private LineChart lineChart;
    private DatabaseHelper dbHelper;
    private MaterialButton btnViewAllOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        // Inicializar componentes
        tvPendingIncidents = view.findViewById(R.id.tvPendingIncidents);
        tvResolvedIncidents = view.findViewById(R.id.tvResolvedIncidents);
        tvActivePedidos = view.findViewById(R.id.tvActivePedidos);
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders);
        lineChart = view.findViewById(R.id.lineChart);
        btnViewAllOrders = view.findViewById(R.id.btnViewAllOrders);

        // Configurar listeners
        btnViewAllOrders.setOnClickListener(this);

        // Inicializar DB Helper
        dbHelper = DatabaseHelper.getInstance(getContext());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cargar datos
        loadMetricsData();
        loadRecentOrders();
        setupChart();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar datos cuando el fragmento vuelve a ser visible
        loadMetricsData();
        loadRecentOrders();
    }

    private void loadMetricsData() {
        // Cargar datos de incidencias
        IncidenciaDAO incidenciaDao = new IncidenciaDAO(dbHelper);
        int pendingCount = incidenciaDao.getPendingIncidenciasCount();
        int resolvedCount = incidenciaDao.getResolvedIncidenciasCount();

        // Cargar datos de pedidos activos
        PedidoDAO pedidoDao = new PedidoDAO(dbHelper);
        int activeOrdersCount = pedidoDao.getActivePedidosCount();

        // Mostrar datos en la UI con animación
        animateTextChange(tvPendingIncidents, pendingCount);
        animateTextChange(tvResolvedIncidents, resolvedCount);
        animateTextChange(tvActivePedidos, activeOrdersCount);
    }

    private void loadRecentOrders() {
        PedidoDAO pedidoDao = new PedidoDAO(dbHelper);
        List<Pedido> recentOrders = pedidoDao.getRecentPedidos(3);
        RecentOrdersAdapter adapter = new RecentOrdersAdapter(getContext(), recentOrders);
        rvRecentOrders.setAdapter(adapter);
    }

    private void setupChart() {
        // Obtiene datos para la gráfica de los últimos 7 días
        ArrayList<Entry> entries = new ArrayList<>();
        final ArrayList<String> xLabels = new ArrayList<>();

        // Simulación de datos (esto debería venir de tu base de datos)
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (int i = 6; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            xLabels.add(sdf.format(calendar.getTime()));

            // Aquí deberías obtener los datos reales desde tu DAO
            // Por ahora usamos datos simulados
            PedidoDAO pedidoDao = new PedidoDAO(dbHelper);
            float value = pedidoDao.getPedidosCountByDate(calendar.getTime());
            entries.add(new Entry(6-i, value));
        }

        // Configuración del LineDataSet
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.daily_orders));
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.colorPrimaryLight));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Configuración del LineData
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Configuración del eje X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        // Configuración general del gráfico
        lineChart.getAxisRight().setEnabled(false);
        Description desc = new Description();
        desc.setText("");
        lineChart.setDescription(desc);
        lineChart.getLegend().setEnabled(true);
        lineChart.animateXY(1000, 1000);
        lineChart.invalidate();
    }

    private void animateTextChange(final TextView textView, final int newValue) {
        // Obtiene el valor actual
        String currentText = textView.getText().toString();
        final int startValue;
        try {
            startValue = Integer.parseInt(currentText);
        } catch (NumberFormatException e) {
            // Si no es un número, simplemente establecer el nuevo valor
            textView.setText(String.valueOf(newValue));
            return;
        }

        // Anima el cambio de valor
        final int diff = newValue - startValue;
        final int duration = 1000; // 1 segundo
        final int steps = 20;
        final int stepDuration = duration / steps;

        for (int i = 0; i <= steps; i++) {
            final int step = i;
            textView.postDelayed(() -> {
                int value = startValue + (diff * step / steps);
                textView.setText(String.valueOf(value));
            }, i * stepDuration);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnViewAllOrders) {
            startActivity(new Intent(getContext(), ListaPedidosActivity.class));
        }
    }
}