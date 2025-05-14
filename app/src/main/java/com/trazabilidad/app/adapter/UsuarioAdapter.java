package com.trazabilidad.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.trazabilidad.app.R;
import com.trazabilidad.app.models.Usuario;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private final List<Usuario> listaUsuarios;
    private final OnUsuarioClickListener listener;

    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario);
        void onEditarClick(Usuario usuario);
        void onEliminarClick(Usuario usuario);
    }

    public UsuarioAdapter(List<Usuario> listaUsuarios, OnUsuarioClickListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        holder.bind(usuario, listener);
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvNombre;
        private final TextView tvEmail;
        private final TextView tvRol;
        private final Chip chipEstado;
        private final ImageButton btnEditar;
        private final ImageButton btnEliminar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardUsuario);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRol = itemView.findViewById(R.id.tvRol);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        public void bind(final Usuario usuario, final OnUsuarioClickListener listener) {
            tvNombre.setText(usuario.getNombre());
            tvEmail.setText(usuario.getEmail());
            tvRol.setText(usuario.getRol());

            // Configurar el estado del usuario
            if (usuario.isActivo()) {
                chipEstado.setText("Activo");
                chipEstado.setChipBackgroundColorResource(R.color.colorActive);
            } else {
                chipEstado.setText("Inactivo");
                chipEstado.setChipBackgroundColorResource(R.color.colorInactive);
            }

            // Configurar listeners
            cardView.setOnClickListener(v -> listener.onUsuarioClick(usuario));
            btnEditar.setOnClickListener(v -> listener.onEditarClick(usuario));
            btnEliminar.setOnClickListener(v -> listener.onEliminarClick(usuario));
        }
    }
}