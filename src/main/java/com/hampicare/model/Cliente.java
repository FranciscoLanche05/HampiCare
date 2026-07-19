package com.hampicare.model;

public class Cliente extends Persona {

    private String apellidos;
    private String cedula;
    private String telefono;
    private String sector;

    public Cliente() {
        super();
    }

    public Cliente(int id, String nombres, String apellidos, String correo, String cedula, String telefono, String sector) {
        super(id, nombres, correo);
        this.apellidos = apellidos;
        this.cedula = cedula;
        this.telefono = telefono;
        this.sector = sector;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    /** Alias para PropertyValueFactory que busca 'nombres' */
    public String getNombres() { return getNombre(); }

    @Override
    public String toString() { return getNombre() + " " + apellidos; }

    @Override
    public String getDescripcionRol() {
        return "Cliente";
    }
}
