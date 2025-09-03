package com.example.mobile;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public class BookingActivity extends AppCompatActivity {

    EditText editDateTime;
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking);

        editDateTime = findViewById(R.id.editDateTime);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(BookingActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });


        editDateTime.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    BookingActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                BookingActivity.this,
                                (timeView, hourOfDay, minuteOfHour) -> {
                                    String dateTime = dayOfMonth + "/" + (month1 + 1) + "/" + year1
                                            + " " + String.format("%02d:%02d", hourOfDay, minuteOfHour);
                                    editDateTime.setText(dateTime);
                                }, hour, minute, true);
                        timePickerDialog.show();
                    }, year, month, day);
            datePickerDialog.show();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
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
}