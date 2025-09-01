package com.example.mobile;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "login.db";
    private static final int DATABASE_VERSION = 2; // Tingkatkan versi database

    // Tabel pengguna
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_DIVISION = "division";
    public static final String COLUMN_NIP = "nip";
    public static final String COLUMN_PASSWORD = "password";

    // Query membuat tabel
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE,"
            + COLUMN_DIVISION + " TEXT,"
            + COLUMN_NIP + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);

        // Menambahkan pengguna contoh - HANYA 2 DIVISI
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, "admin");
        values.put(COLUMN_DIVISION, "Marketing");
        values.put(COLUMN_NIP, "1234567890");
        values.put(COLUMN_PASSWORD, "password123");
        db.insert(TABLE_USERS, null, values);

        // User contoh untuk Electronic Data Processing
        ContentValues values2 = new ContentValues();
        values2.put(COLUMN_USERNAME, "edp");
        values2.put(COLUMN_DIVISION, "Electronic Data Procesing");
        values2.put(COLUMN_NIP, "0987654321");
        values2.put(COLUMN_PASSWORD, "edppass");
        db.insert(TABLE_USERS, null, values2);

        Log.d("DatabaseHelper", "Database created with sample users");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Hapus tabel lama jika ada
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // Buat tabel baru
        onCreate(db);
        Log.d("DatabaseHelper", "Database upgraded from version " + oldVersion + " to " + newVersion);
    }

    // Method untuk memeriksa kredensial pengguna
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        Log.d("DatabaseHelper", "checkUser: " + username + " result: " + (count > 0));
        return count > 0;
    }

    // Method untuk memeriksa apakah username sudah ada
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        Log.d("DatabaseHelper", "checkUsername: " + username + " exists: " + (count > 0));
        return count > 0;
    }

    // Method untuk memeriksa apakah NIP sudah ada
    public boolean checkNip(String nip) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_NIP + " = ?";
        String[] selectionArgs = {nip};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        Log.d("DatabaseHelper", "checkNip: " + nip + " exists: " + (count > 0));
        return count > 0;
    }

    // Method untuk mendapatkan semua divisi yang tersedia (hardcoded)
    public String[] getAllDivisions() {
        return new String[]{"Marketing", "Electronic Data Procesing"};
    }

    // Method untuk menambahkan pengguna baru
    public boolean addUser(String username, String division, String nip, String password) {
        // Validasi divisi
        if (!division.equals("Marketing") && !division.equals("Electronic Data Procesing")) {
            Log.e("DatabaseHelper", "Division not allowed: " + division);
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_DIVISION, division);
        values.put(COLUMN_NIP, nip);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        Log.d("DatabaseHelper", "addUser: " + username + " result: " + (result != -1));
        return result != -1;
    }

    // Method untuk mendapatkan data user berdasarkan username
    public User getUserData(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID, COLUMN_USERNAME, COLUMN_DIVISION, COLUMN_NIP};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            user.setDivision(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIVISION)));
            user.setNip(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIP)));
            Log.d("DatabaseHelper", "getUserData: Found user " + username);
        } else {
            Log.d("DatabaseHelper", "getUserData: User " + username + " not found");
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return user;
    }

    // Kelas model untuk data user
    public static class User {
        private int id;
        private String username;
        private String division;
        private String nip;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDivision() {
            return division;
        }

        public void setDivision(String division) {
            this.division = division;
        }

        public String getNip() {
            return nip;
        }

        public void setNip(String nip) {
            this.nip = nip;
        }
    }
}