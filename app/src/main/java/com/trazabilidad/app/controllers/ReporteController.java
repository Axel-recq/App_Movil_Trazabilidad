package com.trazabilidad.app.controllers;

import android.content.Context;

import com.trazabilidad.app.database.IncidenciaDAO;
import com.trazabilidad.app.database.PedidoDAO;
import com.trazabilidad.app.database.ReporteDAO;
import com.trazabilidad.app.models.Incidencia;
import com.trazabilidad.app.models.Pedido;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteController {

    private final PedidoDAO pedidoDAO;
    private final IncidenciaDAO incidenciaDAO;
    private final ReporteDAO reporteDAO;

    public ReporteController(Context context) {
        pedidoDAO = new PedidoDAO(context);
        incidenciaDAO = new IncidenciaDAO(context);
        reporteDAO = new ReporteDAO(context);
    }

    public void generarReporte(String tipoReporte, int usuarioId, ReporteCallback callback) {
        try {
            switch (tipoReporte) {
                case "Pedidos por Estado":
                    Map<String, Integer> resultadosEstado = generarReportePedidosPorEstado(usuarioId);
                    callback.onSuccess(resultadosEstado);
                    break;
                case "Incidencias por Tipo":
                    Map<String, Integer> resultadosIncidencias = generarReporteIncidenciasPorTipo(usuarioId);
                    callback.onSuccess(resultadosIncidencias);
                    break;
                case "Resumen de Entregas":
                    Map<String, Integer> resultadosResumen = generarReporteResumenEntregas(usuarioId);
                    callback.onSuccess(resultadosResumen);
                    break;
                case "Tiempo Promedio de Entrega":
                    Map<String, Double> resultadosTiempo = generarReporteTiempoPromedioEntrega();
                    callback.onSuccessDouble(resultadosTiempo);
                    break;
                case "Ratio de Entregas por Hora":
                    Map<String, Double> resultadosRatio = generarReporteRatioEntregasPorHora();
                    callback.onSuccessDouble(resultadosRatio);
                    break;
                case "Porcentaje de Entregas Exitosas":
                    Map<String, Double> resultadosPorcentaje = generarReportePorcentajeEntregasExitosas();
                    callback.onSuccessDouble(resultadosPorcentaje);
                    break;
                default:
                    callback.onError("Tipo de reporte no soportado");
            }
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

    private Map<String, Double> generarReporteTiempoPromedioEntrega() {
        Map<String, Double> resultados = new HashMap<>();
        double tiempoPromedio = reporteDAO.obtenerTiempoPromedioEntrega();
        resultados.put("Tiempo Promedio de Entrega (minutos)", tiempoPromedio);
        return resultados;
    }

    private Map<String, Double> generarReporteRatioEntregasPorHora() {
        Map<String, Double> resultados = new HashMap<>();
        double ratio = reporteDAO.obtenerRatioEntregasPorHora();
        resultados.put("Ratio de Entregas por Hora", ratio);
        return resultados;
    }

    private Map<String, Double> generarReportePorcentajeEntregasExitosas() {
        Map<String, Double> resultados = new HashMap<>();
        double porcentaje = reporteDAO.obtenerPorcentajeEntregasExitosas();
        resultados.put("Porcentaje de Entregas Exitosas (%)", porcentaje);
        return resultados;
    }

    public interface ReporteCallback {
        void onSuccess(Map<String, Integer> resultados);
        void onSuccessDouble(Map<String, Double> resultados);
        void onError(String message);
    }
}