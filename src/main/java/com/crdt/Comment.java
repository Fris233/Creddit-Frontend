package com.crdt;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Comment implements Voteable, Reportable {
    private int id;
    private Post post;
    private User author;
    private int parentID;
    private String content;
    private Media media;
    private int votes;
    private int replyCount;
    private Timestamp timeCreated;
    private Timestamp timeEdited;
    private boolean deleted;

    public Comment(int id, Post post, User author, String content, Media media, int parentID, int votes, int replyCount, Timestamp createTime, Timestamp editTime, boolean deleted) {
        this.id = id;
        this.post = post;
        this.author = author;
        this.content = content;
        this.media = media;
        this.parentID = parentID;
        this.votes = votes;
        this.replyCount = replyCount;
        this.timeCreated = createTime;
        this.timeEdited = editTime;
        this.deleted = deleted;
    }

    public int create(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, Comment.class);

        URL url = new URL(BASE_URL + "/comment/create");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return gson.fromJson(sb.toString(), int.class);
    }

    public boolean delete(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, Comment.class);

        URL url = new URL(BASE_URL + "/comment/delete");
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
        String jsonBody = gson.toJson(this, Post.class);

        URL url = new URL(BASE_URL + "/comment/edit");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public int getID() {return id;}
    public Post getPost() {return post;}
    public User getAuthor() {return author;}
    public int getParent() {return parentID;}
    public String getContent() {return content;}
    public Media getMedia() {return media;}
    public Timestamp getTimeCreated() {return timeCreated;}
    public int getVotes() {return votes;}
    public int getReplyCount() {return replyCount;}

    public void setId(int id) {this.id = id;}
    public void setContent(String content) {this.content = content;}
    public void setMedia(Media media) {this.media = media;}

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Comment) {
            return this.id == ((Comment) obj).id;
        }
        return false;
    }
}
