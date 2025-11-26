package com.example.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private Context context;
    private List<NewsItem> newsItems;

    public NewsAdapter(Context context, List<NewsItem> newsItems) {
        this.context = context;
        this.newsItems = newsItems;
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

        String displayTitle = item.getTitle();
        if (displayTitle == null || displayTitle.isEmpty()) {
            displayTitle = "Promo";
        }

        holder.tvNewsTitle.setText(displayTitle);
        holder.tvPenginput.setText("Oleh: " + item.getPenginput());
        holder.tvStatus.setText("Status: " + item.getStatus());

        // Tampilkan timestamp relatif
        holder.tvTimestamp.setText(item.getFormattedTimestamp());

        // ✅ PERBAIKAN: Tampilkan kadaluwarsa dengan format yang benar
        String formattedKadaluwarsa = item.getFormattedKadaluwarsa();
        if (formattedKadaluwarsa != null && !formattedKadaluwarsa.equals("Tidak ada kadaluwarsa")) {
            holder.tvKadaluwarsa.setText("Kadaluwarsa: " + formattedKadaluwarsa);
            holder.tvKadaluwarsa.setVisibility(View.VISIBLE);

            // ✅ TAMBAHKAN WARNA BERDASARKAN STATUS
            if ("Kadaluwarsa".equals(item.getStatus())) {
                holder.tvKadaluwarsa.setTextColor(ContextCompat.getColor(context, R.color.red));
            } else {
                holder.tvKadaluwarsa.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        } else {
            holder.tvKadaluwarsa.setVisibility(View.GONE);
        }

        // Set background color berdasarkan status
        setCardBackgroundBasedOnStatus(holder.cardView, item.getStatus());

        // Load image
        loadNewsImageWithEnhancedValidation(item.getImageUrl(), holder.imgNews, item.getTitle(), position);
    }


    // PERBAIKAN: Method load image yang lebih robust
    private void loadNewsImageWithEnhancedValidation(String imageData, ImageView imageView, String title, int position) {
        Log.d("NewsAdapter", "🖼️ Position " + position + " - Loading image for: " + title);

        // Reset image dulu
        setDefaultImage(imageView);

        if (isValidImageForNewsDisplay(imageData)) {
            String cleanBase64 = imageData.trim();
            Log.d("NewsAdapter", "📷 Loading valid image, length: " + cleanBase64.length() + " for: " + title);

            // Load image di background thread dengan error handling
            new Thread(() -> {
                try {
                    byte[] decodedBytes;
                    try {
                        decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                        Log.d("NewsAdapter", "✅ Base64 decoded successfully: " + decodedBytes.length + " bytes for: " + title);
                    } catch (IllegalArgumentException e) {
                        Log.e("NewsAdapter", "❌ Base64 decoding error for " + title + ": " + e.getMessage());
                        new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                        return;
                    }

                    if (decodedBytes == null || decodedBytes.length == 0) {
                        Log.e("NewsAdapter", "❌ Decoded bytes are empty for: " + title);
                        new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                        return;
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                    Log.d("NewsAdapter", "📐 Image dimensions for " + title + ": " + options.outWidth + "x" + options.outHeight);

                    options.inJustDecodeBounds = false;
                    options.inSampleSize = calculateOptimalSampleSize(options, 400, 400);
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inPurgeable = true;
                    options.inInputShareable = true;

                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                    if (bitmap != null) {
                        Log.d("NewsAdapter", "✅ Successfully decoded bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight() + " for: " + title);

                        final Bitmap finalBitmap = bitmap;
                        new Handler(Looper.getMainLooper()).post(() -> {
                            try {
                                imageView.setImageBitmap(finalBitmap);
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                Log.d("NewsAdapter", "🎉 Image loaded successfully for: " + title);
                            } catch (Exception e) {
                                Log.e("NewsAdapter", "❌ Error setting bitmap: " + e.getMessage());
                                setDefaultImage(imageView);
                            }
                        });
                    } else {
                        Log.e("NewsAdapter", "❌ Failed to decode bitmap for: " + title);
                        new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                    }

                } catch (OutOfMemoryError e) {
                    Log.e("NewsAdapter", "💥 Out of memory for " + title + ": " + e.getMessage());
                    new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                } catch (Exception e) {
                    Log.e("NewsAdapter", "❌ Error loading image for " + title + ": " + e.getMessage());
                    new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                }
            }).start();
        } else {
            Log.w("NewsAdapter", "📷 No valid image data for: " + title);
            setDefaultImage(imageView);
        }
    }

    private boolean isValidImageForNewsDisplay(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            Log.d("NewsAdapter", "❌ Image data is null or empty");
            return false;
        }

        String cleanData = imageData.trim();

        boolean isValid = cleanData.length() >= 100 &&
                !cleanData.equals("null") &&
                !cleanData.equals("NULL") &&
                !cleanData.endsWith("..") &&
                !cleanData.endsWith("...");

        Log.d("NewsAdapter", "🖼️ Image validation - Length: " + cleanData.length() +
                ", Is 'null': " + cleanData.equals("null") +
                ", Valid: " + isValid);

        return isValid;
    }

    private int calculateOptimalSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d("NewsAdapter", "📏 Calculated sample size: " + inSampleSize + " for " + width + "x" + height);
        return inSampleSize;
    }

    private void setDefaultImage(ImageView imageView) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                imageView.setImageResource(R.drawable.ic_placeholder);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setVisibility(View.VISIBLE);
                Log.d("NewsAdapter", "🔄 Set default placeholder image");
            } catch (Exception e) {
                Log.e("NewsAdapter", "❌ Error setting default image: " + e.getMessage());
            }
        });
    }

    // DI NewsAdapter.java - Perbaiki method setCardBackgroundBasedOnStatus
    private void setCardBackgroundBasedOnStatus(CardView cardView, String status) {
        try {
            int backgroundColor;

            switch (status) {
                case "Ditambahkan":
                    backgroundColor = 0xFFE8F5E8; // Light green for added
                    break;
                case "Diubah":
                    backgroundColor = 0xFFE3F2FD; // Light blue for updated
                    break;
                case "Dihapus":
                    backgroundColor = 0xFFFFEBEE; // Light red for deleted
                    break;
                case "Kadaluwarsa":
                    backgroundColor = 0xFFFFF3CD; // Light yellow for expired
                    break;
                default:
                    backgroundColor = 0xFFF5F5F5; // Light gray for unknown
            }

            cardView.setCardBackgroundColor(backgroundColor);
        } catch (Exception e) {
            Log.e("NewsAdapter", "Error setting card background: " + e.getMessage());
            cardView.setCardBackgroundColor(0xFFFFFFFF);
        }
    }

    @Override
    public int getItemCount() {
        return newsItems != null ? newsItems.size() : 0;
    }

    public void updateData(List<NewsItem> newItems) {
        this.newsItems = newItems;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < newsItems.size()) {
            newsItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNews;
        TextView tvNewsTitle, tvPenginput, tvStatus, tvTimestamp, tvKadaluwarsa;
        CardView cardView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNews = itemView.findViewById(R.id.imgNews);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvKadaluwarsa = itemView.findViewById(R.id.tvKadaluwarsa);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}