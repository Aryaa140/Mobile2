package com.example.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FasilitasHunianAdapter extends RecyclerView.Adapter<FasilitasHunianAdapter.ViewHolder> {
    private List<FasilitasHunianItem> fasilitasList;
    private OnFasilitasHapusListener listener;

    public interface OnFasilitasHapusListener {
        void onHapusFasilitas(int position);
    }

    public FasilitasHunianAdapter(List<FasilitasHunianItem> fasilitasList, OnFasilitasHapusListener listener) {
        this.fasilitasList = fasilitasList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fasilitas_hunian, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FasilitasHunianItem item = fasilitasList.get(position);

        holder.textNamaFasilitas.setText(item.getNamaFasilitas());
        holder.textJumlahFasilitas.setText("Jumlah: " + item.getJumlah());

        holder.btnHapusFasilitas.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHapusFasilitas(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fasilitasList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textNamaFasilitas;
        public TextView textJumlahFasilitas;
        public ImageButton btnHapusFasilitas;

        public ViewHolder(View view) {
            super(view);
            textNamaFasilitas = view.findViewById(R.id.textNamaFasilitas);
            textJumlahFasilitas = view.findViewById(R.id.textJumlahFasilitas);
            btnHapusFasilitas = view.findViewById(R.id.btnHapusFasilitas);
        }
    }
}