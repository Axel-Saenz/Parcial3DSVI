package com.example.micrecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListadoNotasActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotaVozAdapter adapter;
    List<NotaVoz> listaNotas = new ArrayList<>();
    File carpetaNotas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_notas); // ← SOLO tiene RecyclerView

        recyclerView = findViewById(R.id.recyclerViewNotas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carpetaNotas = new File(getExternalFilesDir(null), "VoiceJournal/Notas");
        if (!carpetaNotas.exists()) carpetaNotas.mkdirs();

        try {
            cargarNotas();
            Toast.makeText(this, "Notas encontradas: " + listaNotas.size(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al cargar notas", Toast.LENGTH_SHORT).show();
        }

        adapter = new NotaVozAdapter(listaNotas, this);
        recyclerView.setAdapter(adapter);

        VerificarPermisos();
    }

    private void cargarNotas() throws IOException {
        File[] archivos = carpetaNotas.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile() && archivo.getName().endsWith(".3gp")) {
                    String nombre = archivo.getName();
                    String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(new Date(archivo.lastModified()));
                    String ruta = archivo.getAbsolutePath();
                    String duracion = obtenerDuracion(ruta);
                    listaNotas.add(new NotaVoz(nombre, ruta, fecha, duracion));
                }
            }
        }
    }

    private String obtenerDuracion(String ruta) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(ruta);
        String duracionMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duracion = Long.parseLong(duracionMs);
        retriever.release();

        int segundos = (int) (duracion / 1000) % 60;
        int minutos = (int) (duracion / (1000 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }

    private void VerificarPermisos() {
        List<String> permisos = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.RECORD_AUDIO);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permisos.isEmpty()) {
            ActivityCompat.requestPermissions(this, permisos.toArray(new String[0]), 200);
        }
    }
}
