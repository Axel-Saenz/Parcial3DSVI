package com.example.micrecorder;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import java.io.*;
import java.util.*;

public class NotaVozAdapter extends RecyclerView.Adapter<NotaVozAdapter.ViewHolder> {
    private List<NotaVoz> listaNotas;
    private ListadoNotasActivity context;

    public NotaVozAdapter(List<NotaVoz> listaNotas, ListadoNotasActivity context) {
        this.listaNotas = listaNotas;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvFecha, tvDuracion;
        Button btnReproducir, btnEliminar, btnRenombrar;
        SeekBar seekBarProgreso;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreArchivo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvDuracion = itemView.findViewById(R.id.tvDuracion);
            btnReproducir = itemView.findViewById(R.id.btnReproducir);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            seekBarProgreso = itemView.findViewById(R.id.seekBarProgreso);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nota_voz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotaVoz nota = listaNotas.get(position);
        holder.tvNombre.setText(nota.getNombreArchivo());
        holder.tvFecha.setText(nota.getFechaHora());
        holder.tvDuracion.setText("Duración: " + nota.getDuracion());

        holder.seekBarProgreso.setMax(100);
        holder.seekBarProgreso.setProgress(0);
        holder.seekBarProgreso.setEnabled(false);

        holder.btnReproducir.setOnClickListener(v -> {
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(nota.getRuta());
                player.prepare();
                player.start();

                holder.seekBarProgreso.setMax(player.getDuration());
                holder.seekBarProgreso.setEnabled(true);

                new Thread(() -> {
                    while(player.isPlaying()) {
                        int currentPosition = player.getCurrentPosition();
                        holder.seekBarProgreso.post(() -> holder.seekBarProgreso.setProgress(currentPosition));
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    holder.seekBarProgreso.post(() -> {
                        holder.seekBarProgreso.setProgress(0);
                        holder.seekBarProgreso.setEnabled(false);
                    });
                    player.release();
                }).start();

                Toast.makeText(context, "Reproduciendo...", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(context, "Error al reproducir", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            File file = new File(nota.getRuta());
            if (file.exists() && file.delete()) {
                listaNotas.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Nota eliminada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "No se pudo eliminar", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return listaNotas.size();
    }
}
