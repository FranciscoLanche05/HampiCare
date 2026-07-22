package com.hampicare.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;

public final class Conexion {

    private static Conexion instancia;
    private Connection conn;

    private static final String URL = System.getProperty("SUPABASE_URL");
    private static final String USUARIO = System.getProperty("SUPABASE_USER");
    private static final String CLAVE = System.getProperty("SUPABASE_PASSWORD", "");

    private Conexion() {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("[DB] Conectando a: " + URL);
            conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("[DB] Conexion establecida OK");
            initDatabase();
        } catch (SQLException e) {
            System.err.println("[DB] FALLO Conexion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL no encontrado", e);
        }
    }

    private void initDatabase() {
        try (Statement stmt = conn.createStatement()) {
            System.out.println("[DB] Verificando si existen tablas...");
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'usuarios'");
            int count = rs.next() ? rs.getInt(1) : -1;
            System.out.println("[DB] Tablas encontradas: " + count);
            if (count == 0) {
                System.out.println("[DB] Ejecutando script de inicializacion...");
                InputStream is = getClass().getResourceAsStream("/Script.sql");
                if (is == null) {
                    System.err.println("[DB] Script.sql no encontrado en classpath.");
                    return;
                }
                String script;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    script = sb.toString();
                }

                // PostgreSQL: statements separated by semicolons, not lines
                // Strip comments (-- and /* */) then split on ;
                String cleanScript = script.replaceAll("(?m)^--.*$", "");
                cleanScript = cleanScript.replaceAll("/\\*[\\s\\S]*?\\*/", "");
                for (String sql : cleanScript.split(";")) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
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
            if (conn == null || conn.isClosed() || (conn.getAutoCommit() && !conn.isValid(2))) {
                if (conn != null && !conn.isClosed()) {
                    try { conn.close(); } catch (Exception ignored) {}
                }
                System.out.println("[DB] Reconectando a Supabase...");
                conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando la conexion: " + e.getMessage(), e);
        }
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        return null; // Ignore close() calls to protect the singleton connection
                    }
                    try {
                        return method.invoke(conn, args);
                    } catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                });
    }
}
