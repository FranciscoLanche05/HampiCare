package com.hampicare.model;

import java.util.Set;

public class Administrador extends Usuario {

    public Administrador() {
        super();
    }

    public Administrador(int id, String nombre, String correo, String contrasena, boolean activo) {
        super(id, nombre, correo, contrasena, ROL_ADMIN, activo);
    }

    @Override
    public Set<String> getModulosPermitidos() {
        return Set.of("HOME", "INVENTARIO", "USUARIOS", "REPORTES", "CONFIGURACION", "VENTAS", "CLIENTES", "PROVEEDORES", "COMPRAS");
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
