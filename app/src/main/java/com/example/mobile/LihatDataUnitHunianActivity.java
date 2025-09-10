package com.example.mobile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class LihatDataUnitHunianActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UnitHunianAdapter adapter;
    private DatabaseHelper databaseHelper;
    private EditText searchEditText;
    private List<DatabaseHelper.UnitHunian> unitHunianList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_unit_hunian);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi views
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);

        // Setup toolbar
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Setup recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load data unit hunian
        loadUnitHunianData();

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUnitHunianData() {
        unitHunianList = databaseHelper.getAllUnitHunian();
        if (unitHunianList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data unit hunian", Toast.LENGTH_SHORT).show();
        }

        adapter = new UnitHunianAdapter(this, unitHunianList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika activity diresume (setelah edit/delete)
        loadUnitHunianData();
    }
}