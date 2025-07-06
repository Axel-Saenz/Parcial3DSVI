package com.example.micrecorder;

public class NotaVoz {
    private String nombreArchivo;
    private String ruta;
    private String fechaHora;
    private String duracion;

    public NotaVoz(String nombreArchivo, String ruta, String fechaHora, String duracion) {
        this.nombreArchivo = nombreArchivo;
        this.ruta = ruta;
        this.fechaHora = fechaHora;
        this.duracion = duracion;
    }

    public String getNombreArchivo() { return nombreArchivo; }
    public String getRuta() { return ruta; }
    public String getFechaHora() { return fechaHora; }
    public String getDuracion() { return duracion; }
}
