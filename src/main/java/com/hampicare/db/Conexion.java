package com.hampicare.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * PATRÓN SINGLETON: garantiza que exista una única instancia de la
 * conexión a la base de datos en toda la aplicación.
 */
public final class Conexion {

    private static Conexion instancia;
    private Connection conn;

    // Configuración para desarrollo local con H2 persistente
    private static final String URL = "jdbc:h2:./hampicare_db;MODE=PostgreSQL";
    private static final String USUARIO = "sa";
    private static final String CLAVE = "";

    private Conexion() {
        try {
            conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            initDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }
    
    private void initDatabase() {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'USUARIOS'");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Creando la base de datos por primera vez...");
                stmt.execute("RUNSCRIPT FROM './src/main/java/com/hampicare/db/Script.sql'");
            }
        } catch (Exception e) {
            System.err.println("Error al ejecutar el script inicial: " + e.getMessage());
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