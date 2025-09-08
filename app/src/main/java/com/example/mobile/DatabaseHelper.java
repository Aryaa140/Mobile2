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
    private static final int DATABASE_VERSION = 7; // Tingkatkan versi database

    // tabel prospek
    public static final String TABLE_PROSPEK = "prospek";
    public static final String COLUMN_PROSPEK_ID = "prospek_id";
    public static final String COLUMN_NAMA = "nama";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_NO_HP = "no_hp";
    public static final String COLUMN_ALAMAT = "alamat";
    public static final String COLUMN_REFERENSI = "referensi";
    public static final String COLUMN_PENGINPUT = "penginput";
    public static final String COLUMN_TANGGAL_BUAT = "tanggal_buat";


    private static final String CREATE_TABLE_PROSPEK = "CREATE TABLE " + TABLE_PROSPEK + "("
            + COLUMN_PROSPEK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PENGINPUT + " TEXT NOT NULL," // TAMBAHAN BARU
            + COLUMN_NAMA + " TEXT NOT NULL,"
            + COLUMN_EMAIL + " TEXT,"
            + COLUMN_NO_HP + " TEXT,"
            + COLUMN_ALAMAT + " TEXT,"
            + COLUMN_REFERENSI + " TEXT,"
            + COLUMN_TANGGAL_BUAT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
    // tabel user prospek
    // tabel user_prospek (tabel baru)
    public static final String TABLE_USER_PROSPEK = "user_prospek";
    public static final String COLUMN_USER_PROSPEK_ID = "user_prospek_id";
    public static final String COLUMN_UANG_PENGADAAN = "uang_pengadaan";
    public static final String COLUMN_TANGGAL_UPDATE = "tanggal_update";

    private static final String CREATE_TABLE_USER_PROSPEK = "CREATE TABLE " + TABLE_USER_PROSPEK + "("
            + COLUMN_USER_PROSPEK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PROSPEK_ID + " INTEGER,"
            + COLUMN_PENGINPUT + " TEXT NOT NULL,"
            + COLUMN_NAMA + " TEXT NOT NULL,"
            + COLUMN_EMAIL + " TEXT,"
            + COLUMN_NO_HP + " TEXT,"
            + COLUMN_ALAMAT + " TEXT,"
            + COLUMN_REFERENSI + " TEXT,"
            + COLUMN_UANG_PENGADAAN + " REAL DEFAULT 0,"
            + COLUMN_TANGGAL_BUAT + " DATETIME,"
            + COLUMN_TANGGAL_UPDATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY (" + COLUMN_PROSPEK_ID + ") REFERENCES " + TABLE_PROSPEK + "(" + COLUMN_PROSPEK_ID + ")" + ")";


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
    // tabel proyek
    public static final String TABLE_PROYEK = "proyek";
    public static final String COLUMN_PROYEK_ID = "proyek_id";
    public static final String COLUMN_NAMA_PROYEK = "nama_proyek";
    public static final String COLUMN_LOKASI_PROYEK = "lokasi_proyek";
    public static final String COLUMN_STATUS_PROYEK = "status_proyek";

    private static final String CREATE_TABLE_PROYEK = "CREATE TABLE " + TABLE_PROYEK + "("
            + COLUMN_PROYEK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAMA_PROYEK + " TEXT NOT NULL,"
            + COLUMN_LOKASI_PROYEK + " TEXT NOT NULL,"
            + COLUMN_STATUS_PROYEK + " TEXT NOT NULL" + ")";
    //tabel fasilitas
    public static final String TABLE_FASILITAS = "fasilitas";
    public static final String COLUMN_FASILITAS_ID = "fasilitas_id";
    public static final String COLUMN_NAMA_FASILITAS = "nama_fasilitas";
    public static final String COLUMN_NAMA_PROYEK_FASILITAS = "nama_proyek";
    public static final String COLUMN_LOKASI_FASILITAS = "lokasi_fasilitas";
    public static final String COLUMN_STATUS_FASILITAS = "status_fasilitas";

    private static final String CREATE_TABLE_FASILITAS = "CREATE TABLE " + TABLE_FASILITAS + "("
            + COLUMN_FASILITAS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAMA_FASILITAS + " TEXT NOT NULL,"
            + COLUMN_NAMA_PROYEK_FASILITAS + " TEXT NOT NULL,"
            + COLUMN_LOKASI_FASILITAS + " TEXT NOT NULL,"
            + COLUMN_STATUS_FASILITAS + " TEXT NOT NULL,"
            + "FOREIGN KEY (" + COLUMN_NAMA_PROYEK_FASILITAS + ") REFERENCES " + TABLE_PROYEK + "(" + COLUMN_NAMA_PROYEK + ")" + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PROSPEK);
        db.execSQL(CREATE_TABLE_USER_PROSPEK);
        db.execSQL(CREATE_TABLE_PROYEK);
        db.execSQL(CREATE_TABLE_FASILITAS);

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
        if (oldVersion < 5) {
            // Upgrade dari versi lama ke versi 5
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROSPEK);
            db.execSQL(CREATE_TABLE_USER_PROSPEK);
        }

        if (oldVersion < 6) {
            // Upgrade ke versi 6 dengan menambahkan tabel proyek
            db.execSQL(CREATE_TABLE_PROYEK);
        }

        if (oldVersion < 7) {
            // Upgrade ke versi 7 dengan menambahkan tabel fasilitas
            db.execSQL(CREATE_TABLE_FASILITAS);
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROSPEK);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROSPEK);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROYEK);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FASILITAS);
            onCreate(db);
        }
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

    public long addProspek(String penginput, String nama, String email, String noHp, String alamat, String referensi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PENGINPUT, penginput); // TAMBAHAN
        values.put(COLUMN_NAMA, nama);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_NO_HP, noHp);
        values.put(COLUMN_ALAMAT, alamat);
        values.put(COLUMN_REFERENSI, referensi);
        // COLUMN_TANGGAL_BUAT otomatis terisi karena DEFAULT CURRENT_TIMESTAMP

        long result = db.insert(TABLE_PROSPEK, null, values);
        db.close();
        return result;
    }

    public List<Prospek> getAllProspek() {
        List<Prospek> prospekList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] columns = {COLUMN_PROSPEK_ID, COLUMN_PENGINPUT, COLUMN_NAMA, COLUMN_EMAIL,
                    COLUMN_NO_HP, COLUMN_ALAMAT, COLUMN_REFERENSI, COLUMN_TANGGAL_BUAT};

            cursor = db.query(TABLE_PROSPEK, columns, null, null, null, null, COLUMN_TANGGAL_BUAT + " DESC");

            Log.d("DatabaseHelper", "Jumlah data: " + cursor.getCount());

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        Prospek prospek = new Prospek(
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROSPEK_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PENGINPUT)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO_HP)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALAMAT)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFERENSI)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_BUAT))
                        );
                        prospekList.add(prospek);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error parsing data: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getAllProspek: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return prospekList;
    }

    public Prospek getProspekById(int prospekId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_PROSPEK_ID, COLUMN_PENGINPUT, COLUMN_NAMA, COLUMN_EMAIL,
                COLUMN_NO_HP, COLUMN_ALAMAT, COLUMN_REFERENSI, COLUMN_TANGGAL_BUAT};
        String selection = COLUMN_PROSPEK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(prospekId)};

        Cursor cursor = db.query(TABLE_PROSPEK, columns, selection, selectionArgs, null, null, null);

        Prospek prospek = null;
        if (cursor != null && cursor.moveToFirst()) {
            prospek = new Prospek(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROSPEK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PENGINPUT)), // TAMBAHAN
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO_HP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALAMAT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFERENSI)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_BUAT)) // TAMBAHAN
            );
            cursor.close();
        }

        db.close();
        return prospek;
    }

    public int updateProspek(int prospekId, String penginput, String nama, String email,
                             String noHp, String alamat, String referensi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PENGINPUT, penginput); // TAMBAHAN
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
    // ====== USER PROSPEK METHOD ========
    public List<String> getAllProspekNama() {
        List<String> prospekNamaList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] columns = {COLUMN_PROSPEK_ID, COLUMN_NAMA};
            cursor = db.query(TABLE_PROSPEK, columns, null, null, null, null, COLUMN_NAMA + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    prospekNamaList.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getAllProspekNama: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return prospekNamaList;
    }
    public long addUserProspek(int prospekId, String penginput, String nama, String email,
                               String noHp, String alamat, String referensi,
                               double uangPengadaan, String tanggalBuat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROSPEK_ID, prospekId);
        values.put(COLUMN_PENGINPUT, penginput);
        values.put(COLUMN_NAMA, nama);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_NO_HP, noHp);
        values.put(COLUMN_ALAMAT, alamat);
        values.put(COLUMN_REFERENSI, referensi);
        values.put(COLUMN_UANG_PENGADAAN, uangPengadaan);
        values.put(COLUMN_TANGGAL_BUAT, tanggalBuat);

        long result = db.insert(TABLE_USER_PROSPEK, null, values);
        db.close();
        return result;
    }
    public List<UserProspek> getAllUserProspek() {
        List<UserProspek> userProspekList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] columns = {
                    COLUMN_USER_PROSPEK_ID, COLUMN_PROSPEK_ID, COLUMN_PENGINPUT,
                    COLUMN_NAMA, COLUMN_EMAIL, COLUMN_NO_HP, COLUMN_ALAMAT,
                    COLUMN_REFERENSI, COLUMN_UANG_PENGADAAN,
                    COLUMN_TANGGAL_BUAT, COLUMN_TANGGAL_UPDATE
            };

            cursor = db.query(TABLE_USER_PROSPEK, columns, null, null, null, null, COLUMN_TANGGAL_UPDATE + " DESC");

            Log.d("DatabaseHelper", "Jumlah data user_prospek: " + cursor.getCount());

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        UserProspek userProspek = new UserProspek(
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_PROSPEK_ID)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROSPEK_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PENGINPUT)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO_HP)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALAMAT)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFERENSI)),
                                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_UANG_PENGADAAN)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_BUAT)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_UPDATE))
                        );
                        userProspekList.add(userProspek);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error parsing data user_prospek: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getAllUserProspek: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return userProspekList;
    }
    public Prospek getProspekByNama(String nama) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_PROSPEK_ID, COLUMN_PENGINPUT, COLUMN_NAMA, COLUMN_EMAIL,
                COLUMN_NO_HP, COLUMN_ALAMAT, COLUMN_REFERENSI, COLUMN_TANGGAL_BUAT};
        String selection = COLUMN_NAMA + " = ?";
        String[] selectionArgs = {nama};

        Cursor cursor = db.query(TABLE_PROSPEK, columns, selection, selectionArgs, null, null, null);

        Prospek prospek = null;
        if (cursor != null && cursor.moveToFirst()) {
            prospek = new Prospek(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROSPEK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PENGINPUT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO_HP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALAMAT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFERENSI)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_BUAT))
            );
            cursor.close();
        }

        db.close();
        return prospek;
    }
    public int deleteProspekById(int prospekId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_PROSPEK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(prospekId)};
        int result = db.delete(TABLE_PROSPEK, selection, selectionArgs);
        db.close();
        return result;
    }
    public boolean migrateAndDeleteProspek(int prospekId, double uangPengadaan) {
        Prospek prospek = getProspekById(prospekId);
        if (prospek != null) {
            // Tambahkan ke user_prospek
            long result = addUserProspek(
                    prospek.getProspekId(),
                    prospek.getPenginput(),
                    prospek.getNama(),
                    prospek.getEmail(),
                    prospek.getNoHp(),
                    prospek.getAlamat(),
                    prospek.getReferensi(),
                    uangPengadaan,
                    prospek.getTanggalBuat()
            );

            // Hapus dari prospek jika berhasil ditambahkan ke user_prospek
            if (result != -1) {
                int deleteResult = deleteProspekById(prospekId);
                return deleteResult > 0;
            }
        }
        return false;
    }

    public static class UserProspek {
        private int userProspekId;
        private int prospekId;
        private String penginput;
        private String nama;
        private String email;
        private String noHp;
        private String alamat;
        private String referensi;
        private double uangPengadaan;
        private String tanggalBuat;
        private String tanggalUpdate;

        public UserProspek(int userProspekId, int prospekId, String penginput, String nama,
                           String email, String noHp, String alamat, String referensi,
                           double uangPengadaan, String tanggalBuat, String tanggalUpdate) {
            this.userProspekId = userProspekId;
            this.prospekId = prospekId;
            this.penginput = penginput;
            this.nama = nama;
            this.email = email;
            this.noHp = noHp;
            this.alamat = alamat;
            this.referensi = referensi;
            this.uangPengadaan = uangPengadaan;
            this.tanggalBuat = tanggalBuat;
            this.tanggalUpdate = tanggalUpdate;
        }

        // Getter dan Setter methods
        public int getUserProspekId() { return userProspekId; }
        public void setUserProspekId(int userProspekId) { this.userProspekId = userProspekId; }

        public int getProspekId() { return prospekId; }
        public void setProspekId(int prospekId) { this.prospekId = prospekId; }

        public String getPenginput() { return penginput; }
        public void setPenginput(String penginput) { this.penginput = penginput; }

        public String getNama() { return nama; }
        public void setNama(String nama) { this.nama = nama; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getNoHp() { return noHp; }
        public void setNoHp(String noHp) { this.noHp = noHp; }

        public String getAlamat() { return alamat; }
        public void setAlamat(String alamat) { this.alamat = alamat; }

        public String getReferensi() { return referensi; }
        public void setReferensi(String referensi) { this.referensi = referensi; }

        public double getUangPengadaan() { return uangPengadaan; }
        public void setUangPengadaan(double uangPengadaan) { this.uangPengadaan = uangPengadaan; }

        public String getTanggalBuat() { return tanggalBuat; }
        public void setTanggalBuat(String tanggalBuat) { this.tanggalBuat = tanggalBuat; }

        public String getTanggalUpdate() { return tanggalUpdate; }
        public void setTanggalUpdate(String tanggalUpdate) { this.tanggalUpdate = tanggalUpdate; }
    }
//===== proyek method ====
public static class Proyek {
    private int proyekId;
    private String namaProyek;
    private String lokasiProyek;
    private String statusProyek;

    public Proyek(int proyekId, String namaProyek, String lokasiProyek, String statusProyek) {
        this.proyekId = proyekId;
        this.namaProyek = namaProyek;
        this.lokasiProyek = lokasiProyek;
        this.statusProyek = statusProyek;
    }

    public Proyek(String namaProyek, String lokasiProyek, String statusProyek) {
        this.namaProyek = namaProyek;
        this.lokasiProyek = lokasiProyek;
        this.statusProyek = statusProyek;
    }

    // Getter dan Setter
    public int getProyekId() { return proyekId; }
    public void setProyekId(int proyekId) { this.proyekId = proyekId; }

    public String getNamaProyek() { return namaProyek; }
    public void setNamaProyek(String namaProyek) { this.namaProyek = namaProyek; }

    public String getLokasiProyek() { return lokasiProyek; }
    public void setLokasiProyek(String lokasiProyek) { this.lokasiProyek = lokasiProyek; }

    public String getStatusProyek() { return statusProyek; }
    public void setStatusProyek(String statusProyek) { this.statusProyek = statusProyek; }
}
    public long addProyek(String namaProyek, String lokasiProyek, String statusProyek) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nama_proyek", namaProyek);
        values.put("lokasi_proyek", lokasiProyek);
        values.put("status_proyek", statusProyek);

        long result = -1;
        try {
            result = db.insert("proyek", null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error addProyek: " + e.getMessage());
        } finally {
            db.close();
        }
        return result;
    }

    public List<Proyek> getAllProyek() {
        List<Proyek> proyekList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] columns = {
                    COLUMN_PROYEK_ID,
                    COLUMN_NAMA_PROYEK,
                    COLUMN_LOKASI_PROYEK,
                    COLUMN_STATUS_PROYEK
            };

            cursor = db.query(TABLE_PROYEK, columns, null, null, null, null, COLUMN_PROYEK_ID + " DESC");

            Log.d("DatabaseHelper", "Jumlah data proyek: " + cursor.getCount());

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        Proyek proyek = new Proyek(
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROYEK_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_PROYEK)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOKASI_PROYEK)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS_PROYEK))
                        );
                        proyekList.add(proyek);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error parsing data proyek: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getAllProyek: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return proyekList;
    }


    public Proyek getProyekById(int proyekId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_PROYEK_ID,
                COLUMN_NAMA_PROYEK,
                COLUMN_LOKASI_PROYEK,
                COLUMN_STATUS_PROYEK
        };
        String selection = COLUMN_PROYEK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(proyekId)};

        Cursor cursor = db.query(TABLE_PROYEK, columns, selection, selectionArgs, null, null, null);

        Proyek proyek = null;
        if (cursor != null && cursor.moveToFirst()) {
            proyek = new Proyek(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROYEK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_PROYEK)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOKASI_PROYEK)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS_PROYEK))
            );
            cursor.close();
        }

        db.close();
        return proyek;
    }

    public int updateProyek(int proyekId, String namaProyek, String lokasiProyek, String statusProyek) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA_PROYEK, namaProyek);
        values.put(COLUMN_LOKASI_PROYEK, lokasiProyek);
        values.put(COLUMN_STATUS_PROYEK, statusProyek);

        String selection = COLUMN_PROYEK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(proyekId)};
        int result = db.update(TABLE_PROYEK, values, selection, selectionArgs);
        db.close();
        return result;
    }

    public int deleteProyek(int proyekId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_PROYEK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(proyekId)};
        int result = db.delete(TABLE_PROYEK, selection, selectionArgs);
        db.close();
        return result;
    }
    // ==== fasilitas method =====
    public static class Fasilitas {
        private int fasilitasId;
        private String namaFasilitas;
        private String namaProyek;
        private String lokasiFasilitas;
        private String statusFasilitas;

        public Fasilitas(int fasilitasId, String namaFasilitas, String namaProyek, String lokasiFasilitas, String statusFasilitas) {
            this.fasilitasId = fasilitasId;
            this.namaFasilitas = namaFasilitas;
            this.namaProyek = namaProyek;
            this.lokasiFasilitas = lokasiFasilitas;
            this.statusFasilitas = statusFasilitas;
        }

        public Fasilitas(String namaFasilitas, String namaProyek, String lokasiFasilitas, String statusFasilitas) {
            this.namaFasilitas = namaFasilitas;
            this.namaProyek = namaProyek;
            this.lokasiFasilitas = lokasiFasilitas;
            this.statusFasilitas = statusFasilitas;
        }

        // Getter dan Setter methods
        public int getFasilitasId() { return fasilitasId; }
        public void setFasilitasId(int fasilitasId) { this.fasilitasId = fasilitasId; }

        public String getNamaFasilitas() { return namaFasilitas; }
        public void setNamaFasilitas(String namaFasilitas) { this.namaFasilitas = namaFasilitas; }

        public String getNamaProyek() { return namaProyek; }
        public void setNamaProyek(String namaProyek) { this.namaProyek = namaProyek; }

        public String getLokasiFasilitas() { return lokasiFasilitas; }
        public void setLokasiFasilitas(String lokasiFasilitas) { this.lokasiFasilitas = lokasiFasilitas; }

        public String getStatusFasilitas() { return statusFasilitas; }
        public void setStatusFasilitas(String statusFasilitas) { this.statusFasilitas = statusFasilitas; }
    }
    public long addFasilitas(String namaFasilitas, String namaProyek, String lokasiFasilitas, String statusFasilitas) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA_FASILITAS, namaFasilitas);
        values.put(COLUMN_NAMA_PROYEK_FASILITAS, namaProyek);
        values.put(COLUMN_LOKASI_FASILITAS, lokasiFasilitas);
        values.put(COLUMN_STATUS_FASILITAS, statusFasilitas);

        long result = db.insert(TABLE_FASILITAS, null, values);
        db.close();
        return result;
    }

    public List<Fasilitas> getAllFasilitas() {
        List<Fasilitas> fasilitasList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] columns = {
                    COLUMN_FASILITAS_ID,
                    COLUMN_NAMA_FASILITAS,
                    COLUMN_NAMA_PROYEK_FASILITAS,
                    COLUMN_LOKASI_FASILITAS,
                    COLUMN_STATUS_FASILITAS
            };

            cursor = db.query(TABLE_FASILITAS, columns, null, null, null, null, COLUMN_NAMA_FASILITAS + " ASC");

            Log.d("DatabaseHelper", "Jumlah data fasilitas: " + cursor.getCount());

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        Fasilitas fasilitas = new Fasilitas(
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FASILITAS_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_FASILITAS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_PROYEK_FASILITAS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOKASI_FASILITAS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS_FASILITAS))
                        );
                        fasilitasList.add(fasilitas);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error parsing data fasilitas: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getAllFasilitas: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return fasilitasList;
    }

    public List<Fasilitas> getFasilitasByProyek(String namaProyek) {
        List<Fasilitas> fasilitasList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] columns = {
                    COLUMN_FASILITAS_ID,
                    COLUMN_NAMA_FASILITAS,
                    COLUMN_NAMA_PROYEK_FASILITAS,
                    COLUMN_LOKASI_FASILITAS,
                    COLUMN_STATUS_FASILITAS
            };

            String selection = COLUMN_NAMA_PROYEK_FASILITAS + " = ?";
            String[] selectionArgs = {namaProyek};

            cursor = db.query(TABLE_FASILITAS, columns, selection, selectionArgs, null, null, COLUMN_NAMA_FASILITAS + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        Fasilitas fasilitas = new Fasilitas(
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FASILITAS_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_FASILITAS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_PROYEK_FASILITAS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOKASI_FASILITAS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS_FASILITAS))
                        );
                        fasilitasList.add(fasilitas);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error parsing data fasilitas: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getFasilitasByProyek: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return fasilitasList;
    }

    public Fasilitas getFasilitasById(int fasilitasId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_FASILITAS_ID,
                COLUMN_NAMA_FASILITAS,
                COLUMN_NAMA_PROYEK_FASILITAS,
                COLUMN_LOKASI_FASILITAS,
                COLUMN_STATUS_FASILITAS
        };
        String selection = COLUMN_FASILITAS_ID + " = ?";
        String[] selectionArgs = {String.valueOf(fasilitasId)};

        Cursor cursor = db.query(TABLE_FASILITAS, columns, selection, selectionArgs, null, null, null);

        Fasilitas fasilitas = null;
        if (cursor != null && cursor.moveToFirst()) {
            fasilitas = new Fasilitas(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FASILITAS_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_FASILITAS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_PROYEK_FASILITAS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOKASI_FASILITAS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS_FASILITAS))
            );
            cursor.close();
        }

        db.close();
        return fasilitas;
    }

    public int updateFasilitas(int fasilitasId, String namaFasilitas, String namaProyek, String lokasiFasilitas, String statusFasilitas) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA_FASILITAS, namaFasilitas);
        values.put(COLUMN_NAMA_PROYEK_FASILITAS, namaProyek);
        values.put(COLUMN_LOKASI_FASILITAS, lokasiFasilitas);
        values.put(COLUMN_STATUS_FASILITAS, statusFasilitas);

        String selection = COLUMN_FASILITAS_ID + " = ?";
        String[] selectionArgs = {String.valueOf(fasilitasId)};
        int result = db.update(TABLE_FASILITAS, values, selection, selectionArgs);
        db.close();
        return result;
    }

    public int deleteFasilitas(int fasilitasId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_FASILITAS_ID + " = ?";
        String[] selectionArgs = {String.valueOf(fasilitasId)};
        int result = db.delete(TABLE_FASILITAS, selection, selectionArgs);
        db.close();
        return result;
    }

    public List<String> getAllNamaProyek() {
        List<String> proyekList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] columns = {COLUMN_NAMA_PROYEK};
            cursor = db.query(TABLE_PROYEK, columns, null, null, null, null, COLUMN_NAMA_PROYEK + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    proyekList.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_PROYEK)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getAllNamaProyek: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return proyekList;
    }
}
