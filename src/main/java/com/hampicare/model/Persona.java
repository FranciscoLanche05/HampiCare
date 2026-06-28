package com.hampicare.model;

public abstract class
Persona {
    private int    id;
    private String nombre;
    private String correo;
    private String contrasena;

    // ---------------------------------------------------------------
    // Constructores
    // ---------------------------------------------------------------

    public Persona() {}

    public Persona(int id, String nombre, String correo, String contrasena) {
        this.id         = id;
        this.nombre     = nombre;
        this.correo     = correo;
        this.contrasena = contrasena;
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    // ---------------------------------------------------------------
    // Métodos abstractos (ABSTRACCIÓN + POLIMORFISMO)
    // ---------------------------------------------------------------

    public abstract String getRolDescripcion();
    public abstract boolean esValido();

    // ---------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------

    @Override
    public String toString() {
        return "Persona{id=" + id + ", nombre='" + nombre + "', correo='" + correo + "'}";
    }
}