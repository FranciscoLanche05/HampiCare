package com.hampicare.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * PATRÓN SINGLETON: garantiza que exista una única instancia de la
 * conexión a la base de datos en toda la aplicación.
 *
 * IMPORTANTE: ajusta URL / USUARIO / CLAVE a tu entorno antes de correr
 * la app. El script de creación de la BD está en /sql/schema.sql
 * (recuerda: en PostgreSQL hay que crear la base de datos "hampipharma_db"
 * ANTES de correr el script, con: CREATE DATABASE hampipharma_db;)
 */
public final class Conexion {

    private static Conexion instancia;
    private Connection conn;

    // Configuración para desarrollo local con H2 (En memoria, se reinicia al cerrar la app)
    private static final String URL = "jdbc:h2:mem:hampicare_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM './src/main/java/com/hampicare/db/Script.sql'";
    private static final String USUARIO = "sa";
    private static final String CLAVE = "";

    private Conexion() {
        try {
            conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }

    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    public Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando la conexión: " + e.getMessage(), e);
        }
        return conn;
    }
}