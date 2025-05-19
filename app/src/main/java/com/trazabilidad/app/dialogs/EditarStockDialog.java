package com.trazabilidad.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.ProductoController;
import com.trazabilidad.app.models.Producto;

public class EditarStockDialog extends Dialog {

    private final Producto producto;
    private final ProductoController productoController;
    private OnStockUpdateListener listener;

    private EditText editTextCantidad;
    private RadioGroup radioGroupOperacion;
    private RadioButton radioButtonAumentar;
    private RadioButton radioButtonReducir;
    private TextView textViewStockActual;
    private Button buttonActualizar;
    private Button buttonCancelar;

    public interface OnStockUpdateListener {
        void onStockUpdated();
    }

    public EditarStockDialog(Context context, Producto producto) {
        super(context);
        this.producto = producto;
        this.productoController = new ProductoController(context);
    }

    public void setOnStockUpdateListener(OnStockUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_editar_stock);

        // Inicializar vistas
        editTextCantidad = findViewById(R.id.editTextCantidad);
        radioGroupOperacion = findViewById(R.id.radioGroupOperacion);
        radioButtonAumentar = findViewById(R.id.radioButtonAumentar);
        radioButtonReducir = findViewById(R.id.radioButtonReducir);
        textViewStockActual = findViewById(R.id.textViewStockActual);
        buttonActualizar = findViewById(R.id.buttonActualizar);
        buttonCancelar = findViewById(R.id.buttonCancelar);

        // Establecer stock actual
        textViewStockActual.setText("Stock actual: " + producto.getCantidad());

        // Por defecto, seleccionar "Aumentar"
        radioButtonAumentar.setChecked(true);

        // Configurar botones
        buttonCancelar.setOnClickListener(v -> dismiss());
        buttonActualizar.setOnClickListener(v -> actualizarStock());
    }

    private void actualizarStock() {
        // Validar entrada
        String cantidadStr = editTextCantidad.getText().toString().trim();
        if (cantidadStr.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese una cantidad", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                Toast.makeText(getContext(), "La cantidad debe ser mayor que cero", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determinar operación
            boolean esAumento = radioButtonAumentar.isChecked();

            if (esAumento) {
                // Aumentar stock
                productoController.aumentarStock(producto.getId(), cantidad, new ProductoController.OperacionStockCallback() {
                    @Override
                    public void onSuccess(int nuevoStock, boolean bajoStock) {
                        if (listener != null) {
                            listener.onStockUpdated();
                        }
                        dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Reducir stock
                productoController.descontarStock(producto.getId(), cantidad, new ProductoController.OperacionStockCallback() {
                    @Override
                    public void onSuccess(int nuevoStock, boolean bajoStock) {
                        if (listener != null) {
                            listener.onStockUpdated();
                        }

                        if (bajoStock) {
                            Toast.makeText(getContext(), "¡Atención! El stock está bajo el límite recomendado",
                                    Toast.LENGTH_LONG).show();
                        }

                        dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Cantidad inválida", Toast.LENGTH_SHORT).show();
        }
    }
}