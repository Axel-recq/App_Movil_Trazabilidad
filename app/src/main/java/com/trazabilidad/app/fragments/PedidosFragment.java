package com.trazabilidad.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.trazabilidad.app.R;

public class PedidosFragment extends Fragment {

    public PedidosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Reemplaza con tu layout correspondiente
        return inflater.inflate(R.layout.fragment_pedidos, container, false);
    }
}