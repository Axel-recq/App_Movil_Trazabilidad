package com.trazabilidad.app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.trazabilidad.app.fragments.IncidenciasFragment;
import com.trazabilidad.app.fragments.OverviewFragment;
import com.trazabilidad.app.fragments.PedidosFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3;
    public static final int OVERVIEW_PAGE = 0;
    public static final int PEDIDOS_PAGE = 1;
    public static final int INCIDENCIAS_PAGE = 2;

    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case OVERVIEW_PAGE:
                return new OverviewFragment();
            case PEDIDOS_PAGE:
                return new PedidosFragment();
            case INCIDENCIAS_PAGE:
                return new IncidenciasFragment();
            default:
                return new OverviewFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}