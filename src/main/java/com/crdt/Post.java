package com.crdt;

import com.Client;
import com.google.gson.Gson;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;

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

    public Post(int id, User author, Subcreddit subcreddit, String title, String content, ArrayList<Media> media, ArrayList<String> categories, Timestamp timeCreated, Timestamp timeEdited, int votes, int comments) {
        if (id <= 0)
            return;

        if (title == null || title.isEmpty() || title.length() > 255)
            return;
        /*if(content == null || content.isEmpty())
            return;*/

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

    public boolean create(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = Client.GetJSON(this);

        URL url = new URL(BASE_URL + "/post/create");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public void delete() {
    }

    public int GetID() {
        return id;
    }

    public User GetAuthor() {
        return author;
    }

    public Subcreddit GetSubcreddit() {
        return subcreddit;
    }

    public String GetTitle() {
        return title;
    }

    public String GetContent() {
        return content;
    }

    public ArrayList<Media> GetMedia() {
        return media;
    }

    public ArrayList<String> GetCategories() {
        return categories;
    }

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