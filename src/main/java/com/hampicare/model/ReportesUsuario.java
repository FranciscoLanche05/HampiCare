package com.hampicare.model;

import java.util.Set;

/**
 * Rol de solo lectura: ve estadísticas, gráficos y puede exportar
 * reportes, pero no puede modificar ningún dato. Color de acento: morado.
 */
public class ReportesUsuario extends Usuario {

    public ReportesUsuario() {
        super();
    }

    public ReportesUsuario(int id, String nombre, String correo, String contrasena, boolean activo) {
        super(id, nombre, correo, contrasena, ROL_REPORTES, activo);
    }

    @Override
    public Set<String> getModulosPermitidos() {
        return Set.of("HOME", "REPORTES");
    }

    @Override
    public String getClaseColorRol() {
        return "role-reportes";
    }

    @Override
    public boolean puedeEliminar() {
        return false;
    }

    @Override
    public String getDescripcionRol() {
        return "Encargado de Reportes";
    }
}
