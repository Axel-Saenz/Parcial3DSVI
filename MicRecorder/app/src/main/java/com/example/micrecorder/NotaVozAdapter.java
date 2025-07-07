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
            btnRenombrar = itemView.findViewById(R.id.btnRenombrar);
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

        holder.btnRenombrar.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_renombrar, null);
            EditText input = dialogView.findViewById(R.id.editTextNuevoNombre);

            int pos = holder.getAdapterPosition();
            NotaVoz notaSeleccionada = listaNotas.get(pos);

            new AlertDialog.Builder(context)
                    .setTitle("Renombrar Nota")
                    .setView(dialogView)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        String nuevoNombre = input.getText().toString().trim();

                        if (!nuevoNombre.isEmpty()) {
                            File archivoActual = new File(notaSeleccionada.getRuta());
                            File nuevoArchivo = new File(archivoActual.getParent(), nuevoNombre + ".3gp");

                            if (nuevoArchivo.exists()) {
                                Toast.makeText(context, "Ya existe un archivo con ese nombre", Toast.LENGTH_SHORT).show();
                            } else {
                                if (archivoActual.renameTo(nuevoArchivo)) {
                                    NotaVoz nuevaNota = new NotaVoz(
                                            nuevoArchivo.getName(),
                                            nuevoArchivo.getAbsolutePath(),
                                            notaSeleccionada.getFechaHora(),
                                            notaSeleccionada.getDuracion()
                                    );
                                    listaNotas.set(pos, nuevaNota);
                                    notifyItemChanged(pos);
                                    Toast.makeText(context, "Nota renombrada", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "No se pudo renombrar", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }


    @Override
    public int getItemCount() {
        return listaNotas.size();
    }
}
