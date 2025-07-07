package com.example.micrecorder;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.content.Intent;
import android.widget.Button;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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
    private Button btnRetroceso; //botón de retroceso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_notas); // ← SOLO tiene RecyclerView

        // Inicializar botón de retroceso
        btnRetroceso = findViewById(R.id.btnRetroceso);
        btnRetroceso.setOnClickListener(v -> {
            Intent intent = new Intent(this, InicioActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        recyclerView = findViewById(R.id.recyclerViewNotas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carpetaNotas = getNotesFolder(this);
        if (carpetaNotas == null) {
            Toast.makeText(this, "No se pudo acceder a la carpeta", Toast.LENGTH_SHORT).show();
        }
        if (!carpetaNotas.exists()) carpetaNotas.mkdirs();
        VerificarPermisos();

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
        if (carpetaNotas == null || !carpetaNotas.exists() || !carpetaNotas.isDirectory()) {
            throw new IOException("Carpeta no disponible o inválida");
        }

        File[] archivos = carpetaNotas.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                String nombre = archivo.getName().toLowerCase();
                if (archivo.isFile() && (nombre.endsWith(".3gp") || nombre.endsWith(".3ga"))) {
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

    public File getNotesFolder(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getPublicDirectoryUsingMediaStore(context);
        } else {
            File recordingsDir = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recordingsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RECORDINGS);
            }
            File customFolder = new File(recordingsDir, "AudiosDSVIJuanZamoraNosExplotaAYUDA");

            if (!customFolder.exists()) {
                boolean created = customFolder.mkdirs(); // Intenta crearla si no existe
                if (!created) {
                    Log.e("ListadoNotas", "No se pudo crear la carpeta");
                    return null;
                }
            }

            return customFolder;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private File getPublicDirectoryUsingMediaStore(Context context) {
        ContentResolver resolver = context.getContentResolver();

        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
        String[] selectionArgs = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            selectionArgs = new String[]{
                    Environment.DIRECTORY_RECORDINGS + "/AudiosDSVIJuanZamoraNosExplotaAYUDA"
            };
        }

        Cursor cursor = resolver.query(collection, null, selection, selectionArgs, null);

        File folder = null;

        if (cursor != null && cursor.moveToFirst()) {
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String firstFilePath = cursor.getString(dataColumn);
            folder = new File(firstFilePath).getParentFile();
        }

        if (folder == null || !folder.exists()) {
            // Si no hay archivos, creamos la carpeta localmente
            File publicDir = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RECORDINGS);
            }
            folder = new File(publicDir, "AudiosDSVIJuanZamoraNosExplotaAYUDA");
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return folder;
    }

    // Método auxiliar para obtener la ruta desde el URI
    private String getPathFromURI(Context context, Uri uri) {
        String path = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    path = cursor.getString(index);
                }
                cursor.close();
            }
        } else {
            path = uri.getPath();
        }
        return path;
    }
}
