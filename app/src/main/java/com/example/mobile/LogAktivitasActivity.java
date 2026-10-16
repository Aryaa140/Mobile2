package com.example.mobile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogAktivitasActivity extends BaseActivity {

    private RecyclerView recyclerLog;
    private LogAdapter adapter;
    private List<LogEntry> logList = new ArrayList<>();
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_aktivitas);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerLog = findViewById(R.id.recyclerLog);
        searchEditText = findViewById(R.id.searchEditText);

        adapter = new LogAdapter(this, logList);
        recyclerLog.setLayoutManager(new LinearLayoutManager(this));
        recyclerLog.setAdapter(adapter);

        loadLog();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadLog() {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Call<LogListResponse> call = api.getLog("get_log", null, null, null, 300);

        call.enqueue(new Callback<LogListResponse>() {
            @Override
            public void onResponse(Call<LogListResponse> call, Response<LogListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Pakai updateData() agar originalList di adapter sinkron
                    List<LogEntry> data = response.body().getData();
                    if (data != null) {
                        adapter.updateData(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<LogListResponse> call, Throwable t) {
                Toast.makeText(LogAktivitasActivity.this,
                        "Gagal load log: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}