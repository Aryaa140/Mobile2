package com.example.mobile;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "login.db";
    private static final int DATABASE_VERSION = 2;

    // Tabel pengguna
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_DIVISION = "division";
    public static final String COLUMN_NIP = "nip";
    public static final String COLUMN_PASSWORD = "password";

    // Tabel divisi (jika ingin membuat referensi)
    public static final String TABLE_DIVISIONS = "divisions";
    public static final String COLUMN_DIVISION_ID = "division_id";
    public static final String COLUMN_DIVISION_NAME = "division_name";

    // Query membuat tabel
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE,"
            + COLUMN_DIVISION + " TEXT,"
            + COLUMN_NIP + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT"
            + ")";

    private static final String CREATE_TABLE_DIVISIONS = "CREATE TABLE " + TABLE_DIVISIONS + "("
            + COLUMN_DIVISION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_DIVISION_NAME + " TEXT UNIQUE"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_DIVISIONS);

        // Menambahkan divisi default
        insertDivision(db, "Marketing");
        insertDivision(db, "Electronic Data Processing");

        // Menambahkan pengguna contoh
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, "admin");
        values.put(COLUMN_DIVISION, "IT");
        values.put(COLUMN_NIP, "1234567890");
        values.put(COLUMN_PASSWORD, "password123"); // Dalam aplikasi nyata, password harus di-hash
        db.insert(TABLE_USERS, null, values);
    }

    private void insertDivision(SQLiteDatabase db, String divisionName) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DIVISION_NAME, divisionName);
        db.insert(TABLE_DIVISIONS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Upgrade dari versi 1 ke 2 - tambahkan kolom baru
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_DIVISION + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_NIP + " TEXT");

            // Buat tabel divisi
            db.execSQL(CREATE_TABLE_DIVISIONS);

            // Tambahkan divisi default
            insertDivision(db, "Marketing");
            insertDivision(db, "Electronic Data Procesing");

        }
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

        return count > 0;
    }

    // Method untuk mendapatkan semua divisi
    public Cursor getAllDivisions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_DIVISIONS,
                new String[]{COLUMN_DIVISION_ID, COLUMN_DIVISION_NAME},
                null, null, null, null, COLUMN_DIVISION_NAME + " ASC");
    }

    // Method untuk menambahkan pengguna baru
    public boolean addUser(String username, String division, String nip, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_DIVISION, division);
        values.put(COLUMN_NIP, nip);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

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