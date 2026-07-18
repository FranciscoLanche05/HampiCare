package com.hampicare.model;

import java.time.LocalDate;

public class Medicamento {

    private int id;
    private String nombre;
    private String categoria;
    private double precio;
    private int stock;
    private String lote;
    private LocalDate fechaVencimiento;
    private int proveedorId;

    public Medicamento() {
    }

    public Medicamento(int id, String nombre, String categoria, double precio, int stock,
                       String lote, LocalDate fechaVencimiento, int proveedorId) {
        this.id = id;
        setNombre(nombre);
        this.categoria = categoria;
        setPrecio(precio);
        setStock(stock);
        this.lote = lote;
        this.fechaVencimiento = fechaVencimiento;
        this.proveedorId = proveedorId;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    /** Validación obligatoria: solo números positivos. */
    public void setPrecio(double precio) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0.");
        }
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    /** Validación obligatoria: solo números positivos (o cero, agotado). */
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.stock = stock;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(int proveedorId) {
        this.proveedorId = proveedorId;
    }

    /** true si quedan 15 unidades o menos (alerta de "stock bajo"). */
    public boolean isStockBajo() {
        return stock <= 15;
    }
}
