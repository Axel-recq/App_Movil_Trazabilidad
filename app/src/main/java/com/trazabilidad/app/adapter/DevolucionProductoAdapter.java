package com.trazabilidad.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.DevolucionItem;

import java.util.List;

public class DevolucionProductoAdapter extends RecyclerView.Adapter<DevolucionProductoAdapter.ViewHolder> {
    private final List<DevolucionItem> items;
    private final Context context;

    public DevolucionProductoAdapter(Context context, List<DevolucionItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_devolucion_producto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DevolucionItem item = items.get(position);
        holder.bind(item, context);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<DevolucionItem> getItems() {
        return items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductoNombre;
        CheckBox checkBoxDevolver;
        TextInputLayout textInputLayoutCantidad;
        TextInputLayout textInputLayoutMotivo;
        TextInputEditText editTextCantidadDevolver;
        TextInputEditText editTextMotivo;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewProductoNombre = itemView.findViewById(R.id.textViewProductoNombre);
            checkBoxDevolver = itemView.findViewById(R.id.checkBoxDevolver);
            textInputLayoutCantidad = itemView.findViewById(R.id.textInputLayoutCantidad);
            textInputLayoutMotivo = itemView.findViewById(R.id.textInputLayoutMotivo);
            editTextCantidadDevolver = itemView.findViewById(R.id.editTextCantidadDevolver);
            editTextMotivo = itemView.findViewById(R.id.editTextMotivo);
        }

        @SuppressLint("DefaultLocale")
        public void bind(DevolucionItem item, Context context) {
            // Mostrar nombre del producto y cantidad comprada
            textViewProductoNombre.setText(String.format(
                    context.getString(R.string.producto_cantidad),
                    item.getProducto().getNombre(),
                    item.getProducto().getCantidad())
            );

            // Configurar estado inicial del checkbox y campos
            checkBoxDevolver.setChecked(item.isSelected());
            updateFieldsState(item.isSelected());

            // Configurar valores iniciales si existen
            if (item.getCantidad() > 0) {
                editTextCantidadDevolver.setText(String.valueOf(item.getCantidad()));
            } else {
                editTextCantidadDevolver.setText("");
            }
            editTextMotivo.setText(item.getMotivo());

            // Listener para el checkbox
            checkBoxDevolver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                updateFieldsState(isChecked);

                if (!isChecked) {
                    editTextCantidadDevolver.setText("");
                    editTextMotivo.setText("");
                    item.setCantidad(0);
                    item.setMotivo("");
                }
            });

            // Listener para el campo de cantidad
            editTextCantidadDevolver.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        // Si el campo está vacío, establecer cantidad a 0
                        if (s.length() == 0) {
                            item.setCantidad(0);
                            textInputLayoutCantidad.setError(null);
                            return;
                        }

                        int cantidad = Integer.parseInt(s.toString());

                        // Validar que la cantidad no exceda la disponible
                        if (cantidad > item.getProducto().getCantidad()) {
                            textInputLayoutCantidad.setError(
                                    String.format(context.getString(R.string.error_cantidad_excede),
                                            item.getProducto().getCantidad())
                            );
                            item.setCantidad(0);
                        }
                        // Validar que la cantidad no sea negativa
                        else if (cantidad < 0) {
                            textInputLayoutCantidad.setError(context.getString(R.string.error_cantidad_negativa));
                            item.setCantidad(0);
                        }
                        // Cantidad válida
                        else {
                            item.setCantidad(cantidad);
                            textInputLayoutCantidad.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        item.setCantidad(0);
                        textInputLayoutCantidad.setError(context.getString(R.string.error_cantidad_requerida));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Listener para el campo de motivo
            editTextMotivo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setMotivo(s.toString());
                    // Validar que el motivo no esté vacío cuando se ha seleccionado el ítem
                    if (item.isSelected() && s.toString().trim().isEmpty()) {
                        textInputLayoutMotivo.setError(context.getString(R.string.error_motivo_requerido));
                    } else {
                        textInputLayoutMotivo.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Método para actualizar el estado visual de los campos
        private void updateFieldsState(boolean enabled) {
            editTextCantidadDevolver.setEnabled(enabled);
            editTextMotivo.setEnabled(enabled);

            // Cambiar apariencia visual para indicar estado habilitado/deshabilitado
            float alpha = enabled ? 1.0f : 0.5f;
            textInputLayoutCantidad.setAlpha(alpha);
            textInputLayoutMotivo.setAlpha(alpha);
        }
    }
}