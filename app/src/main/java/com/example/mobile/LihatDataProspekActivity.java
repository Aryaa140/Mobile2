package com.example.mobile;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobile.Prospek;
import com.example.mobile.ProspekAdapter;


import java.util.ArrayList;

public class LihatDataProspekActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProspekAdapter adapter;
    private ArrayList<Prospek> prospekList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_prospek);

        recyclerView = findViewById(R.id.recyclerProspek);
        dbHelper = new DatabaseHelper(this);

        // 🔹 Ambil data dari database
        prospekList = new ArrayList<>(dbHelper.getAllProspek());

        // 🔹 Setup adapter
        adapter = new ProspekAdapter(this, prospekList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
