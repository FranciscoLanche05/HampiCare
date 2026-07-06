package com.hampicare.model;

import java.util.Set;

/**
 * Usuario del sistema. Es abstracta a propósito: nunca se instancia
 * "Usuario" directamente, siempre una de sus 3 subclases concretas
 * (Administrador, Cajero, ReportesUsuario), cada una con su propio
 * comportamiento.
 *
 * PILAR APLICADO: Herencia (extiende Persona) + Encapsulamiento
 * (contrasena/rol/activo son privados con getters/setters validados)
 * + Abstracción (getModulosPermitidos/getClaseColor son el "contrato"
 * que cada rol debe cumplir).
 */
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

    /**
     * POLIMORFISMO: cada rol devuelve el conjunto de módulos que puede ver.
     * El DashboardController usa este método para mostrar/ocultar
     * secciones — así se logra "una sola pantalla que se adapta según el
     * rol", sin necesidad de tres dashboards distintos.
     */
    public abstract Set<String> getModulosPermitidos();

    /** POLIMORFISMO: cada rol tiene su propio color de acento en la UI. */
    public abstract String getClaseColorRol();

    /** POLIMORFISMO: indica si el rol puede eliminar registros (solo lectura vs operativo). */
    public abstract boolean puedeEliminar();

    @Override
    public String getDescripcionRol() {
        return rol;
    }

    /**
     * Fábrica estática: dado el rol guardado en la base de datos, crea la
     * subclase concreta correspondiente. Esto evita usar 'if/else' de rol
     * por todo el código: una vez creado el objeto correcto, cada método
     * heredado ya se comporta distinto por sí mismo (polimorfismo real).
     */
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
