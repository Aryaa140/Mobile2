package com.example.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private Context context;
    private List<NewsItem> newsItems;
    private SimpleDateFormat dateFormat;

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

        // PERBAIKAN: Handle image loading dengan lebih robust
        loadNewsImage(item.getImageUrl(), holder.imgNews);
    }

    private void loadNewsImage(String imageData, ImageView imageView) {
        // Reset image dulu
        setDefaultImage(imageView);

        if (imageData == null || imageData.trim().isEmpty()) {
            Log.d("NewsAdapter", "Image data is null or empty");
            setDefaultImage(imageView);
            return;
        }

        try {
            String cleanBase64 = imageData.trim();

            // Handle case untuk item yang dihapus (masih punya gambar)
            if (cleanBase64.startsWith("data:image") || cleanBase64.contains("base64")) {
                // Extract base64 dari data URL jika diperlukan
                if (cleanBase64.contains("base64,")) {
                    cleanBase64 = cleanBase64.split("base64,")[1];
                }
            }

            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

            if (decodedBytes != null && decodedBytes.length > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;

                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Log.d("NewsAdapter", "✅ Image loaded successfully");
                    return;
                }
            }

            setDefaultImage(imageView);

        } catch (Exception e) {
            Log.e("NewsAdapter", "Error loading image: " + e.getMessage());
            setDefaultImage(imageView);
        }
    }

    private void setDefaultImage(ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_placeholder); // Pastikan ada placeholder di drawable
        imageView.setScaleType(ImageView.ScaleType.CENTER);
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < newsItems.size()) {
            newsItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    // PERBAIKAN: Method untuk update data
    public void updateData(List<NewsItem> newItems) {
        newsItems.clear();
        newsItems.addAll(newItems);
        notifyDataSetChanged();
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