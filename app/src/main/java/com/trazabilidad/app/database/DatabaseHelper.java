package com.trazabilidad.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "trazabilidad.db";
    private static final int DATABASE_VERSION = 3; // Incrementado para nuevas mejoras


    private static DatabaseHelper sInstance;

    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_USUARIO_ID = "id";
    public static final String COLUMN_USUARIO_NOMBRE = "nombre";
    public static final String COLUMN_USUARIO_EMAIL = "email";
    public static final String COLUMN_USUARIO_PASSWORD = "password";
    public static final String COLUMN_USUARIO_ROL = "rol";
    public static final String COLUMN_USUARIO_TELEFONO = "telefono";
    public static final String COLUMN_USUARIO_ACTIVO = "activo";

    public static final String TABLE_PEDIDOS = "pedidos";
    public static final String COLUMN_PEDIDO_ID = "id";
    public static final String COLUMN_PEDIDO_NUMERO = "numero";
    public static final String COLUMN_PEDIDO_CLIENTE = "cliente";
    public static final String COLUMN_PEDIDO_DIRECCION = "direccion";
    public static final String COLUMN_PEDIDO_FECHA = "fecha";
    public static final String COLUMN_PEDIDO_ESTADO = "estado";
    public static final String COLUMN_PEDIDO_USUARIO_ID = "usuario_id";
    public static final String COLUMN_PEDIDO_LATITUD = "latitud";
    public static final String COLUMN_PEDIDO_LONGITUD = "longitud";
    public static final String COLUMN_PEDIDO_OBSERVACIONES = "observaciones";

    public static final String TABLE_PRODUCTOS = "productos";
    public static final String COLUMN_PRODUCTO_ID = "id";
    public static final String COLUMN_PRODUCTO_CODIGO = "codigo";
    public static final String COLUMN_PRODUCTO_NOMBRE = "nombre";
    public static final String COLUMN_PRODUCTO_DESCRIPCION = "descripcion";
    public static final String COLUMN_PRODUCTO_PRECIO = "precio";
    public static final String COLUMN_PRODUCTO_CANTIDAD = "cantidad";
    public static final String COLUMN_PRODUCTO_PEDIDO_ID = "pedido_id";

    public static final String TABLE_INCIDENCIAS = "incidencias";
    public static final String COLUMN_INCIDENCIA_ID = "id";
    public static final String COLUMN_INCIDENCIA_TIPO = "tipo";
    public static final String COLUMN_INCIDENCIA_DESCRIPCION = "descripcion";
    public static final String COLUMN_INCIDENCIA_FECHA = "fecha";
    public static final String COLUMN_INCIDENCIA_FOTO_URI = "foto_uri";
    public static final String COLUMN_INCIDENCIA_USUARIO_ID = "usuario_id";
    public static final String COLUMN_INCIDENCIA_PEDIDO_ID = "pedido_id";

    public static final String TABLE_UBICACIONES = "ubicaciones";
    public static final String COLUMN_UBICACION_ID = "id";
    public static final String COLUMN_UBICACION_LATITUD = "latitud";
    public static final String COLUMN_UBICACION_LONGITUD = "longitud";
    public static final String COLUMN_UBICACION_FECHA = "fecha";
    public static final String COLUMN_UBICACION_USUARIO_ID = "usuario_id";
    public static final String COLUMN_UBICACION_PEDIDO_ID = "pedido_id";

    public static final String TABLE_TIPOS_INCIDENCIAS = "tipos_incidencias";
    public static final String COLUMN_TIPO_ID = "id";
    public static final String COLUMN_TIPO_NOMBRE = "nombre";

    // SQL creation statements
    private static final String SQL_CREATE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USUARIO_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_USUARIO_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_USUARIO_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_USUARIO_ROL + " TEXT NOT NULL, " +
                    COLUMN_USUARIO_TELEFONO + " TEXT, " +
                    COLUMN_USUARIO_ACTIVO + " INTEGER DEFAULT 1" +
                    ")";

    private static final String SQL_CREATE_PEDIDOS =
            "CREATE TABLE " + TABLE_PEDIDOS + " (" +
                    COLUMN_PEDIDO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PEDIDO_NUMERO + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PEDIDO_CLIENTE + " TEXT NOT NULL, " +
                    COLUMN_PEDIDO_DIRECCION + " TEXT NOT NULL, " +
                    COLUMN_PEDIDO_FECHA + " INTEGER NOT NULL, " +
                    COLUMN_PEDIDO_ESTADO + " TEXT NOT NULL, " +
                    COLUMN_PEDIDO_USUARIO_ID + " INTEGER, " +
                    COLUMN_PEDIDO_LATITUD + " REAL, " +
                    COLUMN_PEDIDO_LONGITUD + " REAL, " +
                    COLUMN_PEDIDO_OBSERVACIONES + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_PEDIDO_USUARIO_ID + ") REFERENCES " +
                    TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + ")" +
                    ")";

    private static final String SQL_CREATE_PRODUCTOS =
            "CREATE TABLE " + TABLE_PRODUCTOS + " (" +
                    COLUMN_PRODUCTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCTO_CODIGO + " TEXT NOT NULL, " +
                    COLUMN_PRODUCTO_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_PRODUCTO_DESCRIPCION + " TEXT, " +
                    COLUMN_PRODUCTO_PRECIO + " REAL NOT NULL, " +
                    COLUMN_PRODUCTO_CANTIDAD + " INTEGER NOT NULL, " +
                    COLUMN_PRODUCTO_PEDIDO_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_PRODUCTO_PEDIDO_ID + ") REFERENCES " +
                    TABLE_PEDIDOS + "(" + COLUMN_PEDIDO_ID + ") ON DELETE CASCADE" +
                    ")";

    private static final String SQL_CREATE_INCIDENCIAS =
            "CREATE TABLE " + TABLE_INCIDENCIAS + " (" +
                    COLUMN_INCIDENCIA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_INCIDENCIA_TIPO + " TEXT NOT NULL, " +
                    COLUMN_INCIDENCIA_DESCRIPCION + " TEXT NOT NULL, " +
                    COLUMN_INCIDENCIA_FECHA + " INTEGER NOT NULL, " +
                    COLUMN_INCIDENCIA_FOTO_URI + " TEXT, " +
                    COLUMN_INCIDENCIA_USUARIO_ID + " INTEGER NOT NULL, " +
                    COLUMN_INCIDENCIA_PEDIDO_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_INCIDENCIA_USUARIO_ID + ") REFERENCES " +
                    TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_INCIDENCIA_PEDIDO_ID + ") REFERENCES " +
                    TABLE_PEDIDOS + "(" + COLUMN_PEDIDO_ID + ") ON DELETE CASCADE" +
                    ")";

    private static final String SQL_CREATE_UBICACIONES =
            "CREATE TABLE " + TABLE_UBICACIONES + " (" +
                    COLUMN_UBICACION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_UBICACION_LATITUD + " REAL NOT NULL, " +
                    COLUMN_UBICACION_LONGITUD + " REAL NOT NULL, " +
                    COLUMN_UBICACION_FECHA + " INTEGER NOT NULL, " +
                    COLUMN_UBICACION_USUARIO_ID + " INTEGER NOT NULL, " +
                    COLUMN_UBICACION_PEDIDO_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_UBICACION_USUARIO_ID + ") REFERENCES " +
                    TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_UBICACION_PEDIDO_ID + ") REFERENCES " +
                    TABLE_PEDIDOS + "(" + COLUMN_PEDIDO_ID + ") ON DELETE CASCADE" +
                    ")";

    private static final String SQL_CREATE_TIPOS_INCIDENCIAS =
            "CREATE TABLE " + TABLE_TIPOS_INCIDENCIAS + " (" +
                    COLUMN_TIPO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TIPO_NOMBRE + " TEXT NOT NULL UNIQUE" +
                    ")";


    private static final String SQL_CREATE_INDEX_PEDIDOS_USUARIO =
            "CREATE INDEX idx_pedidos_usuario ON " + TABLE_PEDIDOS + "(" + COLUMN_PEDIDO_USUARIO_ID + ")";

    private static final String SQL_CREATE_INDEX_PRODUCTOS_PEDIDO =
            "CREATE INDEX idx_productos_pedido ON " + TABLE_PRODUCTOS + "(" + COLUMN_PRODUCTO_PEDIDO_ID + ")";

    private static final String SQL_CREATE_INDEX_INCIDENCIAS_PEDIDO =
            "CREATE INDEX idx_incidencias_pedido ON " + TABLE_INCIDENCIAS + "(" + COLUMN_INCIDENCIA_PEDIDO_ID + ")";

    private static final String SQL_CREATE_INDEX_INCIDENCIAS_USUARIO =
            "CREATE INDEX idx_incidencias_usuario ON " + TABLE_INCIDENCIAS + "(" + COLUMN_INCIDENCIA_USUARIO_ID + ")";


    private static final String SQL_DELETE_USUARIOS = "DROP TABLE IF EXISTS " + TABLE_USUARIOS;
    private static final String SQL_DELETE_PEDIDOS = "DROP TABLE IF EXISTS " + TABLE_PEDIDOS;
    private static final String SQL_DELETE_PRODUCTOS = "DROP TABLE IF EXISTS " + TABLE_PRODUCTOS;
    private static final String SQL_DELETE_INCIDENCIAS = "DROP TABLE IF EXISTS " + TABLE_INCIDENCIAS;
    private static final String SQL_DELETE_UBICACIONES = "DROP TABLE IF EXISTS " + TABLE_UBICACIONES;
    private static final String SQL_DELETE_TIPOS_INCIDENCIAS = "DROP TABLE IF EXISTS " + TABLE_TIPOS_INCIDENCIAS;


    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {

            db.execSQL(SQL_CREATE_USUARIOS);
            db.execSQL(SQL_CREATE_PEDIDOS);
            db.execSQL(SQL_CREATE_PRODUCTOS);
            db.execSQL(SQL_CREATE_INCIDENCIAS);
            db.execSQL(SQL_CREATE_UBICACIONES);
            db.execSQL(SQL_CREATE_TIPOS_INCIDENCIAS);


            db.execSQL(SQL_CREATE_INDEX_PEDIDOS_USUARIO);
            db.execSQL(SQL_CREATE_INDEX_PRODUCTOS_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_INCIDENCIAS_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_INCIDENCIAS_USUARIO);


            insertarDatosPrueba(db);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error creating database", e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            if (oldVersion < 2) {

                db.execSQL(SQL_CREATE_TIPOS_INCIDENCIAS);

                insertarTiposIncidenciasPrueba(db);
            }

            if (oldVersion < 3) {

                db.execSQL(SQL_CREATE_INDEX_PEDIDOS_USUARIO);
                db.execSQL(SQL_CREATE_INDEX_PRODUCTOS_PEDIDO);
                db.execSQL(SQL_CREATE_INDEX_INCIDENCIAS_PEDIDO);
                db.execSQL(SQL_CREATE_INDEX_INCIDENCIAS_USUARIO);

            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void insertarDatosPrueba(SQLiteDatabase db) {
        // Insert test user
        db.execSQL("INSERT INTO " + TABLE_USUARIOS +
                " (" + COLUMN_USUARIO_NOMBRE + ", " +
                COLUMN_USUARIO_EMAIL + ", " +
                COLUMN_USUARIO_PASSWORD + ", " +
                COLUMN_USUARIO_ROL + ", " +
                COLUMN_USUARIO_TELEFONO + ", " +
                COLUMN_USUARIO_ACTIVO + ") " +
                "VALUES ('Usuario', 'demo@example.com', 'demo123', 'REPARTIDOR', '555-123-4567', 1)");

        // Insert test orders
        long fechaActual = System.currentTimeMillis();

        db.execSQL("INSERT INTO " + TABLE_PEDIDOS +
                " (" + COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ") " +
                "VALUES ('PED-001', 'Cliente Uno', 'Calle Principal 123', " + fechaActual + ", 'ASIGNADO', 1, 19.4326, -99.1332, 'Entregar en horario de oficina')");

        db.execSQL("INSERT INTO " + TABLE_PEDIDOS +
                " (" + COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ") " +
                "VALUES ('PED-002', 'Cliente Dos', 'Avenida Central 456', " + fechaActual + ", 'EN_RUTA', 1, 19.4287, -99.1277, 'Llamar antes de entregar')");

        // Insert test products
        db.execSQL("INSERT INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-001', 'Producto Uno', 'Descripción del producto uno', 100.50, 2, 1)");

        db.execSQL("INSERT INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-002', 'Producto Dos', 'Descripción del producto dos', 75.25, 1, 1)");

        db.execSQL("INSERT INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-003', 'Producto Tres', 'Descripción del producto tres', 200.00, 3, 2)");

        insertarTiposIncidenciasPrueba(db);
    }

    private void insertarTiposIncidenciasPrueba(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_TIPOS_INCIDENCIAS + " (" + COLUMN_TIPO_NOMBRE + ") VALUES ('Retraso')");
        db.execSQL("INSERT INTO " + TABLE_TIPOS_INCIDENCIAS + " (" + COLUMN_TIPO_NOMBRE + ") VALUES ('Producto dañado')");
        db.execSQL("INSERT INTO " + TABLE_TIPOS_INCIDENCIAS + " (" + COLUMN_TIPO_NOMBRE + ") VALUES ('Dirección incorrecta')");
    }
}