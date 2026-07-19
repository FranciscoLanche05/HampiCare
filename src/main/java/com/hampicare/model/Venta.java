package com.hampicare.model;

import java.time.LocalDateTime;

/** Cabecera de una venta.*/
public class Venta {

    private int id;
    private int usuarioId;
    private String numeroFactura;
    private LocalDateTime fecha;
    private double total;

    public Venta() {
    }

    public Venta(int id, int usuarioId, String numeroFactura, LocalDateTime fecha, double total) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
