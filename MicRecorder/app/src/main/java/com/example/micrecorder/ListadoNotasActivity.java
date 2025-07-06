package com.example.micrecorder;

// Librerías necesarias para manejo de audio, archivos, listas, fechas, etc.
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class ListadoNotasActivity extends AppCompatActivity {

    // Componentes de la interfaz
    RecyclerView recyclerView;
    NotaVozAdapter adapter;

    List<NotaVoz> listaNotas = new ArrayList<>();

    File carpetaNotas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_listado_notas);

        recyclerView = findViewById(R.id.recyclerViewNotas);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Define la carpeta donde se almacenarán las notas
        carpetaNotas = new File(Environment.getExternalStorageDirectory(), "VoiceJournal/Notas");

        if (!carpetaNotas.exists()) {
            carpetaNotas.mkdirs();
        }

        // Carga las notas de voz desde la carpeta al iniciar la actividad
        try {
            cargarNotas();
            Toast.makeText(this, "Notas encontradas: " + listaNotas.size(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Crea el adaptador y lo asigna al RecyclerView
        adapter = new NotaVozAdapter(listaNotas, this);
        recyclerView.setAdapter(adapter);
    }
    private String obtenerDuracion(String ruta) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(ruta);

        // Extrae la duración en milisegundos
        String duracionMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duracion = Long.parseLong(duracionMs);

        retriever.release();

        // Conviersión
        int segundos = (int) (duracion / 1000) % 60;
        int minutos = (int) (duracion / (1000 * 60));

        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }

    /**
     * Metodo que recorre los archivos de audio en la carpeta
     * y los carga como objetos NotaVoz en la lista.
     */
    private void cargarNotas() throws IOException {
        File[] archivos = carpetaNotas.listFiles(); // Obtiene todos los archivos de la carpeta
        if (archivos != null) {
            for (File archivo : archivos) {

                // Solo toma los archivos con extensión .3gp (notas de voz)
                if (archivo.isFile() && archivo.getName().endsWith(".3gp")) {
                    String nombre = archivo.getName();
                    String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(new Date(archivo.lastModified()));

                    String ruta = "";
                    String duracion = obtenerDuracion(ruta);

                    // Crea un objeto NotaVoz y lo agrega a la lista
                    listaNotas.add(new NotaVoz(nombre, ruta, fecha, duracion));
                }
            }
        }
    }

    /**
     * Metodo que obtiene la duración de un archivo de audio usando MediaMetadataRetriever
     */


}
