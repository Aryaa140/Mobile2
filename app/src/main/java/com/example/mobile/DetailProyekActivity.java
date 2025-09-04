package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetailProyekActivity extends AppCompatActivity {

    ImageView imgProyek;
    TextView txtNama, txtLokasi, txtDeskripsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_proyek);

        imgProyek = findViewById(R.id.imgProyek);
        txtNama = findViewById(R.id.txtNama);
        txtLokasi = findViewById(R.id.txtLokasi);
        txtDeskripsi = findViewById(R.id.txtDeskripsi);

        Intent intent = getIntent();
        String nama = intent.getStringExtra("nama");
        String lokasi = intent.getStringExtra("lokasi");
        int gambar = intent.getIntExtra("gambar", 0);

        txtNama.setText(nama);
        txtLokasi.setText(lokasi);
        imgProyek.setImageResource(gambar);

        if(nama.equals("The Quality Riverside")) {
            txtDeskripsi.setText("Proyek ini berlokasi strategis dekat jalan raya, ...");
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}