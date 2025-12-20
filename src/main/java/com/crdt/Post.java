package com.crdt;

import com.Client;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;

public class Post implements Voteable, Reportable {
    private int id;
    private User author;
    private Subcreddit subcreddit;
    private String title;
    private String content;
    private ArrayList<Media> media;
    private ArrayList<String> categories;
    private Timestamp timeCreated;
    private Timestamp timeEdited;
    private int votes;
    private int comments;

    private static Type commentMapType = new TypeToken<Map<Integer, Comment[]>>() {}.getType();
    private static Type id_vote_type = new TypeToken<Map<Integer, Integer>>() {}.getType();

    public Post(int id, User author, Subcreddit subcreddit, String title, String content, ArrayList<Media> media, ArrayList<String> categories, Timestamp timeCreated, Timestamp timeEdited, int votes, int comments) {
        if (id < 0)
            return;

        this.id = id;
        this.author = author;
        this.subcreddit = subcreddit;
        this.title = title;
        this.content = content;
        this.media = media;
        this.categories = categories;
        this.timeCreated = timeCreated;
        this.timeEdited = timeEdited;
        this.votes = votes;
        this.comments = comments;
    }

    public int create(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, Post.class);

        URL url = new URL(BASE_URL + "/post/create");
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
        String jsonBody = gson.toJson(this, Post.class);

        URL url = new URL(BASE_URL + "/post/delete");
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

        URL url = new URL(BASE_URL + "/post/edit");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public CommentFeed GetCommentFeed(User user, int lastID, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.add("post", gson.toJsonTree(this, Post.class));
        json.add("lastID", gson.toJsonTree(lastID, int.class));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/post/comment/feed");
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

        JsonObject jsonObj = gson.fromJson(sb.toString(), JsonObject.class);
        Comment[] parents = gson.fromJson(jsonObj.get("parents"), Comment[].class);
        Map<Integer, Comment[]> lv2 = gson.fromJson(jsonObj.get("lv2"), commentMapType);
        Map<Integer, Comment[]> lv3 = gson.fromJson(jsonObj.get("lv3"), commentMapType);
        Map<Integer, Integer> votes = gson.fromJson(jsonObj.get("votes"), id_vote_type);

        return new CommentFeed(parents, lv2, lv3, votes);
    }

    public int GetID() {
        return id;
    }
    public void SetID(int id) {this.id = id;}

    public User GetAuthor() {
        return author;
    }

    public Subcreddit GetSubcreddit() {
        return subcreddit;
    }

    public String GetTitle() {
        return title;
    }
    public void setTitle(String title) {this.title = title;}

    public String GetContent() {
        return content;
    }
    public void setContent(String content) {this.content = content;}

    public ArrayList<Media> GetMedia() {
        return media;
    }
    public void setMedia(ArrayList<Media> media) {this.media = media;}

    public ArrayList<String> GetCategories() {
        return categories;
    }
    public void setCategories(ArrayList<String> categories) {this.categories = categories;}

    public Timestamp GetTimeCreated() {
        return timeCreated;
    }

    public Timestamp GetTimeEdited() {
        return timeEdited;
    }

    public int GetVotes() {
        return votes;
    }
    public int GetComments() {
        return comments;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Post) {
            return this.id == ((Post) obj).id;
        }
        return false;
    }
}