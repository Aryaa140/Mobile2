package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HistoriUserProspekResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<HistoriUserProspek> data;

    @SerializedName("debug")
    private DebugInfo debug;

    // Constructor
    public HistoriUserProspekResponse() {
    }

    public HistoriUserProspekResponse(boolean success, String message, List<HistoriUserProspek> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<HistoriUserProspek> getData() {
        return data;
    }

    public void setData(List<HistoriUserProspek> data) {
        this.data = data;
    }

    public DebugInfo getDebug() {
        return debug;
    }

    public void setDebug(DebugInfo debug) {
        this.debug = debug;
    }

    // Debug Info inner class (optional, untuk handle debug info dari PHP)
    public static class DebugInfo {
        @SerializedName("input_source")
        private String inputSource;

        @SerializedName("received_data")
        private Object receivedData;

        @SerializedName("total_records")
        private int totalRecords;

        @SerializedName("query_id")
        private int queryId;

        public String getInputSource() {
            return inputSource;
        }

        public void setInputSource(String inputSource) {
            this.inputSource = inputSource;
        }

        public Object getReceivedData() {
            return receivedData;
        }

        public void setReceivedData(Object receivedData) {
            this.receivedData = receivedData;
        }

        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public int getQueryId() {
            return queryId;
        }

        public void setQueryId(int queryId) {
            this.queryId = queryId;
        }
    }

    // Helper methods
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    public int getDataCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public String toString() {
        return "HistoriUserProspekResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + (data != null ? data.size() : 0) + " items" +
                '}';
    }
}