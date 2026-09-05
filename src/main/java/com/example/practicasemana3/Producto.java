package com.example.practicasemana3;

public class Producto {
    private String codigo;
    private String nombre;
    private String categoria;
    private double precio;
    private int existencia;
    private String preparacion;


    public Producto(String codigo, String nombre, String categoria,
                    double precio, int existencia, String preparacion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.existencia = existencia;
        this.preparacion = preparacion;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public double getPrecio() { return precio; }
    public int getExistencia() { return existencia; }
    public String getPreparacion() { return preparacion; }

    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setExistencia(int existencia) { this.existencia = existencia; }
    public void setPreparacion(String preparacion) { this.preparacion = preparacion; }
}