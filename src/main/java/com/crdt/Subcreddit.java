package com.crdt;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Subcreddit {
    private int id;
    private String name ;
    private String description ;
    private Timestamp timecreated;
    private User creator;
    private Media subLogo;
    private boolean isPrivate;


    public Subcreddit(int id, String name, String description, Timestamp timecreated, User creator,  Media logo, boolean isPrivate){
        this.id = id;
        this.name = name;
        this.description = description;
        this.timecreated = timecreated;
        this.creator = creator;
        this.subLogo = logo;
        this.isPrivate = isPrivate;
    }

    public boolean create(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, Subcreddit.class);

        URL url = new URL(BASE_URL + "/subcreddit/create");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public boolean update(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, Subcreddit.class);

        URL url = new URL(BASE_URL + "/subcreddit/edit");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public boolean delete(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, Subcreddit.class);

        URL url = new URL(BASE_URL + "/subcreddit/delete");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public ArrayList<User> GetMembers() {
        ArrayList<User> members = new ArrayList<>();
        return members;
    }

    public ArrayList<User> GetBannedMembers() {
        if(this.id <= 0)
            return null;

        ArrayList<User> bannedMembers = new ArrayList<>();
        return bannedMembers;
    }

    public int GetSubId() {
        return id;
    }

    public String GetSubName() {
        return name;
    }

    public String GetDescription() {
        return description;
    }

    public Timestamp GetTimecreated() {
        return timecreated;
    }

    public User GetCreator() {
        return creator;
    }

     public Media GetLogo() {
        return subLogo;
    }

    public boolean GetPrivate() {
        return isPrivate;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Subcreddit) {
            return this.id == ((Subcreddit) obj).id;
        }
        return false;
    }
}

