package com.hampicare.model;

import java.util.Set;

public abstract class Usuario extends Persona {

    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_CAJERO = "CAJERO";
    public static final String ROL_REPORTES = "REPORTES";

    private String contrasena;
    private String rol;
    private boolean activo;

    protected Usuario() {
        super();
    }

    protected Usuario(int id, String nombre, String correo, String contrasena, String rol, boolean activo) {
        super(id, nombre, correo);
        setContrasena(contrasena);
        this.rol = rol;
        this.activo = activo;
    }

    public String getContrasena() {
        return contrasena;
    }

    /** Validación obligatoria de la rúbrica: mínimo 6 caracteres. */
    public void setContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /** POLIMORFISMO: cada rol devuelve el conjunto de módulos que puede ver. */
    public abstract Set<String> getModulosPermitidos();

    /** POLIMORFISMO: cada rol tiene su propio color de acento en la UI. */
    public abstract String getClaseColorRol();

    /** POLIMORFISMO: indica si el rol puede eliminar registros (solo lectura vs operativo). */
    public abstract boolean puedeEliminar();

    @Override
    public String getDescripcionRol() {
        return rol;
    }

    public static Usuario crearPorRol(int id, String nombre, String correo,
                                      String contrasena, String rol, boolean activo) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo.");
        }
        switch (rol.trim().toUpperCase()) {
            case ROL_ADMIN:
                return new Administrador(id, nombre, correo, contrasena, activo);
            case ROL_CAJERO:
                return new Cajero(id, nombre, correo, contrasena, activo);
            case ROL_REPORTES:
                return new ReportesUsuario(id, nombre, correo, contrasena, activo);
            default:
                throw new IllegalArgumentException("Rol no reconocido: " + rol);
        }
    }
}
