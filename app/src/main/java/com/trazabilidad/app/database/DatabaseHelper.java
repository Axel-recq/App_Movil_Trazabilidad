package com.trazabilidad.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "trazabilidad.db";
    private static final int DATABASE_VERSION = 9;
    private static DatabaseHelper sInstance;

    // Table and column names
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
    public static final String COLUMN_PEDIDO_HORA_SALIDA = "hora_salida";
    public static final String COLUMN_PEDIDO_HORA_ESTIMADA = "hora_estimada";
    public static final String COLUMN_PEDIDO_HORA_ENTREGA = "hora_entrega";
    public static final String COLUMN_PEDIDO_ALERTA_DEMORA = "alerta_demora";
    public static final String COLUMN_PEDIDO_MOTIVO_DEMORA = "motivo_demora";
    public static final String COLUMN_PEDIDO_CONFIRMADO = "confirmado";
    public static final String COLUMN_PEDIDO_TOTAL = "total";

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
    public static final String COLUMN_INCIDENCIA_ESTADO = "estado";

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

    public static final String TABLE_DEVOLUCIONES = "devoluciones";
    public static final String COLUMN_DEVOLUCION_ID = "id";
    public static final String COLUMN_DEVOLUCION_PEDIDO_ID = "pedido_id";
    public static final String COLUMN_DEVOLUCION_PRODUCTO_ID = "producto_id";
    public static final String COLUMN_DEVOLUCION_CANTIDAD = "cantidad";
    public static final String COLUMN_DEVOLUCION_MOTIVO = "motivo";
    public static final String COLUMN_DEVOLUCION_FECHA = "fecha";
    public static final String COLUMN_DEVOLUCION_USUARIO_ID = "usuario_id";

    public static final String TABLE_CALIFICACIONES = "calificaciones";
    public static final String COLUMN_CALIFICACION_ID = "id";
    public static final String COLUMN_CALIFICACION_PEDIDO_ID = "pedido_id";
    public static final String COLUMN_CALIFICACION_USUARIO_ID = "usuario_id";
    public static final String COLUMN_CALIFICACION_VALOR = "valor";
    public static final String COLUMN_CALIFICACION_COMENTARIO = "comentario";
    public static final String COLUMN_CALIFICACION_FECHA = "fecha";

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
                    COLUMN_PEDIDO_HORA_SALIDA + " INTEGER, " +
                    COLUMN_PEDIDO_HORA_ESTIMADA + " INTEGER, " +
                    COLUMN_PEDIDO_HORA_ENTREGA + " INTEGER, " +
                    COLUMN_PEDIDO_ALERTA_DEMORA + " INTEGER DEFAULT 0, " +
                    COLUMN_PEDIDO_MOTIVO_DEMORA + " TEXT, " +
                    COLUMN_PEDIDO_CONFIRMADO + " INTEGER DEFAULT 0, " +
                    COLUMN_PEDIDO_TOTAL + " REAL DEFAULT 0, " +
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
                    COLUMN_INCIDENCIA_ESTADO + " TEXT NOT NULL DEFAULT '" + IncidenciaDAO.ESTADO_PENDIENTE + "', " +
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

    private static final String SQL_CREATE_DEVOLUCIONES =
            "CREATE TABLE " + TABLE_DEVOLUCIONES + " (" +
                    COLUMN_DEVOLUCION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DEVOLUCION_PEDIDO_ID + " INTEGER NOT NULL, " +
                    COLUMN_DEVOLUCION_PRODUCTO_ID + " INTEGER NOT NULL, " +
                    COLUMN_DEVOLUCION_CANTIDAD + " INTEGER NOT NULL, " +
                    COLUMN_DEVOLUCION_MOTIVO + " TEXT NOT NULL, " +
                    COLUMN_DEVOLUCION_FECHA + " INTEGER NOT NULL, " +
                    COLUMN_DEVOLUCION_USUARIO_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_DEVOLUCION_PEDIDO_ID + ") REFERENCES " +
                    TABLE_PEDIDOS + "(" + COLUMN_PEDIDO_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COLUMN_DEVOLUCION_PRODUCTO_ID + ") REFERENCES " +
                    TABLE_PRODUCTOS + "(" + COLUMN_PRODUCTO_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COLUMN_DEVOLUCION_USUARIO_ID + ") REFERENCES " +
                    TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + ")" +
                    ")";

    private static final String SQL_CREATE_CALIFICACIONES =
            "CREATE TABLE " + TABLE_CALIFICACIONES + " (" +
                    COLUMN_CALIFICACION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CALIFICACION_PEDIDO_ID + " INTEGER NOT NULL, " +
                    COLUMN_CALIFICACION_USUARIO_ID + " INTEGER NOT NULL, " +
                    COLUMN_CALIFICACION_VALOR + " INTEGER NOT NULL CHECK(" + COLUMN_CALIFICACION_VALOR + " BETWEEN 1 AND 5), " +
                    COLUMN_CALIFICACION_COMENTARIO + " TEXT, " +
                    COLUMN_CALIFICACION_FECHA + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_CALIFICACION_PEDIDO_ID + ") REFERENCES " +
                    TABLE_PEDIDOS + "(" + COLUMN_PEDIDO_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COLUMN_CALIFICACION_USUARIO_ID + ") REFERENCES " +
                    TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + ")" +
                    ")";

    // Indices
    private static final String SQL_CREATE_INDEX_PEDIDOS_USUARIO =
            "CREATE INDEX idx_pedidos_usuario ON " + TABLE_PEDIDOS + "(" + COLUMN_PEDIDO_USUARIO_ID + ")";
    private static final String SQL_CREATE_INDEX_PRODUCTOS_PEDIDO =
            "CREATE INDEX idx_productos_pedido ON " + TABLE_PRODUCTOS + "(" + COLUMN_PRODUCTO_PEDIDO_ID + ")";
    private static final String SQL_CREATE_INDEX_INCIDENCIAS_PEDIDO =
            "CREATE INDEX idx_incidencias_pedido ON " + TABLE_INCIDENCIAS + "(" + COLUMN_INCIDENCIA_PEDIDO_ID + ")";
    private static final String SQL_CREATE_INDEX_INCIDENCIAS_USUARIO =
            "CREATE INDEX idx_incidencias_usuario ON " + TABLE_INCIDENCIAS + "(" + COLUMN_INCIDENCIA_USUARIO_ID + ")";
    private static final String SQL_CREATE_INDEX_UBICACIONES_PEDIDO =
            "CREATE INDEX idx_ubicaciones_pedido ON " + TABLE_UBICACIONES + "(" + COLUMN_UBICACION_PEDIDO_ID + ")";
    private static final String SQL_CREATE_INDEX_UBICACIONES_USUARIO =
            "CREATE INDEX idx_ubicaciones_usuario ON " + TABLE_UBICACIONES + "(" + COLUMN_UBICACION_USUARIO_ID + ")";
    private static final String SQL_CREATE_INDEX_DEVOLUCIONES_PEDIDO =
            "CREATE INDEX idx_devoluciones_pedido ON " + TABLE_DEVOLUCIONES + "(" + COLUMN_DEVOLUCION_PEDIDO_ID + ")";
    private static final String SQL_CREATE_INDEX_CALIFICACIONES_PEDIDO =
            "CREATE INDEX idx_calificaciones_pedido ON " + TABLE_CALIFICACIONES + "(" + COLUMN_CALIFICACION_PEDIDO_ID + ")";

    // SQL drop statements
    private static final String SQL_DELETE_DEVOLUCIONES = "DROP TABLE IF EXISTS " + TABLE_DEVOLUCIONES;
    private static final String SQL_DELETE_CALIFICACIONES = "DROP TABLE IF EXISTS " + TABLE_CALIFICACIONES;
    private static final String SQL_DELETE_UBICACIONES = "DROP TABLE IF EXISTS " + TABLE_UBICACIONES;
    private static final String SQL_DELETE_INCIDENCIAS = "DROP TABLE IF EXISTS " + TABLE_INCIDENCIAS;
    private static final String SQL_DELETE_PRODUCTOS = "DROP TABLE IF EXISTS " + TABLE_PRODUCTOS;
    private static final String SQL_DELETE_PEDIDOS = "DROP TABLE IF EXISTS " + TABLE_PEDIDOS;
    private static final String SQL_DELETE_TIPOS_INCIDENCIAS = "DROP TABLE IF EXISTS " + TABLE_TIPOS_INCIDENCIAS;
    private static final String SQL_DELETE_USUARIOS = "DROP TABLE IF EXISTS " + TABLE_USUARIOS;

    // Vista para reportes de eficiencia
    private static final String SQL_CREATE_VIEW_REPORTES =
            "CREATE VIEW IF NOT EXISTS view_reportes AS " +
                    "SELECT " +
                    "p." + COLUMN_PEDIDO_ID + ", " +
                    "p." + COLUMN_PEDIDO_NUMERO + ", " +
                    "p." + COLUMN_PEDIDO_ESTADO + ", " +
                    "p." + COLUMN_PEDIDO_USUARIO_ID + ", " +
                    "u." + COLUMN_USUARIO_NOMBRE + " AS nombre_repartidor, " +
                    "p." + COLUMN_PEDIDO_HORA_SALIDA + ", " +
                    "p." + COLUMN_PEDIDO_HORA_ENTREGA + ", " +
                    "(p." + COLUMN_PEDIDO_HORA_ENTREGA + " - p." + COLUMN_PEDIDO_HORA_SALIDA + ") / 60000 AS tiempo_entrega_minutos, " +
                    "(CASE WHEN p." + COLUMN_PEDIDO_HORA_ENTREGA + " <= p." + COLUMN_PEDIDO_HORA_ESTIMADA + " THEN 1 ELSE 0 END) AS entrega_a_tiempo, " +
                    "p." + COLUMN_PEDIDO_CONFIRMADO + " AS entrega_exitosa, " +
                    "(SELECT COUNT(*) FROM " + TABLE_DEVOLUCIONES + " d WHERE d." + COLUMN_DEVOLUCION_PEDIDO_ID + " = p." + COLUMN_PEDIDO_ID + ") AS tiene_devoluciones, " +
                    "(SELECT " + COLUMN_CALIFICACION_VALOR + " FROM " + TABLE_CALIFICACIONES + " c WHERE c." + COLUMN_CALIFICACION_PEDIDO_ID + " = p." + COLUMN_PEDIDO_ID + ") AS calificacion " +
                    "FROM " + TABLE_PEDIDOS + " p " +
                    "LEFT JOIN " + TABLE_USUARIOS + " u ON p." + COLUMN_PEDIDO_USUARIO_ID + " = u." + COLUMN_USUARIO_ID + " " +
                    "WHERE p." + COLUMN_PEDIDO_ESTADO + " = 'ENTREGADO' OR p." + COLUMN_PEDIDO_ESTADO + " = 'RECHAZADO'";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            // Crear tablas principales
            db.execSQL(SQL_CREATE_USUARIOS);
            db.execSQL(SQL_CREATE_TIPOS_INCIDENCIAS);
            db.execSQL(SQL_CREATE_PEDIDOS);
            db.execSQL(SQL_CREATE_PRODUCTOS);
            db.execSQL(SQL_CREATE_INCIDENCIAS);
            db.execSQL(SQL_CREATE_UBICACIONES);
            db.execSQL(SQL_CREATE_DEVOLUCIONES);
            db.execSQL(SQL_CREATE_CALIFICACIONES);
            // Crear índices
            db.execSQL(SQL_CREATE_INDEX_PEDIDOS_USUARIO);
            db.execSQL(SQL_CREATE_INDEX_PRODUCTOS_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_INCIDENCIAS_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_INCIDENCIAS_USUARIO);
            db.execSQL(SQL_CREATE_INDEX_UBICACIONES_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_UBICACIONES_USUARIO);
            db.execSQL(SQL_CREATE_INDEX_DEVOLUCIONES_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_CALIFICACIONES_PEDIDO);
            // Crear vista para reportes
            db.execSQL(SQL_CREATE_VIEW_REPORTES);
            // Insertar datos de prueba
            insertarDatosPrueba(db);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error creating database", e);
            // Consider adding user notification here
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
                crearIndicesV3(db);
            }

            if (oldVersion < 4) {
                // Agregar columnas de forma segura
                agregarColumnaSiNoExiste(db, TABLE_PEDIDOS, COLUMN_PEDIDO_HORA_SALIDA, "INTEGER");
                agregarColumnaSiNoExiste(db, TABLE_PEDIDOS, COLUMN_PEDIDO_HORA_ESTIMADA, "INTEGER");
                agregarColumnaSiNoExiste(db, TABLE_PEDIDOS, COLUMN_PEDIDO_HORA_ENTREGA, "INTEGER");
                agregarColumnaSiNoExiste(db, TABLE_PEDIDOS, COLUMN_PEDIDO_ALERTA_DEMORA, "INTEGER DEFAULT 0");
                agregarColumnaSiNoExiste(db, TABLE_PEDIDOS, COLUMN_PEDIDO_MOTIVO_DEMORA, "TEXT");
                agregarColumnaSiNoExiste(db, TABLE_PEDIDOS, COLUMN_PEDIDO_CONFIRMADO, "INTEGER DEFAULT 0");

                // Crear nuevas tablas si no existen
                try {
                    db.execSQL(SQL_CREATE_DEVOLUCIONES);
                    db.execSQL(SQL_CREATE_CALIFICACIONES);
                } catch (Exception e) {
                    Log.w(TAG, "Tablas ya existen", e);
                }

                // Crear nuevos índices
                crearIndicesV4(db);

                // Crear vista
                db.execSQL(SQL_CREATE_VIEW_REPORTES);
            }

            if (oldVersion < 5) {
                try {
                    db.execSQL("ALTER TABLE " + TABLE_INCIDENCIAS +
                            " ADD COLUMN " + COLUMN_INCIDENCIA_ESTADO +
                            " TEXT NOT NULL DEFAULT '" + IncidenciaDAO.ESTADO_PENDIENTE + "'");
                } catch (Exception e) {
                    Log.w(TAG, "Columna estado ya existe", e);
                }
            }

            if (oldVersion < 6) {
                // Eliminar tablas en orden correcto (hijos antes que padres)
                db.execSQL(SQL_DELETE_CALIFICACIONES);
                db.execSQL(SQL_DELETE_DEVOLUCIONES);
                db.execSQL(SQL_DELETE_UBICACIONES);
                db.execSQL(SQL_DELETE_INCIDENCIAS);
                db.execSQL(SQL_DELETE_PRODUCTOS);
                db.execSQL(SQL_DELETE_PEDIDOS);
                db.execSQL(SQL_DELETE_TIPOS_INCIDENCIAS);
                db.execSQL(SQL_DELETE_USUARIOS);

                // Recrear desde cero
                onCreate(db);
            }

            if (oldVersion < 8) {
                // Migración para añadir usuario_id a calificaciones
                try {
                    // Crear una tabla temporal con la nueva estructura
                    db.execSQL("CREATE TABLE calificaciones_temp (" +
                            COLUMN_CALIFICACION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            COLUMN_CALIFICACION_PEDIDO_ID + " INTEGER NOT NULL, " +
                            COLUMN_CALIFICACION_USUARIO_ID + " INTEGER NOT NULL, " +
                            COLUMN_CALIFICACION_VALOR + " INTEGER NOT NULL CHECK(" + COLUMN_CALIFICACION_VALOR + " BETWEEN 1 AND 5), " +
                            COLUMN_CALIFICACION_COMENTARIO + " TEXT, " +
                            COLUMN_CALIFICACION_FECHA + " INTEGER NOT NULL, " +
                            "FOREIGN KEY(" + COLUMN_CALIFICACION_PEDIDO_ID + ") REFERENCES " +
                            TABLE_PEDIDOS + "(" + COLUMN_PEDIDO_ID + ") ON DELETE CASCADE, " +
                            "FOREIGN KEY(" + COLUMN_CALIFICACION_USUARIO_ID + ") REFERENCES " +
                            TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + "))");

                    // Migrar datos existentes, asignando usuario_id = 1 por defecto
                    db.execSQL("INSERT INTO calificaciones_temp (" +
                            COLUMN_CALIFICACION_ID + ", " +
                            COLUMN_CALIFICACION_PEDIDO_ID + ", " +
                            COLUMN_CALIFICACION_USUARIO_ID + ", " +
                            COLUMN_CALIFICACION_VALOR + ", " +
                            COLUMN_CALIFICACION_COMENTARIO + ", " +
                            COLUMN_CALIFICACION_FECHA + ") " +
                            "SELECT " +
                            COLUMN_CALIFICACION_ID + ", " +
                            COLUMN_CALIFICACION_PEDIDO_ID + ", " +
                            "1, " + // usuario_id por defecto
                            COLUMN_CALIFICACION_VALOR + ", " +
                            COLUMN_CALIFICACION_COMENTARIO + ", " +
                            COLUMN_CALIFICACION_FECHA + " " +
                            "FROM " + TABLE_CALIFICACIONES);

                    // Eliminar la tabla original
                    db.execSQL("DROP TABLE " + TABLE_CALIFICACIONES);

                    // Renombrar la tabla temporal a la original
                    db.execSQL("ALTER TABLE calificaciones_temp RENAME TO " + TABLE_CALIFICACIONES);

                    // Recrear el índice
                    db.execSQL(SQL_CREATE_INDEX_CALIFICACIONES_PEDIDO);
                } catch (Exception e) {
                    Log.w(TAG, "Error al migrar calificaciones", e);
                }
            }
            if (oldVersion < 9) {
                agregarColumnaSiNoExiste(db, TABLE_PEDIDOS, COLUMN_PEDIDO_TOTAL, "REAL DEFAULT 0");
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
        } finally {
            db.endTransaction();
        }
    }

    private void agregarColumnaSiNoExiste(SQLiteDatabase db, String tabla, String columna, String tipo) {
        try {
            db.execSQL("ALTER TABLE " + tabla + " ADD COLUMN " + columna + " " + tipo);
        } catch (Exception e) {
            Log.w(TAG, "Columna " + columna + " ya existe en " + tabla, e);
        }
    }

    private void crearIndicesV3(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_INDEX_PEDIDOS_USUARIO);
            db.execSQL(SQL_CREATE_INDEX_PRODUCTOS_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_INCIDENCIAS_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_INCIDENCIAS_USUARIO);
        } catch (Exception e) {
            Log.w(TAG, "Índices V3 ya existen", e);
        }
    }

    private void crearIndicesV4(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_INDEX_UBICACIONES_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_UBICACIONES_USUARIO);
            db.execSQL(SQL_CREATE_INDEX_DEVOLUCIONES_PEDIDO);
            db.execSQL(SQL_CREATE_INDEX_CALIFICACIONES_PEDIDO);
        } catch (Exception e) {
            Log.w(TAG, "Índices V4 ya existen", e);
        }
    }

    private void insertarDatosPrueba(SQLiteDatabase db) {
        // Insertar usuarios existentes
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USUARIOS +
                " (" + COLUMN_USUARIO_NOMBRE + ", " +
                COLUMN_USUARIO_EMAIL + ", " +
                COLUMN_USUARIO_PASSWORD + ", " +
                COLUMN_USUARIO_ROL + ", " +
                COLUMN_USUARIO_TELEFONO + ", " +
                COLUMN_USUARIO_ACTIVO + ") " +
                "VALUES ('Juan Pérez', 'bryanmp', 'Inversiones', 'REPARTIDOR', '999-123-456', 1)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USUARIOS +
                " (" + COLUMN_USUARIO_NOMBRE + ", " +
                COLUMN_USUARIO_EMAIL + ", " +
                COLUMN_USUARIO_PASSWORD + ", " +
                COLUMN_USUARIO_ROL + ", " +
                COLUMN_USUARIO_TELEFONO + ", " +
                COLUMN_USUARIO_ACTIVO + ") " +
                "VALUES ('Admin Perú', 'admin@example.pe', 'admin123', 'ADMINISTRADOR', '999-987-654', 1)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USUARIOS +
                " (" + COLUMN_USUARIO_NOMBRE + ", " +
                COLUMN_USUARIO_EMAIL + ", " +
                COLUMN_USUARIO_PASSWORD + ", " +
                COLUMN_USUARIO_ROL + ", " +
                COLUMN_USUARIO_TELEFONO + ", " +
                COLUMN_USUARIO_ACTIVO + ") " +
                "VALUES ('María López', 'maria.lopez@example.pe', 'maria123', 'REPARTIDOR', '999-222-333', 1)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USUARIOS +
                " (" + COLUMN_USUARIO_NOMBRE + ", " +
                COLUMN_USUARIO_EMAIL + ", " +
                COLUMN_USUARIO_PASSWORD + ", " +
                COLUMN_USUARIO_ROL + ", " +
                COLUMN_USUARIO_TELEFONO + ", " +
                COLUMN_USUARIO_ACTIVO + ") " +
                "VALUES ('Carlos Gómez', 'carlos.gomez@example.pe', 'carlos123', 'SUPERVISOR', '999-444-555', 1)");
        // Nuevos usuarios
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USUARIOS +
                " (" + COLUMN_USUARIO_NOMBRE + ", " +
                COLUMN_USUARIO_EMAIL + ", " +
                COLUMN_USUARIO_PASSWORD + ", " +
                COLUMN_USUARIO_ROL + ", " +
                COLUMN_USUARIO_TELEFONO + ", " +
                COLUMN_USUARIO_ACTIVO + ") " +
                "VALUES ('Luis Torres', 'luis.torres@example.pe', 'luis123', 'REPARTIDOR', '999-555-666', 1)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USUARIOS +
                " (" + COLUMN_USUARIO_NOMBRE + ", " +
                COLUMN_USUARIO_EMAIL + ", " +
                COLUMN_USUARIO_PASSWORD + ", " +
                COLUMN_USUARIO_ROL + ", " +
                COLUMN_USUARIO_TELEFONO + ", " +
                COLUMN_USUARIO_ACTIVO + ") " +
                "VALUES ('Ana Gómez', 'ana.gomez@example.pe', 'ana123', 'REPARTIDOR', '999-777-888', 1)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USUARIOS +
                " (" + COLUMN_USUARIO_NOMBRE + ", " +
                COLUMN_USUARIO_EMAIL + ", " +
                COLUMN_USUARIO_PASSWORD + ", " +
                COLUMN_USUARIO_ROL + ", " +
                COLUMN_USUARIO_TELEFONO + ", " +
                COLUMN_USUARIO_ACTIVO + ") " +
                "VALUES ('Pedro Castillo', 'pedro.castillo@example.pe', 'pedro123', 'SUPERVISOR', '999-999-000', 1)");

        // Insertar tipos de incidencias
        insertarTiposIncidenciasPrueba(db);

        // Definir tiempos base
        long fechaActual = System.currentTimeMillis();

        // PED-001: ASIGNADO, Miraflores
        long horaEstimadaEntrega = fechaActual + (2 * 60 * 60 * 1000); // 2 horas después
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS + " (" +
                COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ", " +
                COLUMN_PEDIDO_HORA_SALIDA + ", " +
                COLUMN_PEDIDO_HORA_ESTIMADA + ", " +
                COLUMN_PEDIDO_HORA_ENTREGA + ", " +
                COLUMN_PEDIDO_ALERTA_DEMORA + ", " +
                COLUMN_PEDIDO_MOTIVO_DEMORA + ", " +
                COLUMN_PEDIDO_CONFIRMADO + ") " +
                "VALUES ('PED-001', 'Cliente Uno', 'Av. Larco 123, Miraflores, Lima', " +
                fechaActual + ", 'ASIGNADO', 1, 37.7749, -122.4194, " +
                "'Entregar en horario de oficina', " +
                fechaActual + ", " +
                horaEstimadaEntrega + ", " +
                (fechaActual + 30 * 60 * 1000) + ", " +
                "0, 'Sin demoras', 1)");

        // PED-002: EN_RUTA, Cercado de Lima
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS +
                " (" + COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ", " +
                COLUMN_PEDIDO_HORA_SALIDA + ", " +
                COLUMN_PEDIDO_HORA_ESTIMADA + ") " +
                "VALUES ('PED-002', 'Cliente Dos', 'Jr. de la Unión 456, Cercado de Lima', " +
                fechaActual + ", 'EN_RUTA', 1, 37.7749, -122.4194, 'Llamar antes de entregar', " +
                (fechaActual - (30 * 60 * 1000)) + ", " + (horaEstimadaEntrega - (30 * 60 * 1000)) + ")");

        // PED-003: ENTREGADO, Miraflores
        long fechaPedidoAnterior = fechaActual - (2 * 24 * 60 * 60 * 1000); // 2 días antes
        long horaSalidaAnterior = fechaPedidoAnterior + (30 * 60 * 1000);
        long horaEntregaAnterior = horaSalidaAnterior + (90 * 60 * 1000);
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS +
                " (" + COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_UBICACION_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ", " +
                COLUMN_PEDIDO_HORA_SALIDA + ", " +
                COLUMN_PEDIDO_HORA_ESTIMADA + ", " +
                COLUMN_PEDIDO_HORA_ENTREGA + ", " +
                COLUMN_PEDIDO_CONFIRMADO + ") " +
                "VALUES ('PED-003', 'Cliente Tres', 'Calle Schell 789, Miraflores, Lima', " +
                fechaPedidoAnterior + ", 'ENTREGADO', 1, 37.7749, -122.4194, 'Entregar en la recepción', " +
                horaSalidaAnterior + ", " + (horaSalidaAnterior + (60 * 60 * 1000)) + ", " +
                horaEntregaAnterior + ", 1)");

        // PED-004: PENDIENTE, Lince
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS +
                " (" + COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ") " +
                "VALUES ('PED-004', 'Ana Martínez', 'Av. Arequipa 456, Lince, Lima', " +
                fechaActual + ", 'PENDIENTE', NULL, 37.7749, -122.4194, 'Pendiente de asignación')");

        // PED-005: EN_PREPARACION, San Isidro
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS +
                " (" + COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ") " +
                "VALUES ('PED-005', 'José López', 'Av. Javier Prado 789, San Isidro, Lima', " +
                fechaActual + ", 'EN_PREPARACION', 1, 37.7749, -122.4194, 'Preparando en almacén')");

        // PED-006: CANCELADO, San Miguel
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS +
                " (" + COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ") " +
                "VALUES ('PED-006', 'Laura Sánchez', 'Calle Los Olivos 101, San Miguel, Lima', " +
                fechaActual + ", 'CANCELADO', 3, 37.7749, -122.4194, 'Cancelado por el cliente')");

        // PED-007: ENTREGADO, Breña
        long fechaAnterior = fechaActual - (3 * 24 * 60 * 60 * 1000);
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS +
                " (" + COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ", " +
                COLUMN_PEDIDO_HORA_SALIDA + ", " +
                COLUMN_PEDIDO_HORA_ESTIMADA + ", " +
                COLUMN_PEDIDO_HORA_ENTREGA + ", " +
                COLUMN_PEDIDO_CONFIRMADO + ") " +
                "VALUES ('PED-007', 'Pedro Ramírez', 'Av. Brasil 202, Breña, Lima', " +
                fechaAnterior + ", 'ENTREGADO', 3, 37.7749, -122.4194, 'Entregado sin problemas', " +
                (fechaAnterior + (30 * 60 * 1000)) + ", " +
                (fechaAnterior + (90 * 60 * 1000)) + ", " +
                (fechaAnterior + (120 * 60 * 1000)) + ", 1)");

        // PED-008: ENTREGADO, San Isidro
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS + " (" +
                COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ", " +
                COLUMN_PEDIDO_HORA_SALIDA + ", " +
                COLUMN_PEDIDO_HORA_ESTIMADA + ", " +
                COLUMN_PEDIDO_HORA_ENTREGA + ", " +
                COLUMN_PEDIDO_CONFIRMADO + ") " +
                "VALUES ('PED-008', 'Cliente Ocho', 'Av. Benavides 123, San Isidro, Lima', " +
                (fechaActual - 3 * 24 * 60 * 60 * 1000) + ", 'ENTREGADO', 5, 37.7749, -122.4194, 'Entregado con éxito', " +
                (fechaActual - 3 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000) + ", " +
                (fechaActual - 3 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000 + 60 * 60 * 1000) + ", " +
                (fechaActual - 3 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000 + 50 * 60 * 1000) + ", 1)");

        // PED-009: ENTREGADO con retraso, Surco
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS + " (" +
                COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ", " +
                COLUMN_PEDIDO_HORA_SALIDA + ", " +
                COLUMN_PEDIDO_HORA_ESTIMADA + ", " +
                COLUMN_PEDIDO_HORA_ENTREGA + ", " +
                COLUMN_PEDIDO_ALERTA_DEMORA + ", " +
                COLUMN_PEDIDO_MOTIVO_DEMORA + ", " +
                COLUMN_PEDIDO_CONFIRMADO + ") " +
                "VALUES ('PED-009', 'Cliente Nueve', 'Av. Caminos del Inca 456, Surco, Lima', " +
                (fechaActual - 1 * 24 * 60 * 60 * 1000) + ", 'ENTREGADO', 5, 37.7749, -122.4194, 'Entregado con retraso', " +
                (fechaActual - 1 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000) + ", " +
                (fechaActual - 1 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000 + 60 * 60 * 1000) + ", " +
                (fechaActual - 1 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000 + 90 * 60 * 1000) + ", 1, 'Tráfico en la vía', 1)");

        // PED-010: RECHAZADO, La Molina
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS + " (" +
                COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ", " +
                COLUMN_PEDIDO_HORA_SALIDA + ", " +
                COLUMN_PEDIDO_HORA_ESTIMADA + ", " +
                COLUMN_PEDIDO_CONFIRMADO + ") " +
                "VALUES ('PED-010', 'Cliente Diez', 'Av. La Molina 789, La Molina, Lima', " +
                (fechaActual - 4 * 24 * 60 * 60 * 1000) + ", 'RECHAZADO', 6, 37.7749, -122.4194, 'Cliente no aceptó el pedido', " +
                (fechaActual - 4 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000) + ", " +
                (fechaActual - 4 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000 + 60 * 60 * 1000) + ", 0)");

        // PED-011: EN_RUTA, Barranco
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PEDIDOS + " (" +
                COLUMN_PEDIDO_NUMERO + ", " +
                COLUMN_PEDIDO_CLIENTE + ", " +
                COLUMN_PEDIDO_DIRECCION + ", " +
                COLUMN_PEDIDO_FECHA + ", " +
                COLUMN_PEDIDO_ESTADO + ", " +
                COLUMN_PEDIDO_USUARIO_ID + ", " +
                COLUMN_PEDIDO_LATITUD + ", " +
                COLUMN_PEDIDO_LONGITUD + ", " +
                COLUMN_PEDIDO_OBSERVACIONES + ", " +
                COLUMN_PEDIDO_HORA_SALIDA + ", " +
                COLUMN_PEDIDO_HORA_ESTIMADA + ") " +
                "VALUES ('PED-011', 'Cliente Once', 'Av. Pedro de Osma 123, Barranco, Lima', " +
                fechaActual + ", 'EN_RUTA', 1, 37.7749, -122.4194, 'En camino', " +
                (fechaActual - 20 * 60 * 1000) + ", " +
                (fechaActual - 20 * 60 * 1000 + 60 * 60 * 1000) + ")");

        // Insertar productos para los pedidos
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-001', 'Chompa de Alpaca', 'Chompa tejida a mano', 150.00, 2, 1)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-002', 'Poncho Tradicional', 'Poncho de lana', 120.50, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-003', 'Sombrero de Paja', 'Sombrero típico', 50.00, 3, 2)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-004', 'Chalina de Alpaca', 'Chalina tejida', 80.00, 1, 4)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-005', 'Bufanda de Lana', 'Bufanda abrigadora', 60.00, 2, 5)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-006', 'Gorro Andino', 'Gorro de lana', 40.00, 3, 6)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-007', 'Chullo Peruano', 'Gorro tradicional', 30.00, 1, 7)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-008', 'Chompa de Alpaca', 'Chompa tejida a mano', 150.00, 1, 8)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-009', 'Poncho Tradicional', 'Poncho de lana', 120.50, 2, 9)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-010', 'Sombrero de Paja', 'Sombrero típico', 50.00, 1, 10)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PRODUCTOS +
                " (" + COLUMN_PRODUCTO_CODIGO + ", " +
                COLUMN_PRODUCTO_NOMBRE + ", " +
                COLUMN_PRODUCTO_DESCRIPCION + ", " +
                COLUMN_PRODUCTO_PRECIO + ", " +
                COLUMN_PRODUCTO_CANTIDAD + ", " +
                COLUMN_PRODUCTO_PEDIDO_ID + ") " +
                "VALUES ('PROD-011', 'Chalina de Alpaca', 'Chalina tejida', 80.00, 1, 11)");

        // Insertar incidencias
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCIDENCIAS +
                " (" + COLUMN_INCIDENCIA_TIPO + ", " +
                COLUMN_INCIDENCIA_DESCRIPCION + ", " +
                COLUMN_INCIDENCIA_FECHA + ", " +
                COLUMN_INCIDENCIA_USUARIO_ID + ", " +
                COLUMN_INCIDENCIA_PEDIDO_ID + ") " +
                "VALUES ('Retraso', 'Tráfico en Jr. de la Unión', " +
                fechaActual + ", 1, 2)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCIDENCIAS +
                " (" + COLUMN_INCIDENCIA_TIPO + ", " +
                COLUMN_INCIDENCIA_DESCRIPCION + ", " +
                COLUMN_INCIDENCIA_FECHA + ", " +
                COLUMN_INCIDENCIA_USUARIO_ID + ", " +
                COLUMN_INCIDENCIA_PEDIDO_ID + ") " +
                "VALUES ('Producto dañado', 'Caja aplastada al llegar', " +
                (fechaActual - (2 * 24 * 60 * 60 * 1000)) + ", 1, 3)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCIDENCIAS +
                " (" + COLUMN_INCIDENCIA_TIPO + ", " +
                COLUMN_INCIDENCIA_DESCRIPCION + ", " +
                COLUMN_INCIDENCIA_FECHA + ", " +
                COLUMN_INCIDENCIA_USUARIO_ID + ", " +
                COLUMN_INCIDENCIA_PEDIDO_ID + ") " +
                "VALUES ('Retraso', 'Tráfico pesado en Surco', " +
                (fechaActual - 1 * 24 * 60 * 60 * 1000 + 60 * 60 * 1000) + ", 5, 9)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCIDENCIAS +
                " (" + COLUMN_INCIDENCIA_TIPO + ", " +
                COLUMN_INCIDENCIA_DESCRIPCION + ", " +
                COLUMN_INCIDENCIA_FECHA + ", " +
                COLUMN_INCIDENCIA_USUARIO_ID + ", " +
                COLUMN_INCIDENCIA_PEDIDO_ID + ") " +
                "VALUES ('Dirección incorrecta', 'El cliente indicó que la dirección era errónea', " +
                (fechaActual - 4 * 24 * 60 * 60 * 1000 + 45 * 60 * 1000) + ", 6, 10)");

        // Insertar ubicaciones con coordenadas en San Francisco
        // Ubicaciones para PED-002 (en ruta)
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_UBICACIONES +
                " (" + COLUMN_UBICACION_LATITUD + ", " +
                COLUMN_UBICACION_LONGITUD + ", " +
                COLUMN_UBICACION_FECHA + ", " +
                COLUMN_UBICACION_USUARIO_ID + ", " +
                COLUMN_UBICACION_PEDIDO_ID + ") " +
                "VALUES (37.7749, -122.4194, " + (fechaActual - (20 * 60 * 1000)) + ", 1, 2)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_UBICACIONES +
                " (" + COLUMN_UBICACION_LATITUD + ", " +
                COLUMN_UBICACION_LONGITUD + ", " +
                COLUMN_UBICACION_FECHA + ", " +
                COLUMN_UBICACION_USUARIO_ID + ", " +
                COLUMN_UBICACION_PEDIDO_ID + ") " +
                "VALUES (37.7749, -122.4194, " + (fechaActual - (10 * 60 * 1000)) + ", 1, 2)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_UBICACIONES +
                " (" + COLUMN_UBICACION_LATITUD + ", " +
                COLUMN_UBICACION_LONGITUD + ", " +
                COLUMN_UBICACION_FECHA + ", " +
                COLUMN_UBICACION_USUARIO_ID + ", " +
                COLUMN_UBICACION_PEDIDO_ID + ") " +
                "VALUES (37.7749, -122.4194, " + fechaActual + ", 1, 2)");

        // Ubicaciones para PED-011 (en ruta)
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_UBICACIONES +
                " (" + COLUMN_UBICACION_LATITUD + ", " +
                COLUMN_UBICACION_LONGITUD + ", " +
                COLUMN_UBICACION_FECHA + ", " +
                COLUMN_UBICACION_USUARIO_ID + ", " +
                COLUMN_UBICACION_PEDIDO_ID + ") " +
                "VALUES (37.7749, -122.4194, " + (fechaActual - 15 * 60 * 1000) + ", 1, 11)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_UBICACIONES +
                " (" + COLUMN_UBICACION_LATITUD + ", " +
                COLUMN_UBICACION_LONGITUD + ", " +
                COLUMN_UBICACION_FECHA + ", " +
                COLUMN_UBICACION_USUARIO_ID + ", " +
                COLUMN_UBICACION_PEDIDO_ID + ") " +
                "VALUES (37.7749, -122.4194, " + (fechaActual - 10 * 60 * 1000) + ", 1, 11)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_UBICACIONES +
                " (" + COLUMN_UBICACION_LATITUD + ", " +
                COLUMN_UBICACION_LONGITUD + ", " +
                COLUMN_UBICACION_FECHA + ", " +
                COLUMN_UBICACION_USUARIO_ID + ", " +
                COLUMN_UBICACION_PEDIDO_ID + ") " +
                "VALUES (37.7749, -122.4194, " + (fechaActual - 5 * 60 * 1000) + ", 1, 11)");

        // Insertar devoluciones
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_DEVOLUCIONES +
                " (" + COLUMN_DEVOLUCION_PEDIDO_ID + ", " +
                COLUMN_DEVOLUCION_PRODUCTO_ID + ", " +
                COLUMN_DEVOLUCION_CANTIDAD + ", " +
                COLUMN_DEVOLUCION_MOTIVO + ", " +
                COLUMN_DEVOLUCION_FECHA + ", " +
                COLUMN_DEVOLUCION_USUARIO_ID + ") " +
                "VALUES (3, 3, 1, 'Producto defectuoso', " +
                (fechaActual - (1 * 24 * 60 * 60 * 1000)) + ", 1)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_DEVOLUCIONES +
                " (" + COLUMN_DEVOLUCION_PEDIDO_ID + ", " +
                COLUMN_DEVOLUCION_PRODUCTO_ID + ", " +
                COLUMN_DEVOLUCION_CANTIDAD + ", " +
                COLUMN_DEVOLUCION_MOTIVO + ", " +
                COLUMN_DEVOLUCION_FECHA + ", " +
                COLUMN_DEVOLUCION_USUARIO_ID + ") " +
                "VALUES (10, 10, 1, 'Cliente no quiso el producto', " +
                (fechaActual - 4 * 24 * 60 * 60 * 1000 + 60 * 60 * 1000) + ", 6)");

        // Insertar calificaciones
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_CALIFICACIONES +
                " (" + COLUMN_CALIFICACION_PEDIDO_ID + ", " +
                COLUMN_CALIFICACION_USUARIO_ID + ", " +
                COLUMN_CALIFICACION_VALOR + ", " +
                COLUMN_CALIFICACION_COMENTARIO + ", " +
                COLUMN_CALIFICACION_FECHA + ") " +
                "VALUES (3, 1, 4, 'Buen servicio, pero el producto llegó dañado', " +
                (fechaActual - (1 * 24 * 60 * 60 * 1000)) + ")");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_CALIFICACIONES +
                " (" + COLUMN_CALIFICACION_PEDIDO_ID + ", " +
                COLUMN_CALIFICACION_USUARIO_ID + ", " +
                COLUMN_CALIFICACION_VALOR + ", " +
                COLUMN_CALIFICACION_COMENTARIO + ", " +
                COLUMN_CALIFICACION_FECHA + ") " +
                "VALUES (7, 1, 5, 'Excelente servicio', " +
                (fechaAnterior + (24 * 60 * 60 * 1000)) + ")");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_CALIFICACIONES +
                " (" + COLUMN_CALIFICACION_PEDIDO_ID + ", " +
                COLUMN_CALIFICACION_USUARIO_ID + ", " +
                COLUMN_CALIFICACION_VALOR + ", " +
                COLUMN_CALIFICACION_COMENTARIO + ", " +
                COLUMN_CALIFICACION_FECHA + ") " +
                "VALUES (8, 5, 5, 'Excelente servicio', " +
                (fechaActual - 3 * 24 * 60 * 60 * 1000 + 60 * 60 * 1000) + ")");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_CALIFICACIONES +
                " (" + COLUMN_CALIFICACION_PEDIDO_ID + ", " +
                COLUMN_CALIFICACION_USUARIO_ID + ", " +
                COLUMN_CALIFICACION_VALOR + ", " +
                COLUMN_CALIFICACION_COMENTARIO + ", " +
                COLUMN_CALIFICACION_FECHA + ") " +
                "VALUES (9, 5, 3, 'Llegó tarde', " +
                (fechaActual - 1 * 24 * 60 * 60 * 1000 + 2 * 60 * 60 * 1000) + ")");
    }

    private void insertarTiposIncidenciasPrueba(SQLiteDatabase db) {
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_TIPOS_INCIDENCIAS + " (" + COLUMN_TIPO_NOMBRE + ") VALUES ('Retraso')");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_TIPOS_INCIDENCIAS + " (" + COLUMN_TIPO_NOMBRE + ") VALUES ('Producto dañado')");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_TIPOS_INCIDENCIAS + " (" + COLUMN_TIPO_NOMBRE + ") VALUES ('Dirección incorrecta')");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}