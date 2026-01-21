package com.example.mobile;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class BasicResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("available")
    private Boolean available;

    @SerializedName("fcm_notification")
    private Map<String, Object> fcmNotification;

    @SerializedName("data_saved")
    private Boolean dataSaved;

    @SerializedName("id_proyek")
    private Integer idProyek;

    @SerializedName("fasilitas_count")
    private Integer fasilitasCount;

    // ✅ FIELD UNTUK AUTO DELETE (sudah ada)
    @SerializedName("deleted_count")
    private Integer deletedCount;

    @SerializedName("expired_count")
    private Integer expiredCount;

    // ✅ TAMBAHKAN FIELD BARU UNTUK DATA TAMBAHAN
    @SerializedName("expired_promos")
    private List<Map<String, Object>> expiredPromos;

    @SerializedName("total")
    private Integer total;

    @SerializedName("added_count")
    private Integer addedCount;

    @SerializedName("updated_count")
    private Integer updatedCount;

    @SerializedName("id_hunian")
    private Integer idHunian;

    // Constructor default untuk Gson
    public BasicResponse() {
    }

    // Constructor untuk manual creation
    public BasicResponse(boolean success, String message, Boolean dataSaved, Integer idProyek) {
        this.success = success;
        this.message = message;
        this.dataSaved = dataSaved;
        this.idProyek = idProyek;
    }

    // ✅ CONSTRUCTOR KHUSUS UNTUK AUTO DELETE
    public BasicResponse(boolean success, String message, int expiredCount, int deletedCount) {
        this.success = success;
        this.message = message;
        this.expiredCount = expiredCount;
        this.deletedCount = deletedCount;
    }

    // GETTERS
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public boolean isAvailable() {
        return available != null && available;
    }

    public Map<String, Object> getFcmNotification() {
        return fcmNotification;
    }

    public Boolean getDataSaved() {
        return dataSaved != null ? dataSaved : false;
    }

    public Integer getIdProyek() {
        return idProyek != null ? idProyek : 0;
    }

    public Integer getFasilitasCount() {
        return fasilitasCount != null ? fasilitasCount : 0;
    }

    // ✅ GETTER UNTUK AUTO DELETE (dengan default value 0)
    public int getDeletedCount() {
        return deletedCount != null ? deletedCount : 0;
    }

    public int getExpiredCount() {
        return expiredCount != null ? expiredCount : 0;
    }

    // ✅ GETTER UNTUK FIELD BARU
    public List<Map<String, Object>> getExpiredPromos() {
        return expiredPromos;
    }

    public int getTotal() {
        return total != null ? total : 0;
    }

    public int getAddedCount() {
        return addedCount != null ? addedCount : 0;
    }

    public int getUpdatedCount() {
        return updatedCount != null ? updatedCount : 0;
    }

    // ✅ METHOD HELPER UNTUK DEBUG
    public boolean hasExpiredPromos() {
        return expiredPromos != null && !expiredPromos.isEmpty();
    }

    public int getTotalProcessed() {
        return getExpiredCount() + getDeletedCount();
    }

    // SETTERS
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDataSaved(Boolean dataSaved) {
        this.dataSaved = dataSaved;
    }

    public void setIdProyek(Integer idProyek) {
        this.idProyek = idProyek;
    }

    public void setFcmNotification(Map<String, Object> fcmNotification) {
        this.fcmNotification = fcmNotification;
    }

    public void setDeletedCount(Integer deletedCount) {
        this.deletedCount = deletedCount;
    }

    public void setExpiredCount(Integer expiredCount) {
        this.expiredCount = expiredCount;
    }

    public void setExpiredPromos(List<Map<String, Object>> expiredPromos) {
        this.expiredPromos = expiredPromos;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public void setAddedCount(Integer addedCount) {
        this.addedCount = addedCount;
    }

    public void setUpdatedCount(Integer updatedCount) {
        this.updatedCount = updatedCount;
    }

    public Integer getIdHunian() {
        return idHunian != null ? idHunian : 0;
    }

    // SETTER untuk id_hunian
    public void setIdHunian(Integer idHunian) {
        this.idHunian = idHunian;
    }

    // ✅ METHOD UNTUK LOGGING
    public String toDebugString() {
        return "BasicResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", expiredCount=" + getExpiredCount() +
                ", deletedCount=" + getDeletedCount() +
                ", total=" + getTotal() +
                ", hasExpiredPromos=" + hasExpiredPromos() +
                '}';
    }

    @Override
    public String toString() {
        return "BasicResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", expiredCount=" + getExpiredCount() +
                ", deletedCount=" + getDeletedCount() +
                '}';
    }
}