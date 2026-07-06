package com.hampicare.model;

import java.util.Set;

/**
 * Rol con acceso total: CRUD completo, gestión de usuarios, reportes y
 * configuración del sistema. Color de acento: sage (verde salvia, ya
 * usado en el diseño original de Hampi Pharma).
 */
public class Administrador extends Usuario {

    public Administrador() {
        super();
    }

    public Administrador(int id, String nombre, String correo, String contrasena, boolean activo) {
        super(id, nombre, correo, contrasena, ROL_ADMIN, activo);
    }

    @Override
    public Set<String> getModulosPermitidos() {
        return Set.of("HOME", "INVENTARIO", "USUARIOS", "REPORTES", "CONFIGURACION");
    }

    @Override
    public String getClaseColorRol() {
        return "role-admin";
    }

    @Override
    public boolean puedeEliminar() {
        return true;
    }

    @Override
    public String getDescripcionRol() {
        return "Administrador";
    }
}
