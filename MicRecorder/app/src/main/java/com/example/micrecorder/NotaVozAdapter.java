package com.example.micrecorder;

import android.media.MediaPlayer;
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
        Button btnReproducir, btnEliminar;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreArchivo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvDuracion = itemView.findViewById(R.id.tvDuracion);
            btnReproducir = itemView.findViewById(R.id.btnReproducir);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
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

        holder.btnReproducir.setOnClickListener(v -> {
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(nota.getRuta());
                player.prepare();
                player.start();
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
