package com.example.mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "login.db";
    private static final int DATABASE_VERSION = 3; // Tingkatkan versi database

    // tabel prospek
    public static final String TABLE_PROSPEK = "prospek";
    public static final String COLUMN_PROSPEK_ID = "prospek_id";
    public static final String COLUMN_NAMA = "nama";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_NO_HP = "no_hp";
    public static final String COLUMN_ALAMAT = "alamat";
    public static final String COLUMN_REFERENSI = "referensi";

    private static final String CREATE_TABLE_PROSPEK = "CREATE TABLE " + TABLE_PROSPEK + "("
            + COLUMN_PROSPEK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAMA + " TEXT NOT NULL,"
            + COLUMN_EMAIL + " TEXT,"
            + COLUMN_NO_HP + " TEXT,"
            + COLUMN_ALAMAT + " TEXT,"
            + COLUMN_REFERENSI + " TEXT" + ")";

    // tabel users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_DIVISION = "division";
    public static final String COLUMN_NIP = "nip";
    public static final String COLUMN_PASSWORD = "password";

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
        db.execSQL(CREATE_TABLE_PROSPEK);

        // User contoh Marketing
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, "admin");
        values.put(COLUMN_DIVISION, "Marketing");
        values.put(COLUMN_NIP, "1234567890");
        values.put(COLUMN_PASSWORD, "password123");
        db.insert(TABLE_USERS, null, values);

        // User contoh EDP
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROSPEK);
        onCreate(db);
        Log.d("DatabaseHelper", "Database upgraded from version " + oldVersion + " to " + newVersion);
    }

    // ======== USER METHODS ========

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

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

    public String[] getAllDivisions() {
        return new String[]{"Marketing", "Electronic Data Procesing"};
    }

    public boolean addUser(String username, String division, String nip, String password) {
        if (!division.equals("Marketing") && !division.equals("Electronic Data Procesing")) {
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
        return result != -1;
    }

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
            cursor.close();
        }

        db.close();
        return user;
    }

    public boolean updateUserPassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        int rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);
        db.close();
        return rowsAffected > 0;
    }

    public boolean verifyCurrentPassword(String username, String currentPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, currentPassword};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    public boolean updateUser(String nip, String newUsername, String newDivision) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, newUsername);
        values.put(COLUMN_DIVISION, newDivision);

        String selection = COLUMN_NIP + " = ?";
        String[] selectionArgs = {nip};

        int rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);
        db.close();
        return rowsAffected > 0;
    }

    public User getUserByNip(String nip) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID, COLUMN_USERNAME, COLUMN_DIVISION, COLUMN_NIP};
        String selection = COLUMN_NIP + " = ?";
        String[] selectionArgs = {nip};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            user.setDivision(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIVISION)));
            user.setNip(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIP)));
            cursor.close();
        }

        db.close();
        return user;
    }

    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        int rowsAffected = db.delete(TABLE_USERS, selection, selectionArgs);
        db.close();
        return rowsAffected > 0;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        int userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            cursor.close();
        }

        db.close();
        return userId;
    }

    // ======== PROSPEK METHODS ========

    public long addProspek(String nama, String email, String noHp, String alamat, String referensi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA, nama);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_NO_HP, noHp);
        values.put(COLUMN_ALAMAT, alamat);
        values.put(COLUMN_REFERENSI, referensi);

        long result = db.insert(TABLE_PROSPEK, null, values);
        db.close();
        return result;
    }

    public List<Prospek> getAllProspek() {
        List<Prospek> prospekList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_PROSPEK_ID, COLUMN_NAMA, COLUMN_EMAIL, COLUMN_NO_HP, COLUMN_ALAMAT, COLUMN_REFERENSI};
        Cursor cursor = db.query(TABLE_PROSPEK, columns, null, null, null, null, COLUMN_NAMA + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Prospek prospek = new Prospek(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROSPEK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO_HP)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALAMAT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFERENSI))
                );
                prospekList.add(prospek);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return prospekList;
    }

    public Prospek getProspekById(int prospekId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_PROSPEK_ID, COLUMN_NAMA, COLUMN_EMAIL, COLUMN_NO_HP, COLUMN_ALAMAT, COLUMN_REFERENSI};
        String selection = COLUMN_PROSPEK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(prospekId)};

        Cursor cursor = db.query(TABLE_PROSPEK, columns, selection, selectionArgs, null, null, null);

        Prospek prospek = null;
        if (cursor != null && cursor.moveToFirst()) {
            prospek = new Prospek(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROSPEK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO_HP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALAMAT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFERENSI))
            );
            cursor.close();
        }

        db.close();
        return prospek;
    }

    public int updateProspek(int prospekId, String nama, String email, String noHp, String alamat, String referensi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA, nama);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_NO_HP, noHp);
        values.put(COLUMN_ALAMAT, alamat);
        values.put(COLUMN_REFERENSI, referensi);

        String selection = COLUMN_PROSPEK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(prospekId)};
        int result = db.update(TABLE_PROSPEK, values, selection, selectionArgs);
        db.close();
        return result;
    }

    public int deleteProspek(int prospekId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_PROSPEK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(prospekId)};
        int result = db.delete(TABLE_PROSPEK, selection, selectionArgs);
        db.close();
        return result;
    }

    // ======== USER MODEL ========

    public static class User {
        private int id;
        private String username;
        private String division;
        private String nip;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getDivision() { return division; }
        public void setDivision(String division) { this.division = division; }

        public String getNip() { return nip; }
        public void setNip(String nip) { this.nip = nip; }
    }
}
