package com.example.mobile;

import android.net.Uri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.UUID;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.content.Context;
import android.util.Log;
public class FirebaseDatabaseHelper {
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Context context;
    private static final String TAG = "FirebaseDatabaseHelper";
    public FirebaseDatabaseHelper(Context context) {
        this.context = context;
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
            }
            databaseReference = FirebaseDatabase.getInstance().getReference("promo");
            storageReference = FirebaseStorage.getInstance().getReference("promo_images");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static class Promo {
        private String id;
        private String namaGambar;
        private String referensiProyek;
        private String namaPenginput;
        private String imageUrl;
        private long timestamp;

        public Promo() {}

        public Promo(String namaGambar, String referensiProyek, String namaPenginput, String imageUrl) {
            this.id = UUID.randomUUID().toString();
            this.namaGambar = namaGambar;
            this.referensiProyek = referensiProyek;
            this.namaPenginput = namaPenginput;
            this.imageUrl = imageUrl;
            this.timestamp = System.currentTimeMillis();
        }

        // Getter dan Setter
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNamaGambar() { return namaGambar; }
        public void setNamaGambar(String namaGambar) { this.namaGambar = namaGambar; }
        public String getReferensiProyek() { return referensiProyek; }
        public void setReferensiProyek(String referensiProyek) { this.referensiProyek = referensiProyek; }
        public String getNamaPenginput() { return namaPenginput; }
        public void setNamaPenginput(String namaPenginput) { this.namaPenginput = namaPenginput; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public void savePromoWithImage(Uri imageUri, String namaGambar, String referensiProyek,
                                   String namaPenginput, OnSuccessListener<String> onSuccessListener,
                                   OnFailureListener onFailureListener) {

        // Validasi Firebase initialization
        if (databaseReference == null || storageReference == null) {
            onFailureListener.onFailure(new Exception("Firebase not initialized"));
            return;
        }

        if (imageUri == null) {
            onFailureListener.onFailure(new Exception("Image URI is null"));
            return;
        }

        // Generate unique filename
        String imageFileName = "promo_" + System.currentTimeMillis() + ".jpg";
        StorageReference fileReference = storageReference.child(imageFileName);

        // Upload image to Firebase Storage
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Promo promo = new Promo(namaGambar, referensiProyek, namaPenginput, imageUrl);
                        savePromoData(promo, onSuccessListener, onFailureListener);
                    }).addOnFailureListener(onFailureListener);
                })
                .addOnFailureListener(onFailureListener);
    }

    private void savePromoData(Promo promo, OnSuccessListener<String> onSuccess,
                               OnFailureListener onFailure) {
        String promoId = databaseReference.push().getKey();
        if (promoId != null) {
            promo.setId(promoId);
            databaseReference.child(promoId)
                    .setValue(promo)
                    .addOnSuccessListener(aVoid -> onSuccess.onSuccess(promoId))
                    .addOnFailureListener(onFailure);
        }
    }

    public DatabaseReference getPromoReference() {
        return databaseReference;
    }
}