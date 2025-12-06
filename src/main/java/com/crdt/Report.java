package com.crdt;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Report {

    private int id;
    private User reporter;
    private Reportable target;
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


    public void SubmitReport() throws SQLException {
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