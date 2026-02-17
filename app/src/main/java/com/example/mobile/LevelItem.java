package com.example.mobile;

public class LevelItem {
    private int id;
    private String divisi;
    private String level;  // e.g., "KOR", "AO"

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDivisi() { return divisi; }
    public void setDivisi(String divisi) { this.divisi = divisi; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    @Override
    public String toString() {
        return divisi; // what the spinner displays
    }
}