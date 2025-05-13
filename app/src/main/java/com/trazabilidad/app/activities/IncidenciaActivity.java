package com.trazabilidad.app.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
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
    private TextInputEditText etDescripcion;
    private AutoCompleteTextView autoCompleteTipoIncidencia;
    private MaterialButton btnTomarFoto, btnGaleria, btnRegistrar;
    private ImageView ivFoto;
    private ProgressBar progressBar;
    private MaterialCardView cardViewFoto;
    private LinearLayout placeholderFoto;
    private Toolbar toolbar;

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

        pedidoId = getIntent().getIntExtra("PEDIDO_ID", -1);

        inicializarUI();
        configurarToolbar();
        inicializarControladores();
        inicializarActivityResultLaunchers();
        configurarListeners();
        cargarTiposIncidencias();

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
        toolbar = findViewById(R.id.toolbar);
        etDescripcion = findViewById(R.id.etDescripcion);
        autoCompleteTipoIncidencia = findViewById(R.id.autoCompleteTipoIncidencia);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGaleria = findViewById(R.id.btnGaleria);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        ivFoto = findViewById(R.id.ivFoto);
        progressBar = findViewById(R.id.progressBar);
        cardViewFoto = findViewById(R.id.cardViewFoto);
        placeholderFoto = findViewById(R.id.placeholderFoto);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registrar Incidencia");
        }
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
                            mostrarFoto(fotoUri);
                        } else if (result.getData() != null && result.getData().getData() != null) {
                            fotoUri = result.getData().getData();
                            Log.d(TAG, "Foto tomada sin EXTRA_OUTPUT: " + fotoUri.toString());
                            mostrarFoto(fotoUri);
                        } else if (result.getData() != null && result.getData().getExtras() != null) {
                            Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                            if (bitmap != null) {
                                Log.d(TAG, "Foto devuelta como Bitmap");
                                mostrarFoto(bitmap);
                                fotoUri = saveBitmapToFile(bitmap);
                                if (fotoUri != null) {
                                    Log.d(TAG, "Bitmap guardado como archivo: " + fotoUri.toString());
                                } else {
                                    Log.w(TAG, "No se pudo guardar el Bitmap como archivo");
                                }
                            } else {
                                Log.w(TAG, "No se recibió Bitmap en extras");
                                mostrarSnackbar("No se pudo tomar la foto");
                            }
                        } else {
                            Log.w(TAG, "No se recibió URI ni Bitmap de la foto");
                            mostrarSnackbar("No se pudo tomar la foto");
                        }
                    } else {
                        Log.w(TAG, "No se pudo tomar la foto, resultado: " + result.getResultCode());
                        mostrarSnackbar("Acción cancelada");
                    }
                }
        );

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        fotoUri = uri;
                        Log.d(TAG, "Imagen seleccionada con Photo Picker: " + fotoUri.toString());
                        mostrarFoto(fotoUri);
                        getContentResolver().takePersistableUriPermission(
                                fotoUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } else {
                        Log.w(TAG, "No se seleccionó ninguna imagen con Photo Picker");
                        mostrarSnackbar("No se seleccionó ninguna imagen");
                    }
                }
        );
    }

    private void mostrarFoto(Uri uri) {
        placeholderFoto.setVisibility(View.GONE);
        ivFoto.setImageURI(uri);
        ivFoto.setVisibility(View.VISIBLE);
    }

    private void mostrarFoto(Bitmap bitmap) {
        placeholderFoto.setVisibility(View.GONE);
        ivFoto.setImageBitmap(bitmap);
        ivFoto.setVisibility(View.VISIBLE);
    }

    private void configurarListeners() {
        btnTomarFoto.setOnClickListener(v -> checkCameraPermission());
        btnGaleria.setOnClickListener(v -> seleccionarConPhotoPicker());
        btnRegistrar.setOnClickListener(v -> validarYRegistrarIncidencia());
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
                mostrarSnackbar("Se requiere permiso de cámara para esta función");
            }
        }
    }

    private void tomarFoto() {
        PackageManager packageManager = getPackageManager();
        try {
            File photoFile = createImageFile();
            fotoUri = FileProvider.getUriForFile(
                    this,
                    "com.trazabilidad.app.fileprovider",
                    photoFile
            );
            Log.d(TAG, "URI generado para la foto: " + fotoUri.toString());

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Log.d(TAG, "Usando intent simplificado");
                Intent simpleCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(simpleCameraIntent);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error al iniciar la cámara", ex);
            Intent simpleCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fotoUri = null;
            cameraLauncher.launch(simpleCameraIntent);
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

    private void validarYRegistrarIncidencia() {
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String tipo = autoCompleteTipoIncidencia.getText() != null ? autoCompleteTipoIncidencia.getText().toString().trim() : "";

        if (descripcion.isEmpty()) {
            etDescripcion.setError("Este campo es obligatorio");
            etDescripcion.requestFocus();
            return;
        }

        List<String> tiposValidos = tipoIncidenciaDAO.obtenerTiposIncidencias();
        if (tipo.isEmpty()) {
            autoCompleteTipoIncidencia.setError("Seleccione un tipo");
            autoCompleteTipoIncidencia.requestFocus();
            return;
        }
        if (!tiposValidos.contains(tipo)) {
            autoCompleteTipoIncidencia.setError("Tipo no válido");
            autoCompleteTipoIncidencia.requestFocus();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar registro")
                .setMessage("¿Está seguro de registrar esta incidencia?")
                .setPositiveButton("Registrar", (dialog, which) -> registrarIncidencia(descripcion, tipo))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void registrarIncidencia(String descripcion, String tipo) {
        mostrarProgreso(true);

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
                mostrarProgreso(false);
                mostrarSnackbar("Incidencia registrada correctamente");


                new android.os.Handler().postDelayed(() -> finish(), 1500);
            }

            @Override
            public void onError(String message) {
                mostrarProgreso(false);
                mostrarSnackbar("Error: " + message);
            }
        });
    }

    private void mostrarProgreso(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!mostrar);
        etDescripcion.setEnabled(!mostrar);
        autoCompleteTipoIncidencia.setEnabled(!mostrar);
        btnTomarFoto.setEnabled(!mostrar);
        btnGaleria.setEnabled(!mostrar);
    }

    private void cargarTiposIncidencias() {
        List<String> tipos = tipoIncidenciaDAO.obtenerTiposIncidencias();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, tipos);
        autoCompleteTipoIncidencia.setAdapter(adapter);
    }

    private void verificarPedido(int pedidoId) {
        pedidoController.obtenerDetallePedido(pedidoId, new PedidoController.PedidoDetalleCallback() {
            @Override
            public void onSuccess(Pedido pedido, List<Producto> productos) {
                Log.d(TAG, "Pedido verificado correctamente: " + pedidoId);
                mostrarSnackbar("Registrando incidencia para pedido: " + pedido.getNumero());
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Error al verificar pedido: " + message);
                mostrarSnackbar("Pedido no encontrado");
                new android.os.Handler().postDelayed(() -> finish(), 1500);
            }
        });
    }

    private void mostrarSnackbar(String mensaje) {
        Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG).show();
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