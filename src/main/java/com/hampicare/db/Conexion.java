package com.hampicare.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Conexion {

    private static Conexion instancia;
    private Connection conn;

    // Configuración para desarrollo local con H2 persistente
    private static final String URL = "jdbc:h2:./hampicare_db;MODE=PostgreSQL";
    private static final String USUARIO = "sa";
    private static final String CLAVE = "";

    private Conexion() {
        try {
            System.out.println("[DB] Conectando a: " + URL);
            conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("[DB] Conexion establecida OK");
            initDatabase();
        } catch (SQLException e) {
            System.err.println("[DB] FALLO Conexion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }
    
    private void initDatabase() {
        try (Statement stmt = conn.createStatement()) {
            System.out.println("[DB] Verificando si existen tablas...");
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'USUARIOS' " +
                    "OR table_name = 'usuarios'");
            int count = rs.next() ? rs.getInt(1) : -1;
            System.out.println("[DB] Tablas encontradas: " + count);
            if (count == 0) {
                System.out.println("[DB] Ejecutando script de inicializacion...");
                stmt.execute("RUNSCRIPT FROM 'classpath:/com/hampicare/db/ScriptH2.sql'");
                System.out.println("[DB] Script ejecutado OK");
            } else {
                System.out.println("[DB] Base de datos ya inicializada.");
            }
        } catch (Exception e) {
            System.err.println("[DB] ERROR en initDatabase: " + e.getMessage());
            e.printStackTrace();
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