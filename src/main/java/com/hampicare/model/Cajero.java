package com.hampicare.model;

import java.util.Set;

public class Cajero extends Usuario {

    public Cajero() {
        super();
    }

    public Cajero(int id, String nombre, String correo, String contrasena, boolean activo) {
        super(id, nombre, correo, contrasena, ROL_CAJERO, activo);
    }

    @Override
    public Set<String> getModulosPermitidos() {
        return Set.of("HOME", "VENTAS", "INVENTARIO");
    }

    @Override
    public String getClaseColorRol() {
        return "role-cajero";
    }

    @Override
    public boolean puedeEliminar() {
        return false;
    }

    @Override
    public String getDescripcionRol() {
        return "Cajero";
    }
}
