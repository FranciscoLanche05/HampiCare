package com.hampicare.model;

public class Usuario extends Persona {
    public static final String ROL_ADMIN    = "ADMIN";
    public static final String ROL_CAJERO   = "CAJERO";
    public static final String ROL_REPORTES = "REPORTES";
    private String  rol;
    private boolean activo;

    // ---------------------------------------------------------------
    // Constructores
    // ---------------------------------------------------------------

    public Usuario() {
        super();
        this.activo = true;
    }


    public Usuario(int id, String nombre, String correo, String contrasena,
                   String rol, boolean activo) {
        super(id, nombre, correo, contrasena);  // llama al constructor de Persona
        this.rol    = rol;
        this.activo = activo;
    }


    public Usuario(String nombre, String correo, String contrasena, String rol) {
        super(0, nombre, correo, contrasena);
        this.rol    = rol;
        this.activo = true;
    }

    // ---------------------------------------------------------------
    // Getters y Setters propios
    // ---------------------------------------------------------------

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

    // ---------------------------------------------------------------
    // Implementación de métodos abstractos de Persona (POLIMORFISMO)
    // ---------------------------------------------------------------

    @Override
    public String getRolDescripcion() {
        switch (rol) {
            case ROL_ADMIN:
                return "Administrador — acceso total al sistema";
            case ROL_CAJERO:
                return "Cajero — registro de ventas e inventario";
            case ROL_REPORTES:
                return "Reportes — solo lectura y exportación";
            default:
                return "Rol desconocido";
        }
    }


    @Override
    public boolean esValido() {
        if (getNombre()     == null || getNombre().trim().isEmpty())     return false;
        if (getCorreo()     == null || getCorreo().trim().isEmpty())     return false;
        if (getContrasena() == null || getContrasena().length() < 6)    return false;
        if (!ROL_ADMIN.equals(rol) && !ROL_CAJERO.equals(rol) && !ROL_REPORTES.equals(rol)) {
            return false;
        }
        return true;
    }

    // ---------------------------------------------------------------
    // Métodos de conveniencia (útiles en el controlador)
    // ---------------------------------------------------------------

    public boolean esAdmin() {
        return ROL_ADMIN.equals(rol);
    }

    public boolean esCajero() {
        return ROL_CAJERO.equals(rol);
    }

    public boolean esReportes() {
        return ROL_REPORTES.equals(rol);
    }

    // ---------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------

    @Override
    public String toString() {
        return "Usuario{id=" + getId()
                + ", nombre='" + getNombre() + "'"
                + ", correo='" + getCorreo() + "'"
                + ", rol='" + rol + "'"
                + ", activo=" + activo + "}";
    }
}