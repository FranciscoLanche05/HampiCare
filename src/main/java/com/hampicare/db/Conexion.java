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

    // Se obtienen las variables pasadas por Launch4j (JVM options: -DSUPABASE_URL=...)
    private static final String URL = System.getProperty("SUPABASE_URL");
    private static final String USUARIO = System.getProperty("SUPABASE_USER");
    private static final String CLAVE = System.getProperty("SUPABASE_PASSWORD");

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