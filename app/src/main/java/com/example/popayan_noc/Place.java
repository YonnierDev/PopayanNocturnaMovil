package com.example.popayan_noc;

public class Place {
    public int id;
    public int categoriaid;
    public int usuarioid;
    public String nombre;
    public String descripcion;
    public String ubicacion;
    public boolean estado;
    public String imagen;
    public boolean aprobacion;
    public Double rating; // Nuevo campo para el rating promedio

    // Constructor anterior (sin rating)
    public Place(int id, int categoriaid, int usuarioid, String nombre, String descripcion, String ubicacion, boolean estado, String imagen, boolean aprobacion) {
        this.id = id;
        this.categoriaid = categoriaid;
        this.usuarioid = usuarioid;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.imagen = imagen;
        this.aprobacion = aprobacion;
        this.rating = null;
    }

    // Nuevo constructor con rating
    public Place(int id, int categoriaid, int usuarioid, String nombre, String descripcion, String ubicacion, boolean estado, String imagen, boolean aprobacion, Double rating) {
        this.id = id;
        this.categoriaid = categoriaid;
        this.usuarioid = usuarioid;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.imagen = imagen;
        this.aprobacion = aprobacion;
        this.rating = rating;
    }
}

