package com.crdt;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;

public class Report {

    private int id;
    private User reporter;
    @JsonAdapter(ReportableForcedAdapter.class) private Reportable target;
    private String reason;
    private ReportType type;
    private ReportStatus status;
    private Timestamp timeReported;

    public Report(int id, User reporter, Reportable target, String reason, ReportType type, ReportStatus status, Timestamp timeReported) {

        if(reporter == null) {
            throw new IllegalArgumentException("Reporter cannot be null.");
        }
        if (target == null) {
            throw new IllegalArgumentException("Report target cannot be null.");
        }
        if (reason == null) {
            throw new IllegalArgumentException("Reason cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Report type cannot be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Report status cannot be null.");
        }

        this.id = id;
        this.reporter = reporter;
        this.target = target;
        this.reason = reason;
        this.type = type;
        this.status = status;
        this.timeReported = timeReported;
    }


    public boolean SubmitReport(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, Report.class);

        URL url = new URL(BASE_URL + "/report/submit");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream frisos = conn.getOutputStream()) {
            frisos.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public static ArrayList<Report> GetUserReportFeed(Admin admin, int lastID) throws SQLException {
        return null;
    }

    public static ArrayList<Report> GetPostReportFeed(User user, Subcreddit subcreddit, int lastID) throws SQLException {
        return null;
    }

    public static ArrayList<Report> GetCommentReportFeed(User user, Subcreddit subcreddit, int lastID) throws SQLException {
        return null;
    }

    public void Resolve() throws SQLException {
    }

    public void Dismiss() throws SQLException {
    }


    public int getId() {
        return id;
    }


    public Reportable getTarget() {
        return target;
    }

    public String getReason() {
        return reason;
    }


    public ReportType getType() {
        return type;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public Timestamp getTimeReported() {
        return timeReported;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Report) {
            return this.id == ((Report) obj).id;
        }
        return false;
    }

}