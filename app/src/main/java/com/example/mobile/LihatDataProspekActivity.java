package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LihatDataProspekActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private ProspekAdapter2 adapter;
    private ArrayList<Prospek2> prospekList;
    private ArrayList<Prospek2> prospekListFull;
    private EditText searchEditText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadDataFromMySQL();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_prospek);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);

        prospekList = new ArrayList<>();
        prospekListFull = new ArrayList<>();

        adapter = new ProspekAdapter2(this, prospekList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load data dari MySQL
        loadDataFromMySQL();

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
                startActivity(new Intent(this, NewBeranda.class));
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

    private void loadDataFromMySQL() {
        String username = sharedPreferences.getString("username", "");
        Log.d("LihatDataProspek", "Loading data for username: " + username);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProspekResponse> call = apiService.getProspekByPenginput(username);

        call.enqueue(new Callback<ProspekResponse>() {
            @Override
            public void onResponse(Call<ProspekResponse> call, Response<ProspekResponse> response) {
                Log.d("LihatDataProspek", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ProspekResponse prospekResponse = response.body();
                    Log.d("LihatDataProspek", "Success: " + prospekResponse.isSuccess());
                    Log.d("LihatDataProspek", "Message: " + prospekResponse.getMessage());

                    if (prospekResponse.isSuccess()) {
                        List<Prospek2> data = prospekResponse.getData();
                        Log.d("LihatDataProspek", "Data count: " + (data != null ? data.size() : "null"));

                        prospekList.clear();
                        prospekListFull.clear();

                        if (data != null) {
                            prospekList.addAll(data);
                            prospekListFull.addAll(data);
                        }

                        adapter.notifyDataSetChanged();

                        if (data == null || data.isEmpty()) {
                            Toast.makeText(LihatDataProspekActivity.this, "Tidak ada data prospek", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LihatDataProspekActivity.this, "Gagal memuat data: " + prospekResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("LihatDataProspek", "Response not successful");
                    Toast.makeText(LihatDataProspekActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProspekResponse> call, Throwable t) {
                Log.e("LihatDataProspek", "Failure: " + t.getMessage(), t);
                Toast.makeText(LihatDataProspekActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterData(String query) {
        ArrayList<Prospek2> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(prospekListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Prospek2 prospek : prospekListFull) {
                if ((prospek.getNamaProspek() != null && prospek.getNamaProspek().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getNamaPenginput() != null && prospek.getNamaPenginput().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getEmail() != null && prospek.getEmail().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getNoHp() != null && prospek.getNoHp().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getTanggalInput() != null && prospek.getTanggalInput().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getStatusNpwp() != null && prospek.getStatusNpwp().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getStatusBpjs() != null && prospek.getStatusBpjs().toLowerCase().contains(lowerCaseQuery))) {
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
        loadDataFromMySQL();
    }
}