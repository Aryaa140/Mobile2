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
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
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

        // ✅ SET DATA DENGAN FORMAT YANG BENAR
        holder.tvNewsTitle.setText(item.getTitle());
        holder.tvPenginput.setText("Oleh: " + item.getPenginput());
        holder.tvStatus.setText("Status: " + item.getStatus());
        holder.tvNewsDate.setText(item.getTime());

        // ✅ SET BACKGROUND COLOR BERDASARKAN STATUS
        setCardBackgroundBasedOnStatus(holder.cardView, item.getStatus());

        // ✅ FIX: IMAGE LOADING YANG LEBIH ROBUST
        loadNewsImageWithFallback(item.getImageUrl(), holder.imgNews, item.getTitle(), position);
    }

    // ✅ PERBAIKAN METHOD: loadNewsImageWithFallback di NewsAdapter
    private void loadNewsImageWithFallback(String imageData, ImageView imageView, String title, int position) {
        Log.d("NewsAdapter", "🖼️ Position " + position + " - Loading image for: " + title);

        // ✅ PERBAIKAN: VALIDASI YANG LEBIH BAIK
        if (isValidImageForDisplay(imageData)) {
            String cleanBase64 = imageData.trim();
            Log.d("NewsAdapter", "📷 Loading valid image, length: " + cleanBase64.length());
            loadNewsImageOptimized(cleanBase64, imageView, title, position);
        } else {
            Log.w("NewsAdapter", "📷 No valid image data for: " + title +
                    " | Data: " + (imageData != null ? "Length=" + imageData.length() + ", First50=" +
                    (imageData.length() > 50 ? imageData.substring(0, 50) : imageData) : "NULL"));
            setDefaultImage(imageView);
        }
    }

    // ✅ METHOD BARU: Load image dengan optimasi maksimal
    private void loadNewsImageOptimized(String imageData, ImageView imageView, String title, int position) {
        new Thread(() -> {
            try {
                Log.d("NewsAdapter", "🎬 Starting optimized image decode for: " + title);

                byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);

                if (decodedBytes == null || decodedBytes.length == 0) {
                    throw new IllegalArgumentException("Decoded bytes are empty");
                }

                Log.d("NewsAdapter", "📊 Decoded bytes length: " + decodedBytes.length);

                // ✅ OPTIMISASI MEMORI MAXIMAL
                BitmapFactory.Options options = new BitmapFactory.Options();

                // First, get image dimensions only
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                Log.d("NewsAdapter", "📐 Image dimensions: " + options.outWidth + "x" + options.outHeight);
                Log.d("NewsAdapter", "📐 Image MIME type: " + options.outMimeType);

                // Calculate sample size - lebih agresif untuk NewsActivity
                options.inSampleSize = calculateOptimalSampleSize(options, 300, 300);
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565; // Use less memory (16-bit)
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inDither = true; // Improve quality for reduced color depth

                Log.d("NewsAdapter", "🔧 Using sample size: " + options.inSampleSize);

                // Decode with optimized options
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                if (bitmap != null) {
                    Log.d("NewsAdapter", "✅ Successfully decoded bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                    // ✅ COMPRESS LEBIH LANJUT UNTUK MENGURANGI MEMORY
                    Bitmap finalBitmap = resizeBitmapIfNeeded(bitmap, 300, 300);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        imageView.setImageBitmap(finalBitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Log.d("NewsAdapter", "🎉 Image loaded successfully for: " + title);

                        // Clean up original bitmap jika berbeda
                        if (finalBitmap != bitmap) {
                            bitmap.recycle();
                        }
                    });
                } else {
                    Log.e("NewsAdapter", "❌ Failed to decode bitmap for: " + title);
                    new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                }

            } catch (OutOfMemoryError e) {
                Log.e("NewsAdapter", "💥 Out of memory for " + title + ": " + e.getMessage());
                // Try with even lower quality
                loadImageWithReducedQuality(imageData, imageView, title);
            } catch (Exception e) {
                Log.e("NewsAdapter", "❌ Error loading image for " + title + ": " + e.getMessage());
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
            }
        }).start();
    }

    // ✅ METHOD BARU: Hitung sample size yang lebih optimal
    private int calculateOptimalSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }

            // Untuk NewsActivity, kita bisa lebih agresif dalam mengurangi size
            if (inSampleSize == 1 && (height > reqHeight * 2 || width > reqWidth * 2)) {
                inSampleSize = 2;
            }
        }

        Log.d("NewsAdapter", "📏 Calculated optimal sample size: " + inSampleSize + " for " + width + "x" + height);
        return inSampleSize;
    }

    // ✅ METHOD BARU: Resize bitmap jika masih terlalu besar
    private Bitmap resizeBitmapIfNeeded(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        Log.d("NewsAdapter", "🔄 Resizing bitmap from " + width + "x" + height + " to " + newWidth + "x" + newHeight);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    // ✅ PERBAIKAN: Validasi gambar untuk display
    private boolean isValidImageForDisplay(String imageData) {
        if (imageData == null || imageData.isEmpty() || imageData.equals("null")) {
            return false;
        }

        String cleanData = imageData.trim();

        // ✅ KRITERIA VALIDASI YANG LEBIH TEPAT
        boolean isValid = cleanData.length() > 500 && // Minimum 500 chars
                !cleanData.endsWith("..") &&
                !cleanData.endsWith("...") &&
                cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$");

        if (!isValid) {
            Log.w("NewsAdapter", "❌ Invalid image data: " +
                    "length=" + cleanData.length() +
                    ", endsWithDot=" + cleanData.endsWith("..") +
                    ", validBase64=" + cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$"));
        }

        return isValid;
    }

    // ✅ PERBAIKAN: VALIDASI BASE64 YANG LEBIH AKURAT
    private boolean isValidBase64(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            return false;
        }

        try {
            // Cek panjang minimum untuk base64 gambar yang valid
            if (base64.length() < 100) {
                Log.w("NewsAdapter", "Base64 too short: " + base64.length());
                return false;
            }

            // Cek karakter base64 yang valid
            if (!base64.matches("^[a-zA-Z0-9+/]*={0,2}$")) {
                Log.w("NewsAdapter", "Base64 contains invalid characters");
                return false;
            }

            // Test decode
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            if (decoded == null || decoded.length == 0) {
                Log.w("NewsAdapter", "Failed to decode base64");
                return false;
            }

            // Cek signature file image
            if (decoded.length >= 4) {
                // Cek JPEG signature
                if ((decoded[0] & 0xFF) == 0xFF && (decoded[1] & 0xFF) == 0xD8) {
                    Log.d("NewsAdapter", "✅ Valid JPEG image");
                    return true;
                }
                // Cek PNG signature
                if ((decoded[0] & 0xFF) == 0x89 && decoded[1] == 0x50 && decoded[2] == 0x4E && decoded[3] == 0x47) {
                    Log.d("NewsAdapter", "✅ Valid PNG image");
                    return true;
                }
            }

            Log.w("NewsAdapter", "No valid image signature found");
            return true; // Tetap coba decode meski tanpa signature yang dikenali

        } catch (IllegalArgumentException e) {
            Log.e("NewsAdapter", "Base64 decoding error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e("NewsAdapter", "Base64 validation error: " + e.getMessage());
            return false;
        }
    }

    // ✅ PERBAIKAN: LOAD IMAGE DENGAN ERROR HANDLING YANG LEBIH BAIK
    private void loadNewsImage(String imageData, ImageView imageView, String title, int position) {
        new Thread(() -> {
            try {
                Log.d("NewsAdapter", "🎬 Starting image decode for: " + title);

                byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);

                if (decodedBytes == null || decodedBytes.length == 0) {
                    throw new IllegalArgumentException("Decoded bytes are empty");
                }

                Log.d("NewsAdapter", "📊 Decoded bytes length: " + decodedBytes.length);

                // ✅ FIX: OPTIMIZE MEMORY USAGE DENGAN OPTIONS
                BitmapFactory.Options options = new BitmapFactory.Options();

                // First, get image dimensions only
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                Log.d("NewsAdapter", "📐 Image dimensions: " + options.outWidth + "x" + options.outHeight);
                Log.d("NewsAdapter", "📐 Image MIME type: " + options.outMimeType);

                // Calculate sample size
                options.inSampleSize = calculateInSampleSize(options, 400, 400);
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565; // Use less memory
                options.inPurgeable = true;
                options.inInputShareable = true;

                Log.d("NewsAdapter", "🔧 Using sample size: " + options.inSampleSize);

                // Decode with optimized options
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                if (bitmap != null) {
                    Log.d("NewsAdapter", "✅ Successfully decoded bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                    new Handler(Looper.getMainLooper()).post(() -> {
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Log.d("NewsAdapter", "🎉 Image loaded successfully for: " + title);
                    });
                } else {
                    Log.e("NewsAdapter", "❌ Failed to decode bitmap for: " + title);
                    new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                }

            } catch (OutOfMemoryError e) {
                Log.e("NewsAdapter", "💥 Out of memory for " + title + ": " + e.getMessage());
                // Try with even lower quality
                loadImageWithReducedQuality(imageData, imageView, title);
            } catch (Exception e) {
                Log.e("NewsAdapter", "❌ Error loading image for " + title + ": " + e.getMessage());
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
            }
        }).start();
    }

    // ✅ METHOD BARU: LOAD IMAGE DENGAN QUALITY YANG DIKURANGI (UNTUK HANDLE OUT OF MEMORY)
    private void loadImageWithReducedQuality(String imageData, ImageView imageView, String title) {
        new Thread(() -> {
            try {
                String cleanBase64 = imageData.trim();
                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

                if (decodedBytes == null || decodedBytes.length == 0) {
                    throw new IllegalArgumentException("Decoded bytes are empty");
                }

                // ✅ OPTIMISASI MAXIMAL UNTUK HINDARI OUT OF MEMORY
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                // Sample size yang lebih besar untuk reduce memory
                options.inSampleSize = calculateInSampleSize(options, 200, 200); // Resize ke 200x200
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565; // Menggunakan 16-bit instead of 32-bit
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inDither = true; // Enable dithering untuk kualitas warna yang better

                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                if (bitmap != null) {
                    // ✅ COMPRESS LEBIH LANJUT JIKA PERLU
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream); // Quality 60%
                    byte[] compressedBytes = outputStream.toByteArray();

                    Bitmap compressedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.length);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (compressedBitmap != null) {
                            imageView.setImageBitmap(compressedBitmap);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            Log.d("NewsAdapter", "✅ Image loaded with reduced quality for: " + title);
                        } else {
                            setDefaultImage(imageView);
                        }
                    });

                    // Clean up
                    outputStream.close();
                } else {
                    throw new RuntimeException("Failed to decode bitmap even with reduced quality");
                }

            } catch (OutOfMemoryError e) {
                Log.e("NewsAdapter", "❌ Still out of memory for " + title + ", using placeholder");
                new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
            } catch (Exception e) {
                Log.e("NewsAdapter", "❌ Error in reduced quality loading for " + title + ": " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
            }
        }).start();
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

    // ✅ PERBAIKAN: SET BACKGROUND BERDASARKAN STATUS
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
                default:
                    backgroundColor = 0xFFF5F5F5; // Light gray for unknown
            }

            cardView.setCardBackgroundColor(backgroundColor);
        } catch (Exception e) {
            Log.e("NewsAdapter", "Error setting card background: " + e.getMessage());
            cardView.setCardBackgroundColor(0xFFFFFFFF);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d("NewsAdapter", "📏 Calculated inSampleSize: " + inSampleSize + " for " + width + "x" + height);
        return inSampleSize;
    }

    @Override
    public int getItemCount() {
        return newsItems != null ? newsItems.size() : 0;
    }

    // ✅ METHOD UNTUK UPDATE DATA
    public void updateData(List<NewsItem> newItems) {
        this.newsItems = newItems;
        notifyDataSetChanged();
    }

    // ✅ METHOD UNTUK HAPUS ITEM
    public void removeItem(int position) {
        if (position >= 0 && position < newsItems.size()) {
            newsItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNews;
        TextView tvNewsTitle, tvPenginput, tvStatus, tvNewsDate;
        CardView cardView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNews = itemView.findViewById(R.id.imgNews);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNewsDate = itemView.findViewById(R.id.tvNewsDate);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}