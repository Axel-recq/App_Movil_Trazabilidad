package com.trazabilidad.app.controllers;

import android.content.Context;

import com.trazabilidad.app.database.IncidenciaDAO;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.models.Incidencia;
import com.trazabilidad.app.models.Pedido;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteController {

    private PedidoDAO pedidoDAO;
    private IncidenciaDAO incidenciaDAO;

    public ReporteController(Context context) {
        pedidoDAO = new PedidoDAO(context);
        incidenciaDAO = new IncidenciaDAO(context);
    }

    public void generarReporte(String tipoReporte, int usuarioId, ReporteCallback callback) {
        try {
            Map<String, Integer> resultados = new HashMap<>();

            switch (tipoReporte) {
                case "Pedidos por Estado":
                    resultados = generarReportePedidosPorEstado(usuarioId);
                    break;
                case "Incidencias por Tipo":
                    resultados = generarReporteIncidenciasPorTipo(usuarioId);
                    break;
                case "Resumen de Entregas":
                    resultados = generarReporteResumenEntregas(usuarioId);
                    break;
                default:
                    callback.onError("Tipo de reporte no soportado");
                    return;
            }

            callback.onSuccess(resultados);
        } catch (Exception e) {
            callback.onError("Error al generar reporte: " + e.getMessage());
        }
    }

    private Map<String, Integer> generarReportePedidosPorEstado(int usuarioId) {
        Map<String, Integer> resultados = new HashMap<>();
        List<Pedido> pedidos = pedidoDAO.obtenerPedidosPorUsuario(usuarioId);

        // Inicializar contadores
        resultados.put("ASIGNADO", 0);
        resultados.put("EN_RUTA", 0);
        resultados.put("ENTREGADO", 0);
        resultados.put("INCIDENCIA", 0);

        // Contar pedidos por estado
        for (Pedido pedido : pedidos) {
            String estado = pedido.getEstado();
            if (resultados.containsKey(estado)) {
                resultados.put(estado, resultados.get(estado) + 1);
            } else {
                resultados.put(estado, 1);
            }
        }

        return resultados;
    }

    private Map<String, Integer> generarReporteIncidenciasPorTipo(int usuarioId) {
        Map<String, Integer> resultados = new HashMap<>();
        List<Incidencia> incidencias = incidenciaDAO.obtenerIncidenciasPorUsuario(usuarioId);

        // Contar incidencias por tipo
        for (Incidencia incidencia : incidencias) {
            String tipo = incidencia.getTipo();
            if (resultados.containsKey(tipo)) {
                resultados.put(tipo, resultados.get(tipo) + 1);
            } else {
                resultados.put(tipo, 1);
            }
        }

        return resultados;
    }

    private Map<String, Integer> generarReporteResumenEntregas(int usuarioId) {
        Map<String, Integer> resultados = new HashMap<>();
        List<Pedido> pedidos = pedidoDAO.obtenerPedidosPorUsuario(usuarioId);

        int totalPedidos = pedidos.size();
        int pedidosEntregados = 0;
        int pedidosPendientes = 0;
        int pedidosConIncidencia = 0;

        for (Pedido pedido : pedidos) {
            String estado = pedido.getEstado();
            if ("ENTREGADO".equals(estado)) {
                pedidosEntregados++;
            } else if ("INCIDENCIA".equals(estado)) {
                pedidosConIncidencia++;
            } else {
                pedidosPendientes++;
            }
        }

        resultados.put("Total de Pedidos", totalPedidos);
        resultados.put("Pedidos Entregados", pedidosEntregados);
        resultados.put("Pedidos Pendientes", pedidosPendientes);
        resultados.put("Pedidos con Incidencia", pedidosConIncidencia);

        // Calcular porcentaje de efectividad
        if (totalPedidos > 0) {
            int efectividad = (pedidosEntregados * 100) / totalPedidos;
            resultados.put("Efectividad (%)", efectividad);
        } else {
            resultados.put("Efectividad (%)", 0);
        }

        return resultados;
    }

    public interface ReporteCallback {
        void onSuccess(Map<String, Integer> resultados);
        void onError(String message);
    }
}
