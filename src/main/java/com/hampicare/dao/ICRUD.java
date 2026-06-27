package com.hampicare.dao;
import java.util.List;


public interface ICRUD<T> {
    boolean guardar(T objeto);
    List<T> listar();
    boolean actualizar(T objeto);
    boolean eliminar(int id);
}