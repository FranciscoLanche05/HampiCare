package com.hampicare.model;

/**
 * Clase base abstracta. Toda entidad que representa a una persona dentro
 * del sistema (Usuario, y a futuro Cliente/Proveedor si se requiere)
 * hereda de aquí.
 *
 * PILAR APLICADO: Abstracción + Encapsulamiento
 * - Los atributos son 'protected' para que las clases hijas puedan
 *   acceder directamente sin romper el encapsulamiento hacia el exterior.
 * - getDescripcionRol() es un método abstracto: cada clase hija decide
 *   cómo se describe a sí misma (esto se termina de resolver con
 *   POLIMORFISMO en las clases hijas de Usuario).
 */
public abstract class Persona {

    protected int id;
    protected String nombre;
    protected String correo;

    protected Persona() {
    }

    protected Persona(int id, String nombre, String correo) {
        this.id = id;
        setNombre(nombre);
        setCorreo(correo);
    }

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
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo no puede estar vacío.");
        }
        this.correo = correo.trim();
    }

    /** Método abstracto: cada subclase concreta define qué es "su rol". */
    public abstract String getDescripcionRol();
}
