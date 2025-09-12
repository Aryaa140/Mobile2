package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import android.util.Log;
public class LihatDataProspekActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private ProspekAdapter adapter;
    private ArrayList<Prospek> prospekList;
    private ArrayList<Prospek> prospekListFull; // Untuk pencarian
    private DatabaseHelper dbHelper;
    private EditText searchEditText;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Refresh data jika ada perubahan dari edit activity
            refreshData();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_prospek);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);

        dbHelper = new DatabaseHelper(this);

        // Ambil data dari database
        prospekListFull = new ArrayList<>(dbHelper.getAllProspek());
        prospekList = new ArrayList<>(prospekListFull);

        adapter = new ProspekAdapter(this, prospekList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup pencarian
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataProspekActivity.this, LihatDataActivity.class);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, BerandaActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void filterData(String query) {
        ArrayList<Prospek> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(prospekListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Prospek prospek : prospekListFull) {
                if (prospek.getNama().toLowerCase().contains(lowerCaseQuery) ||
                        prospek.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                        prospek.getNoHp().toLowerCase().contains(lowerCaseQuery) ||
                        prospek.getPenginput().toLowerCase().contains(lowerCaseQuery) ||
                        (prospek.getStatusNpwp() != null && prospek.getStatusNpwp().toLowerCase().contains(lowerCaseQuery)) || // TAMBAHAN: Cari di status NPWP
                        (prospek.getStatusBpjs() != null && prospek.getStatusBpjs().toLowerCase().contains(lowerCaseQuery))) { // TAMBAHAN: Cari di status BPJS
                    filteredList.add(prospek);
                }
            }
        }

        prospekList.clear();
        prospekList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika activity di-resume
        refreshData();
    }

    private void refreshData() {
        prospekListFull.clear();
        ArrayList<Prospek> newData = new ArrayList<>(dbHelper.getAllProspek());

        // Debug: Log jumlah data yang diambil
        Log.d("LihatDataProspek", "Jumlah data dari database: " + newData.size());

        prospekListFull.addAll(newData);

        // Debug: Log isi data
        for (Prospek p : prospekListFull) {
            Log.d("LihatDataProspek", "Prospek: " + p.getNama() + ", NPWP: " + p.getStatusNpwp() + ", BPJS: " + p.getStatusBpjs());
        }

        filterData(searchEditText.getText().toString());

        // Debug: Log jumlah data setelah filter
        Log.d("LihatDataProspek", "Jumlah data setelah filter: " + prospekList.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (adapter != null) {
            adapter.close();
        }
    }
}