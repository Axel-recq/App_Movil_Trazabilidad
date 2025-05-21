package com.trazabilidad.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.trazabilidad.app.R;
import com.trazabilidad.app.adapter.MainViewPagerAdapter;
import com.trazabilidad.app.adapter.NotificationAdapter;
import com.trazabilidad.app.database.DatabaseHelper;
import com.trazabilidad.app.models.Notificacion;
import com.trazabilidad.app.models.Usuario;
import com.trazabilidad.app.utils.NotificationManager;
import com.trazabilidad.app.utils.SessionManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private TextView tvBienvenida;
    private TextView tvFecha;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private SessionManager sessionManager;
    private Usuario usuarioActual;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DatabaseHelper dbHelper;
    private ExtendedFloatingActionButton fabNuevo;
    private ImageView btnNotifications;
    private CircleImageView imgProfile;
    private NotificationManager notificationManager;
    private BadgeDrawable badgeDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa DB Helper
        dbHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase trazDb = dbHelper.getWritableDatabase();

        // Verificación de sesión
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }
        usuarioActual = sessionManager.getUsuarioDetails();

        // Inicializa NotificationManager
        notificationManager = new NotificationManager(this);
        notificationManager.limpiarNotificacionesAntiguas(); // Limpiar notificaciones antiguas

        // Inicializa los componentes de UI
        initializeUI();

        // Configura la fecha actual
        setupDate();

        // Configura el NavigationDrawer
        setupNavigationDrawer();

        // Actualiza el header del Navigation Drawer con los datos del usuario
        updateNavigationHeader();

        // Configura el ViewPager y TabLayout
        setupViewPager();

        // Configura el botón flotante
        setupFAB();

        // Configura el botón de notificaciones
        setupNotifications();

        // Configura el perfil
        setupProfile();

        // Actualiza el badge de notificaciones
        actualizarBadgeNotificaciones();
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void initializeUI() {
        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Componentes principales
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvFecha = findViewById(R.id.tvFecha);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fabNuevo = findViewById(R.id.fabNuevo);
        btnNotifications = findViewById(R.id.btnNotifications);
        imgProfile = findViewById(R.id.imgProfile);

        // Configurar acceso a menú de administración
        MenuItem usuariosItem = navigationView.getMenu().findItem(R.id.nav_usuarios);
        usuariosItem.setVisible(sessionManager.isUserAdmin());

        // Configurar texto de bienvenida
        tvBienvenida.setText(getString(R.string.welcome_format, usuarioActual.getNombre()));

        // Inicializar badge para notificaciones solo para administradores
        badgeDrawable = BadgeDrawable.create(this);
        badgeDrawable.setBackgroundColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        badgeDrawable.setBadgeTextColor(getResources().getColor(R.color.colorOnPrimary, getTheme()));
        badgeDrawable.setVisible(false);
        badgeDrawable.setHorizontalOffset(10);
        badgeDrawable.setVerticalOffset(10);

        // Mostrar el botón de notificaciones y el badge solo para administradores
        if (sessionManager.isUserAdmin()) {
            com.google.android.material.badge.BadgeUtils.attachBadgeDrawable(badgeDrawable, btnNotifications, findViewById(R.id.notificationButtonContainer));
        } else {
            btnNotifications.setVisibility(View.GONE); // Ocultar el botón para no administradores
        }
    }

    private void setupDate() {
        Date currentDate = Calendar.getInstance().getTime();
        String dayOfWeek = (String) DateFormat.format("EEEE", currentDate);
        String dayOfMonth = (String) DateFormat.format("dd", currentDate);
        String monthName = (String) DateFormat.format("MMMM", currentDate);

        dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase(Locale.getDefault()) + dayOfWeek.substring(1);
        monthName = monthName.substring(0, 1).toUpperCase(Locale.getDefault()) + monthName.substring(1);

        String formattedDate = dayOfWeek + ", " + dayOfMonth + " de " + monthName;
        tvFecha.setText(formattedDate);
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.toolbar),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void limpiarNotificacionesSiEsNecesario() {
        SharedPreferences prefs = getSharedPreferences("TrazabilidadPrefs", MODE_PRIVATE);
        long ultimaLimpieza = prefs.getLong("ultima_limpieza_notificaciones", 0);
        long ahora = System.currentTimeMillis();
        long unDiaEnMilis = 24 * 60 * 60 * 1000;

        if (ahora - ultimaLimpieza > unDiaEnMilis) {
            notificationManager.limpiarNotificacionesAntiguas();
            prefs.edit().putLong("ultima_limpieza_notificaciones", ahora).apply();
        }
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvNombre = headerView.findViewById(R.id.nav_header_nombre);
        TextView tvEmail = headerView.findViewById(R.id.nav_header_email);

        tvNombre.setText(usuarioActual.getNombre());
        tvEmail.setText(usuarioActual.getEmail());
    }

    private void setupViewPager() {
        MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case MainViewPagerAdapter.OVERVIEW_PAGE:
                    tab.setText(R.string.overview);
                    break;
            }
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateFabForPage(position);
            }
        });
    }

    private void setupFAB() {
        fabNuevo.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();
            switch (currentPosition) {
                case MainViewPagerAdapter.OVERVIEW_PAGE:
                    showNewActionBottomSheet();
                    break;
            }
        });

        updateFabForPage(0);
    }

    private void updateFabForPage(int position) {
        switch (position) {
            case MainViewPagerAdapter.OVERVIEW_PAGE:
                fabNuevo.setText(R.string.nuevo);
                fabNuevo.setIconResource(R.drawable.ic_add);
                break;
        }
        fabNuevo.extend();
    }

    private void setupNotifications() {
        if (!sessionManager.isUserAdmin()) {
            return; // No configurar notificaciones para no administradores
        }

        btnNotifications.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_notifications, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            RecyclerView rvNotifications = bottomSheetView.findViewById(R.id.rvNotifications);
            LinearLayout emptyStateContainer = bottomSheetView.findViewById(R.id.emptyStateContainer);
            ProgressBar progressBar = bottomSheetView.findViewById(R.id.progressBar);
            ChipGroup chipGroup = bottomSheetView.findViewById(R.id.chipGroup);
            MaterialButton btnMarkAllRead = bottomSheetView.findViewById(R.id.btnMarkAllRead);

            rvNotifications.setLayoutManager(new LinearLayoutManager(this));

            // Mostrar el ProgressBar mientras se cargan las notificaciones
            progressBar.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.GONE);

            // Obtener notificaciones iniciales (sin filtro)
            notificationManager.obtenerNotificaciones(new NotificationManager.NotificationCallback() {
                @Override
                public void onSuccess(List<Notificacion> notificaciones) {
                    progressBar.setVisibility(View.GONE);
                    NotificationAdapter adapter = new NotificationAdapter(MainActivity.this, notificaciones);
                    rvNotifications.setAdapter(adapter);

                    // Configurar el listener para el estado vacío
                    adapter.setOnNotificationActionListener(new NotificationAdapter.OnNotificationActionListener() {
                        @Override
                        public void onNotificationRead(int position) {
                            actualizarBadgeNotificaciones();
                        }

                        @Override
                        public void onNotificationDeleted(int position) {
                            actualizarBadgeNotificaciones();
                        }

                        @Override
                        public void onEmptyState(boolean isEmpty) {
                            emptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                            rvNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                        }
                    });

                    // Configurar los filtros de los chips
                    chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(ChipGroup group, int checkedId) {
                            Notificacion.Tipo tipoFiltro = null;
                            if (checkedId == R.id.chipStock) {
                                tipoFiltro = Notificacion.Tipo.BAJO_STOCK;
                            } else if (checkedId == R.id.chipDevolucion) {
                                tipoFiltro = Notificacion.Tipo.DEVOLUCION;
                            } else if (checkedId == R.id.chipCalificacion) {
                                tipoFiltro = Notificacion.Tipo.CALIFICACION;
                            }
                            adapter.filtrarPorTipo(tipoFiltro);
                        }
                    });

                    // Configurar el botón "Marcar todas como leídas"
                    btnMarkAllRead.setOnClickListener(v1 -> {
                        int checkedChipId = chipGroup.getCheckedChipId();
                        if (checkedChipId == R.id.chipAll || checkedChipId == -1) {
                            adapter.marcarTodasComoLeidas();
                        } else {
                            Notificacion.Tipo tipoFiltro = null;
                            if (checkedChipId == R.id.chipStock) {
                                tipoFiltro = Notificacion.Tipo.BAJO_STOCK;
                            } else if (checkedChipId == R.id.chipDevolucion) {
                                tipoFiltro = Notificacion.Tipo.DEVOLUCION;
                            } else if (checkedChipId == R.id.chipCalificacion) {
                                tipoFiltro = Notificacion.Tipo.CALIFICACION;
                            }
                            notificationManager.marcarComoLeidasPorTipo(tipoFiltro);
                            adapter.filtrarPorTipo(tipoFiltro); // Refrescar la lista con el filtro actual
                        }
                        actualizarBadgeNotificaciones();
                    });

                    // Mostrar la lista de notificaciones
                    rvNotifications.setVisibility(View.VISIBLE);
                    emptyStateContainer.setVisibility(notificaciones.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onError(String message) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error al cargar notificaciones: " + message, Toast.LENGTH_SHORT).show();
                }
            });

            bottomSheetDialog.show();
        });
    }

    private void setupProfile() {
        imgProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PerfilActivity.class));
        });
    }

    private void actualizarBadgeNotificaciones() {
        if (!sessionManager.isUserAdmin()) {
            badgeDrawable.setVisible(false); // Asegurar que el badge esté oculto para no administradores
            return;
        }

        notificationManager.obtenerNotificaciones(new NotificationManager.NotificationCallback() {
            @Override
            public void onSuccess(List<Notificacion> notificaciones) {
                int count = notificationManager.getNotificacionesNoLeidasCount();
                badgeDrawable.setNumber(count);
                badgeDrawable.setVisible(count > 0);
            }

            @Override
            public void onError(String message) {
                badgeDrawable.setVisible(false);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_home) {
            intent = new Intent(this, MainActivity.class);
        } else if (id == R.id.nav_pedidos) {
            intent = new Intent(this, ListaPedidosActivity.class);
        } else if (id == R.id.nav_mapa) {
            intent = new Intent(this, MapaActivity.class);
        } else if (id == R.id.nav_reportes) {
            intent = new Intent(this, ReporteActivity.class);
        } else if (id == R.id.nav_incidencias) {
            intent = new Intent(this, IncidenciasListActivity.class);
        } else if (id == R.id.nav_lista_productos) {
            intent = new Intent(this, ListaProductosActivity.class);
        } else if (id == R.id.nav_usuarios) {
            intent = new Intent(this, ListaUsuariosActivity.class);
        } else if (id == R.id.nav_perfil) {
            intent = new Intent(this, PerfilActivity.class);
        } else if (id == R.id.nav_logout) {
            mostrarDialogoConfirmacionCerrarSesion();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        if (intent != null) {
            startActivity(intent);
        }
        return true;
    }

    private void showNewActionBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_new_actions, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Configurar visibilidad de botones solo para administradores
        View btnNewPedido = bottomSheetView.findViewById(R.id.btnNewPedido);
        btnNewPedido.setVisibility(sessionManager.isUserAdmin() ? View.VISIBLE : View.GONE);
        btnNewPedido.setOnClickListener(v -> {
            if (sessionManager.isUserAdmin()) {
                startActivity(new Intent(MainActivity.this, NuevoPedidoActivity.class));
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            }
        });

        View btnNewIncidencia = bottomSheetView.findViewById(R.id.btnNewIncidencia);
        btnNewIncidencia.setVisibility(sessionManager.isUserAdmin() ? View.VISIBLE : View.GONE);
        btnNewIncidencia.setOnClickListener(v -> {
            if (sessionManager.isUserAdmin()) {
                startActivity(new Intent(MainActivity.this, IncidenciaActivity.class));
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            }
        });

        View btnNewProducto = bottomSheetView.findViewById(R.id.btnNewProducto);
        btnNewProducto.setVisibility(sessionManager.isUserAdmin() ? View.VISIBLE : View.GONE);
        btnNewProducto.setOnClickListener(v -> {
            if (sessionManager.isUserAdmin()) {
                startActivity(new Intent(MainActivity.this, GestionProductoActivity.class));
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            }
        });

        View btnNewUsuario = bottomSheetView.findViewById(R.id.btnNewUsuario);
        btnNewUsuario.setVisibility(sessionManager.isUserAdmin() ? View.VISIBLE : View.GONE);
        btnNewUsuario.setOnClickListener(v -> {
            if (sessionManager.isUserAdmin()) {
                startActivity(new Intent(MainActivity.this, CrearEditarUsuarioActivity.class));
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void mostrarDialogoConfirmacionCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.logout);
        builder.setMessage(R.string.logout_confirmation);
        builder.setPositiveButton(R.string.yes_logout, (dialog, which) -> {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarBadgeNotificaciones();
    }
}