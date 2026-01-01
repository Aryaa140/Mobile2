package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifikasiKodeActivity extends AppCompatActivity {

    private EditText etKodeVerifikasi;
    private Button btnVerifikasi, btnKirimUlang, btnKembali;
    private TextView tvEmail, tvTimer;

    private String username, email;
    private int kodeAsli;
    private CountDownTimer countDownTimer;
    private boolean canResend = false;
    private long timeRemaining = 5 * 60 * 1000; // 5 menit dalam milidetik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verifikasi_kode);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data from intent
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        email = intent.getStringExtra("EMAIL");
        kodeAsli = intent.getIntExtra("KODE_VERIFIKASI", 0);

        // Initialize views
        etKodeVerifikasi = findViewById(R.id.kodeVerifikasi);
        btnVerifikasi = findViewById(R.id.btnVerifikasi);
        btnKirimUlang = findViewById(R.id.btnKirimUlang);
        btnKembali = findViewById(R.id.btnKembali);
        tvEmail = findViewById(R.id.textViewEmail);
        tvTimer = findViewById(R.id.textViewTimer);

        // Set email text
        tvEmail.setText("Kode dikirim ke: " + email);

        // Start timer
        startTimer();

        btnVerifikasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifikasiKode();
            }
        });

        btnKirimUlang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canResend) {
                    kirimUlangKode();
                } else {
                    Toast.makeText(VerifikasiKodeActivity.this,
                            "Tunggu hingga timer habis untuk mengirim ulang",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerifikasiKodeActivity.this, LupaPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void verifikasiKode() {
        String kodeInput = etKodeVerifikasi.getText().toString().trim();

        if (TextUtils.isEmpty(kodeInput)) {
            etKodeVerifikasi.setError("Kode verifikasi harus diisi");
            etKodeVerifikasi.requestFocus();
            return;
        }

        if (kodeInput.length() != 6) {
            etKodeVerifikasi.setError("Kode harus 6 digit");
            etKodeVerifikasi.requestFocus();
            return;
        }

        try {
            int kode = Integer.parseInt(kodeInput);

            if (kode == kodeAsli) {
                // Kode valid, pindah ke activity reset password
                Toast.makeText(this, "Kode verifikasi valid", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(VerifikasiKodeActivity.this, ResetPasswordActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            } else {
                etKodeVerifikasi.setError("Kode verifikasi salah");
                etKodeVerifikasi.requestFocus();
            }
        } catch (NumberFormatException e) {
            etKodeVerifikasi.setError("Kode harus berupa angka");
            etKodeVerifikasi.requestFocus();
        }
    }

    private void kirimUlangKode() {
        // Generate new code
        kodeAsli = generateKodeVerifikasi();

        // Send new code via email
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.kirimKodeVerifikasi(username, email, kodeAsli);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse emailResponse = response.body();

                    if (emailResponse.isSuccess()) {
                        Toast.makeText(VerifikasiKodeActivity.this,
                                "Kode verifikasi baru telah dikirim",
                                Toast.LENGTH_SHORT).show();

                        // Reset timer
                        resetTimer();
                    } else {
                        Toast.makeText(VerifikasiKodeActivity.this,
                                "Gagal mengirim kode: " + emailResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(VerifikasiKodeActivity.this,
                            "Gagal mengirim kode verifikasi",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(VerifikasiKodeActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private int generateKodeVerifikasi() {
        return (int) ((Math.random() * 900000) + 100000);
    }

    private void startTimer() {
        canResend = false;
        btnKirimUlang.setEnabled(false);
        btnKirimUlang.setTextColor(getResources().getColor(android.R.color.darker_gray));

        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("Kode berlaku: %02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                canResend = true;
                btnKirimUlang.setEnabled(true);
                btnKirimUlang.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                tvTimer.setText("Kode telah kadaluarsa");
                tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }.start();
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeRemaining = 5 * 60 * 1000; // Reset ke 5 menit
        startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}