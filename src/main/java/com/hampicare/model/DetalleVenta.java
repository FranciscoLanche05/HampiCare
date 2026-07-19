package com.hampicare.model;

public class DetalleVenta {

    private int id;
    private int ventaId;
    private int medicamentoId;
    private String nombreMedicamento; // solo para mostrar en tabla/PDF, no se persiste directamente
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetalleVenta() {
    }

    public DetalleVenta(int medicamentoId, String nombreMedicamento, int cantidad, double precioUnitario) {
        this.medicamentoId = medicamentoId;
        this.nombreMedicamento = nombreMedicamento;
        this.precioUnitario = precioUnitario;
        setCantidad(cantidad);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVentaId() {
        return ventaId;
    }

    public void setVentaId(int ventaId) {
        this.ventaId = ventaId;
    }

    public int getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(int medicamentoId) {
        this.medicamentoId = medicamentoId;
    }

    public String getNombreMedicamento() {
        return nombreMedicamento;
    }

    public void setNombreMedicamento(String nombreMedicamento) {
        this.nombreMedicamento = nombreMedicamento;
    }

    public int getCantidad() {
        return cantidad;
    }

    /** Validación: cantidad debe ser positiva. Recalcula el subtotal automáticamente. */
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }
        this.cantidad = cantidad;
        this.subtotal = cantidad * precioUnitario;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        if (precioUnitario <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0.");
        }
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
