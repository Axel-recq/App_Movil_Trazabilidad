package com.trazabilidad.app.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.trazabilidad.app.R;
import com.trazabilidad.app.controllers.PedidoController;
import com.trazabilidad.app.database.TipoIncidenciaDAO;
import com.trazabilidad.app.models.Incidencia;
import com.trazabilidad.app.models.Pedido;
import com.trazabilidad.app.models.Producto;
import com.trazabilidad.app.utils.DateUtils;
import com.trazabilidad.app.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class IncidenciaActivity extends AppCompatActivity {

    private static final String TAG = "IncidenciaActivity";
    private EditText etDescripcion;
    private Spinner spinnerTipoIncidencia;
    private Button btnTomarFoto, btnGaleria, btnRegistrar;
    private ImageView ivFoto;
    private ProgressBar progressBar;

    private PedidoController pedidoController;
    private SessionManager sessionManager;
    private TipoIncidenciaDAO tipoIncidenciaDAO;
    private int pedidoId = -1;
    private Uri fotoUri;
    private String rutaFoto;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidencia);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registrar Incidencia");
        }

        pedidoId = getIntent().getIntExtra("PEDIDO_ID", -1);

        inicializarUI();
        inicializarControladores();
        inicializarActivityResultLaunchers();
        configurarListeners();
        cargarTiposIncidencias();

        // Verificar si el dispositivo tiene cámara
        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            btnTomarFoto.setEnabled(false);
            btnTomarFoto.setVisibility(View.GONE);
            Log.d(TAG, "No se detectó hardware de cámara en el dispositivo");
        }

        if (pedidoId != -1) {
            verificarPedido(pedidoId);
        }
    }

    private void inicializarUI() {
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerTipoIncidencia = findViewById(R.id.spinnerTipoIncidencia);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGaleria = findViewById(R.id.btnGaleria);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        ivFoto = findViewById(R.id.ivFoto);
        progressBar = findViewById(R.id.progressBar);
    }

    private void inicializarControladores() {
        pedidoController = new PedidoController(this);
        sessionManager = new SessionManager(this);
        tipoIncidenciaDAO = new TipoIncidenciaDAO(this);
    }

    private void inicializarActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (fotoUri != null) {
                            Log.d(TAG, "Foto tomada exitosamente con EXTRA_OUTPUT: " + fotoUri.toString());
                            ivFoto.setImageURI(fotoUri);
                            ivFoto.setVisibility(View.VISIBLE);
                        } else if (result.getData() != null && result.getData().getData() != null) {
                            fotoUri = result.getData().getData();
                            Log.d(TAG, "Foto tomada sin EXTRA_OUTPUT: " + fotoUri.toString());
                            ivFoto.setImageURI(fotoUri);
                            ivFoto.setVisibility(View.VISIBLE);
                        } else if (result.getData() != null && result.getData().getExtras() != null) {
                            Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                            if (bitmap != null) {
                                Log.d(TAG, "Foto devuelta como Bitmap");
                                ivFoto.setImageBitmap(bitmap);
                                ivFoto.setVisibility(View.VISIBLE);
                                fotoUri = saveBitmapToFile(bitmap);
                                if (fotoUri != null) {
                                    Log.d(TAG, "Bitmap guardado como archivo: " + fotoUri.toString());
                                } else {
                                    Log.w(TAG, "No se pudo guardar el Bitmap como archivo");
                                }
                            } else {
                                Log.w(TAG, "No se recibió Bitmap en extras");
                                Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w(TAG, "No se recibió URI ni Bitmap de la foto");
                            Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "No se pudo tomar la foto, resultado: " + result.getResultCode());
                        Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        fotoUri = uri;
                        Log.d(TAG, "Imagen seleccionada con Photo Picker: " + fotoUri.toString());
                        ivFoto.setImageURI(fotoUri);
                        ivFoto.setVisibility(View.VISIBLE);
                        // Persistir acceso al URI
                        getContentResolver().takePersistableUriPermission(
                                fotoUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } else {
                        Log.w(TAG, "No se seleccionó ninguna imagen con Photo Picker");
                        Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void configurarListeners() {
        btnTomarFoto.setOnClickListener(v -> checkCameraPermission());
        btnGaleria.setOnClickListener(v -> seleccionarConPhotoPicker());
        btnRegistrar.setOnClickListener(v -> registrarIncidencia());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Solicitando permiso de cámara");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            Log.d(TAG, "Permiso de cámara concedido, intentando tomar foto");
            tomarFoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso de cámara concedido en onRequestPermissionsResult");
                tomarFoto();
            } else {
                Log.w(TAG, "Permiso de cámara denegado");
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void tomarFoto() {
        PackageManager packageManager = getPackageManager();

        Intent explicitCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        explicitCameraIntent.setComponent(new ComponentName("com.android.camera2", "com.android.camera.CameraActivity"));
        List<ResolveInfo> explicitActivities = packageManager.queryIntentActivities(explicitCameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Log.d(TAG, "Actividades disponibles para intent explícito (com.android.camera2): " + explicitActivities.size());
        for (ResolveInfo info : explicitActivities) {
            Log.d(TAG, "Actividad encontrada: " + info.activityInfo.packageName + "/" + info.activityInfo.name);
        }

        if (!explicitActivities.isEmpty()) {
            Log.d(TAG, "Lanzando intent explícito para com.android.camera2");
            fotoUri = null;
            cameraLauncher.launch(explicitCameraIntent);
        } else {
            // Intent simplificado para capturar Bitmap
            Intent simpleCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            List<ResolveInfo> activities = packageManager.queryIntentActivities(simpleCameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
            Log.d(TAG, "Actividades disponibles para ACTION_IMAGE_CAPTURE (simple): " + activities.size());
            for (ResolveInfo info : activities) {
                Log.d(TAG, "Actividad encontrada: " + info.activityInfo.packageName + "/" + info.activityInfo.name);
            }

            if (simpleCameraIntent.resolveActivity(packageManager) != null) {
                Log.d(TAG, "Lanzando intent simplificado sin EXTRA_OUTPUT");
                fotoUri = null;
                cameraLauncher.launch(simpleCameraIntent);
            } else {
                Log.w(TAG, "No se encontró actividad para intent simplificado");
                // Intent con FileProvider
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activities = packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                Log.d(TAG, "Actividades disponibles para ACTION_IMAGE_CAPTURE (con FileProvider): " + activities.size());
                for (ResolveInfo info : activities) {
                    Log.d(TAG, "Actividad encontrada: " + info.activityInfo.packageName + "/" + info.activityInfo.name);
                }
                if (!activities.isEmpty()) {
                    try {
                        File photoFile = createImageFile();
                        fotoUri = FileProvider.getUriForFile(
                                this,
                                "com.trazabilidad.app.fileprovider",
                                photoFile
                        );
                        Log.d(TAG, "URI generado para la foto: " + fotoUri.toString());
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        cameraLauncher.launch(takePictureIntent);
                    } catch (IOException ex) {
                        Log.e(TAG, "Error al crear el archivo de imagen", ex);
                        Toast.makeText(this, "Error al crear el archivo de imagen: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (SecurityException ex) {
                        Log.e(TAG, "Error de seguridad con FileProvider", ex);
                        Toast.makeText(this, "Error de seguridad al configurar la cámara", Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Log.e(TAG, "Error al configurar la cámara", ex);
                        Toast.makeText(this, "Error al configurar la cámara: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w(TAG, "No se encontraron aplicaciones de cámara instaladas");
                    // Probar con ACTION_IMAGE_CAPTURE_SECURE
                    Intent secureCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                    activities = packageManager.queryIntentActivities(secureCameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    Log.d(TAG, "Actividades disponibles para ACTION_IMAGE_CAPTURE_SECURE: " + activities.size());
                    for (ResolveInfo info : activities) {
                        Log.d(TAG, "Actividad encontrada para SECURE: " + info.activityInfo.packageName + "/" + info.activityInfo.name);
                    }
                    if (!activities.isEmpty()) {
                        Log.d(TAG, "Lanzando intent con ACTION_IMAGE_CAPTURE_SECURE");
                        fotoUri = null;
                        cameraLauncher.launch(secureCameraIntent);
                    } else {
                        Log.w(TAG, "No se encontraron aplicaciones para ACTION_IMAGE_CAPTURE_SECURE");
                        Toast.makeText(this, "No se encontró una aplicación de cámara instalada", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private void seleccionarConPhotoPicker() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
        Log.d(TAG, "Lanzando Photo Picker para seleccionar imagen");
    }

    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + DateUtils.getTimeStamp() + "_";
        File storageDir = getExternalFilesDir("Incidencias");
        if (storageDir != null && !storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "No se pudo crear el directorio de almacenamiento");
                throw new IOException("No se pudo crear el directorio de almacenamiento");
            }
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        rutaFoto = image.getAbsolutePath();
        Log.d(TAG, "Archivo de imagen creado en: " + rutaFoto);
        return image;
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        try {
            File photoFile = createImageFile();
            try (FileOutputStream out = new FileOutputStream(photoFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
            }
            return FileProvider.getUriForFile(
                    this,
                    "com.trazabilidad.app.fileprovider",
                    photoFile
            );
        } catch (IOException ex) {
            Log.e(TAG, "Error al guardar Bitmap como archivo", ex);
            return null;
        }
    }

    private void registrarIncidencia() {
        String descripcion = etDescripcion.getText().toString().trim();
        String tipo = spinnerTipoIncidencia.getSelectedItem() != null ? spinnerTipoIncidencia.getSelectedItem().toString() : "";

        if (descripcion.isEmpty()) {
            mostrarToast("Por favor ingrese una descripción");
            return;
        }

        if (tipo.isEmpty()) {
            mostrarToast("Por favor seleccione un tipo de incidencia");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegistrar.setEnabled(false);

        Incidencia incidencia = new Incidencia();
        incidencia.setDescripcion(descripcion);
        incidencia.setTipo(tipo);
        incidencia.setFecha(new Date());
        incidencia.setUsuarioId(sessionManager.getUsuarioDetails().getId());

        if (pedidoId != -1) incidencia.setPedidoId(pedidoId);
        if (fotoUri != null) incidencia.setFotoUri(fotoUri.toString());

        pedidoController.registrarIncidencia(incidencia, new PedidoController.OperacionCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                mostrarToast("Incidencia registrada correctamente");
                finish();
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                btnRegistrar.setEnabled(true);
                mostrarToast(message);
            }
        });
    }

    private void cargarTiposIncidencias() {
        List<String> tipos = tipoIncidenciaDAO.obtenerTiposIncidencias();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoIncidencia.setAdapter(adapter);
    }

    private void verificarPedido(int pedidoId) {
        pedidoController.obtenerDetallePedido(pedidoId, new PedidoController.PedidoDetalleCallback() {
            @Override
            public void onSuccess(Pedido pedido, List<Producto> productos) {
                Log.d(TAG, "Pedido verificado correctamente: " + pedidoId);
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Error al verificar pedido: " + message);
                mostrarToast("Pedido no encontrado: " + message);
                finish();
            }
        });
    }

    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
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