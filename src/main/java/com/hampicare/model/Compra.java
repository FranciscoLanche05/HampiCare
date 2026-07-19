package com.hampicare.model;

import java.time.LocalDateTime;

public class Compra {

    private int id;
    private int proveedorId;
    private int medicamentoId;
    private int cantidad;
    private double precioCompra;
    private LocalDateTime fecha;

    // Transient fields for UI display
    private String proveedorNombre;
    private String medicamentoNombre;

    public Compra() {
    }

    public Compra(int id, int proveedorId, int medicamentoId, int cantidad, double precioCompra, LocalDateTime fecha) {
        this.id = id;
        this.proveedorId = proveedorId;
        this.medicamentoId = medicamentoId;
        this.cantidad = cantidad;
        this.precioCompra = precioCompra;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }
    public int getMedicamentoId() { return medicamentoId; }
    public void setMedicamentoId(int medicamentoId) { this.medicamentoId = medicamentoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
    public String getMedicamentoNombre() { return medicamentoNombre; }
    public void setMedicamentoNombre(String medicamentoNombre) { this.medicamentoNombre = medicamentoNombre; }
}
