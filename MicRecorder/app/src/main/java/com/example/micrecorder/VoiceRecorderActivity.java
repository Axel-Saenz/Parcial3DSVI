package com.example.micrecorder;

import android.os.Build;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class VoiceRecorderActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 200;

    private TextView tvTimer;
    private Button btnStart, btnPause, btnStop, btnRetroceso;
    private View indicadorGrabando; // indicador para animacion mientras graba

    private MediaRecorder recorder;
    private String filePath;

    private boolean isRecording = false;
    private boolean isPaused = false;

    // Para contar el tiempo
    private Handler handler = new Handler();
    private long startTime = 0L;
    private long pausedTime = 0L;  // tiempo acumulado antes de la pausa

    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            long elapsed = System.currentTimeMillis() - startTime + pausedTime;
            int secs = (int) (elapsed / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recorder);

        tvTimer   = findViewById(R.id.tvTimer);
        btnStart  = findViewById(R.id.btnStart);
        btnPause  = findViewById(R.id.btnPause);
        btnStop   = findViewById(R.id.btnStop);
        indicadorGrabando = findViewById(R.id.indicadorGrabando);

        // Inicializar botón de retroceso
        btnRetroceso = findViewById(R.id.btnRetroceso);
        btnRetroceso.setOnClickListener(v -> {
            Intent intent = new Intent(this, InicioActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnPause.setEnabled(false);
        btnStop.setEnabled(false);

        btnStart.setOnClickListener(v -> {
            if (checkPermissions()) startRecording();
        });

        btnPause.setOnClickListener(v -> {
            if (isRecording && !isPaused) {
                pauseRecording();
            } else if (isRecording && isPaused) {
                resumeRecording();
            }
        });

        btnStop.setOnClickListener(v -> stopRecording());
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void startRecording() {
        try {
            // Prepara ruta de almacenamiento
            // Reemplaza getExternalStorageDirectory()
            File folder = new File(getExternalFilesDir(null), "VoiceJournal/Notas");

            if (!folder.exists()) folder.mkdirs();

            String filename = "nota_" + System.currentTimeMillis() + ".3gp";
            filePath = new File(folder, filename).getAbsolutePath();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(filePath);
            recorder.prepare();
            recorder.start();

            isRecording = true;
            isPaused = false;
            startTime = System.currentTimeMillis();
            pausedTime = 0L;
            handler.post(updateTimer);

            btnStart.setEnabled(false);
            btnPause.setEnabled(true);
            btnStop.setEnabled(true);
            btnPause.setText("⏸ Pausar");

            //Mostrar animación de grabando
            indicadorGrabando.setVisibility(View.VISIBLE);
            Animation blink = AnimationUtils.loadAnimation(this, R.anim.blink);
            indicadorGrabando.startAnimation(blink);


            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder.pause();
            isPaused = true;
            handler.removeCallbacks(updateTimer);
            // Guarda el tiempo transcurrido hasta la pausa
            pausedTime += System.currentTimeMillis() - startTime;
            btnPause.setText("▶ Reanudar");

            //Pausar animación del indicador
            indicadorGrabando.clearAnimation();
            indicadorGrabando.setAlpha(1f);
            Toast.makeText(this, "Pausado", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Pausado", Toast.LENGTH_SHORT).show();
        }
    }

    private void resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder.resume();
            isPaused = false;
            // Reinicia el contador desde ahora
            startTime = System.currentTimeMillis();
            handler.post(updateTimer);
            btnPause.setText("⏸ Pausar");

            //Reanudar animación de grabando
            Animation blink = AnimationUtils.loadAnimation(this, R.anim.blink);
            indicadorGrabando.startAnimation(blink);
            Toast.makeText(this, "Reanudado", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (isRecording) {
            recorder.stop();
            recorder.release();
            recorder = null;
            handler.removeCallbacks(updateTimer);

            Toast.makeText(this, "Grabación guardada:\n" + filePath,
                    Toast.LENGTH_LONG).show();

            //Ocultar el indicador visual
            indicadorGrabando.clearAnimation();
            indicadorGrabando.setVisibility(View.GONE);

            resetUI();
        }
    }

    private void resetUI() {
        isRecording = false;
        isPaused = false;
        pausedTime = 0L;
        tvTimer.setText("00:00");

        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        btnStop.setEnabled(false);
    }

    // Resultado de petición de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean ok = false;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) { ok = true; break; }
            }
            if (ok) {
                startRecording();
            } else {
                Toast.makeText(this,
                        "Permisos denegados, no se puede grabar",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}