package com.hampicare.db;

import com.sun.jdi.connect.spi.ClosedConnectionException;
import jdk.internal.classfile.impl.CatchBuilderImpl;

import javax.sql.ConnectionPoolDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion {

    private static Connection instance;

    private static final String SERVER_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = "hampicare_db";
    private static final String USER = "adminHampiCare";
    private static final String PASSWORD = "adminHampi";

    private static final String SCRIPT_PATH = "src/main/java/com/hampicare/db/Script.sql";

    private Conexion(){}

    public static Connection getInstance(){
        if (instance == null){
            conectarODesplegar();
        }else {
            try{
                if (instance.isClosed()){
                    conectarODesplegar();
                }
            }catch (SQLException e ){
                e.printStackTrace();
            }
        }
        return instance;
    }

    private static void conectarODesplegar(){
        try{
            instance = DriverManager.getConnection(SERVER_URL + DB_NAME, USER, PASSWORD);
            System.out.println("Conexion Exitosa a " + DB_NAME);
        }catch (SQLException e){
            System.out.println("La base de datos no existe o el servidor esta vacio. Iniciando Autoconfiguracion");
            crearBaseDatosYTablas();

            try{
                instance = DriverManager.getConnection(SERVER_URL + DB_NAME, USER, PASSWORD);
                System.out.println("Conexion exitosa tras autoconfiguracion");
            }catch (SQLException e ){
                System.out.println("Fallo critico: el servidor de base de datos no responde");
                instance = null;
            }
        }
    }

    private static void crearBaseDatosYTablas(){
        try(Connection connPostgres = DriverManager.getConnection(SERVER_URL + "postgres" , USER, PASSWORD); Statement stmt = connPostgres.createStatement()){

            stmt.executeUpdate("CREATE DATABASE" + DB_NAME);
            System.out.println("Base de datos " + DB_NAME + " creada con exito");
        }catch(SQLException e){
            System.out.println("Error al crear la base de datos, El usuario no cuenta con los permisos necesarios");
            e.printStackTrace();
            return;
        }

        try (Connection connNuevBD = DriverManager.getConnection(SERVER_URL + DB_NAME, USER, PASSWORD); Statement stmt = connNuevBD.createStatement()){
            String sql = new String(Files.readAllBytes(Paths.get(SCRIPT_PATH)));
            stmt.execute(sql);
            System.out.println("Tablas, enums y datos creados.");
        } catch (SQLException | IOException  e) {
            System.out.println("Error al inyectar el script");
            e.printStackTrace();
        }
    }

    public static void cerrarConexionDB(){
        try{
            if(instance != null && !instance.isClosed()){
                instance.close();
                System.out.println("Conexion cerrada");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

}
