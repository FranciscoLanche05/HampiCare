package com.hampicare.db;

public class TestConn {
    public static void main(String[] args) {
        System.out.println("Intentando conectar...");
        try {
            java.sql.Connection conn = Conexion.getInstancia().getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("¡Conexión exitosa!");
            } else {
                System.out.println("La conexión es nula o está cerrada.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
