package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class LogEntry {

    @SerializedName("Id_log")        private int idLog;
    @SerializedName("Username")      private String username;
    @SerializedName("NIP")           private String nip;
    @SerializedName("Divisi")        private String divisi;
    @SerializedName("Level")         private String level;
    @SerializedName("Aksi")          private String aksi;
    @SerializedName("Fitur")         private String fitur;
    @SerializedName("Deskripsi")     private String deskripsi;
    @SerializedName("Activity_Name") private String activityName;
    @SerializedName("Endpoint")      private String endpoint;
    @SerializedName("Id_target")     private Integer idTarget;
    @SerializedName("Nama_target")   private String namaTarget;
    @SerializedName("Ip_address")    private String ipAddress;
    @SerializedName("User_agent")    private String userAgent;
    @SerializedName("Tanggal_Log")   private String tanggalLog;
    @SerializedName("Device_Time_Millis")     private Long deviceTimeMillis;
    @SerializedName("Device_Time_String")     private String deviceTimeString;
    @SerializedName("Server_Time_Micros")     private Long serverTimeMicros;
    @SerializedName("Timezone")               private String timezone;
    @SerializedName("Timezone_Offset")        private String timezoneOffset;
    @SerializedName("Hari")                   private String hari;
    @SerializedName("Tanggal_Log_Formatted")  private String tanggalLogFormatted;

    // Getter
    public Long getDeviceTimeMillis()        { return deviceTimeMillis; }
    public String getDeviceTimeString()      { return deviceTimeString; }
    public Long getServerTimeMicros()        { return serverTimeMicros; }
    public String getTimezone()              { return timezone; }
    public String getTimezoneOffset()        { return timezoneOffset; }
    public String getHari()                  { return hari; }
    public String getTanggalLogFormatted()   { return tanggalLogFormatted; }
    // Getter
    public int getIdLog()            { return idLog; }
    public String getUsername()      { return username; }
    public String getNip()           { return nip; }
    public String getDivisi()        { return divisi; }
    public String getLevel()         { return level; }
    public String getAksi()          { return aksi; }
    public String getFitur()         { return fitur; }
    public String getDeskripsi()     { return deskripsi; }
    public String getActivityName()  { return activityName; }
    public String getEndpoint()      { return endpoint; }
    public Integer getIdTarget()     { return idTarget; }
    public String getNamaTarget()    { return namaTarget; }
    public String getIpAddress()     { return ipAddress; }
    public String getUserAgent()     { return userAgent; }
    public String getTanggalLog()    { return tanggalLog; }
}