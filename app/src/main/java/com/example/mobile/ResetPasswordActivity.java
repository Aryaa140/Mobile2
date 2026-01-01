package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etPasswordBaru, etKonfirmasiPassword;
    private Button btnResetPassword;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get username from intent
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        etPasswordBaru = findViewById(R.id.etPasswordBaru);
        etKonfirmasiPassword = findViewById(R.id.etKonfirmasiPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String passwordBaru = etPasswordBaru.getText().toString().trim();
        String konfirmasiPassword = etKonfirmasiPassword.getText().toString().trim();

        if (TextUtils.isEmpty(passwordBaru)) {
            etPasswordBaru.setError("Password baru harus diisi");
            etPasswordBaru.requestFocus();
            return;
        }

        if (passwordBaru.length() < 6) {
            etPasswordBaru.setError("Password minimal 6 karakter");
            etPasswordBaru.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(konfirmasiPassword)) {
            etKonfirmasiPassword.setError("Konfirmasi password harus diisi");
            etKonfirmasiPassword.requestFocus();
            return;
        }

        if (!passwordBaru.equals(konfirmasiPassword)) {
            etKonfirmasiPassword.setError("Password tidak cocok");
            etKonfirmasiPassword.requestFocus();
            return;
        }

        // Panggil endpoint update password
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.updatePassword(username, passwordBaru);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse resetResponse = response.body();

                    if (resetResponse.isSuccess()) {
                        Toast.makeText(ResetPasswordActivity.this,
                                "Password berhasil direset",
                                Toast.LENGTH_SHORT).show();

                        // Kembali ke login
                        Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this,
                                "Gagal reset password: " + resetResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this,
                            "Gagal reset password",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}