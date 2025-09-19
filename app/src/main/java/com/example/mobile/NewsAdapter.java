package com.example.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private Context context;
    private List<NewsItem> newsItems;
    private SimpleDateFormat dateFormat;
    public interface OnItemRemoveListener {
        void onItemRemoved(int position, NewsItem removedItem);
    }

    public NewsAdapter(Context context, List<NewsItem> newsItems) {
        this.context = context;
        this.newsItems = newsItems;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));

    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = newsItems.get(position);

        holder.tvNewsTitle.setText(item.getTitle());
        holder.tvPenginput.setText("Oleh: " + item.getPenginput());
        holder.tvStatus.setText("Status: " + item.getStatus());
        holder.tvNewsDate.setText(dateFormat.format(item.getTimestamp()));

        // Load image using Picasso
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getImageUrl())
                    .fit() // Otomatis menyesuaikan ukuran
                    .centerCrop() // Crop agar pas dengan ImageView
                    .into(holder.imgNews);
        } else {
            // Gunakan background color saja tanpa drawable
            holder.imgNews.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            holder.imgNews.setImageDrawable(null);
            holder.imgNews.setScaleType(ImageView.ScaleType.CENTER);

            // Tambahkan text placeholder jika perlu
            holder.imgNews.setContentDescription("Gambar tidak tersedia");
        }
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    public void removeItem(int position) {
        NewsItem removedItem = newsItems.get(position);
        newsItems.remove(position);
        notifyItemRemoved(position);


    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNews;
        TextView tvNewsTitle, tvPenginput, tvStatus, tvNewsDate;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNews = itemView.findViewById(R.id.imgNews);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNewsDate = itemView.findViewById(R.id.tvNewsDate);
        }
    }
}