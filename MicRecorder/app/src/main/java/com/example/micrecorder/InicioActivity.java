package com.example.micrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class InicioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_inicio);  // ← Aquí usamos el layout con botones
    }

    public void abrirGrabadora(View view) {
        Intent intent = new Intent(this, VoiceRecorderActivity.class);
        startActivity(intent);
    }

    public void abrirListado(View view) {
        Intent intent = new Intent(this, ListadoNotasActivity.class);
        startActivity(intent);
    }
}
