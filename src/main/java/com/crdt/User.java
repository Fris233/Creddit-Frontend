package com.crdt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

public class User implements Reportable {
    protected int id;
    protected String username;
    protected String email;
    protected String password;
    protected Gender gender;
    protected String bio;
    protected Media pfp;
    protected Timestamp timeCreated;
    protected boolean active;

    public User(int id, String username, String email, String password, Gender gender, String bio, Media pfp, Timestamp timeCreated, boolean active) {
        if (id < 0)
            return;
        if (username == null || username.isEmpty() || username.length() > 32)
            return;
        if(!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") || email.length() > 255)
            return;
        /*if (password == null || password.length() < 8 || password.length() > 32)
            return;*/
        if (gender == null)
            return;

        this.username = username;
        this.id = id;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.bio = bio;
        this.pfp = pfp;
        this.timeCreated = timeCreated;
        this.active = active;
    }

    public boolean register(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this);
        URL url = new URL(BASE_URL + "/user/register");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public static User login(String usermail, String password, String BASE_URL, Gson gson) throws Exception {
        URL url = new URL(BASE_URL + String.format("/user/login?usermail=%s&password=%s", java.net.URLEncoder.encode(usermail, "UTF-8"), java.net.URLEncoder.encode(password, "UTF-8")));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        User user = gson.fromJson(sb.toString(), User.class);
        return user;
    }

    public void update() {
        if(!this.active)
            return;
    }

    public void delete() {
        if(!this.active)
            return;
    }

    public void activate() { //todo put in admin
        if(this.active)
            return;
    }

    public void viewPost(Post post) {
        if(!this.active || post == null || post.GetID() <= 0)
            return;
    }

    public void sharePost(Post post) {
    }

    public void savePost(Post post) {
    }

    public boolean Vote(Post post, int value, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(this));
        json.add("post", gson.toJsonTree(post));
        json.addProperty("value", gson.toJson(value));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/post/vote");
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

        Map<?,?> map = gson.fromJson(sb.toString(), Map.class);
        return map.get("status").equals("ok");
    }

    public int CheckVote(Post post, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(this));
        json.add("post", gson.toJsonTree(post));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/user/checkvote");
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

    public void joinSubcreddit(Subcreddit subcreddit) {
        if(!this.active || subcreddit == null || subcreddit.GetSubId() <= 0)
            return;
    }

    public void leaveSubcreddit(Subcreddit subcreddit) {
        if(!this.active || subcreddit == null || subcreddit.GetSubId() <= 0)
            return;
    }

    public ArrayList<Subcreddit> GetSubcreddits() {
        ArrayList<Subcreddit> subcreddits = new ArrayList<>();
        return subcreddits;
    }

    public void sendFriendRequest(User user) {
        if(!this.active)
            return;
    }

    public void unfriend(User friend) {
        if(!this.active)
            return;
    }

    public ArrayList<User> GetFriends() {
        ArrayList<User> friends = new ArrayList<>();
        return friends;
    }

    public ArrayList<User> GetSentFriendRequests() {
        ArrayList<User> friends = new ArrayList<>();
        return friends;
    }

    public ArrayList<User> GetReceivedFriendRequests() {
        ArrayList<User> friends = new ArrayList<>();
        return friends;
    }

    public ArrayList<Message> GetPrivateMessageFeed(User friend, int lastMessageID) {
        ArrayList<Message> messages = new ArrayList<>();
        return messages;
    }

    public ArrayList<Message> GetLatestPrivateMessages(User friend, int lastMessageID) {
        ArrayList<Message> messages = new ArrayList<>();
        return messages;
    }

    public void addReport(Report report) {
    }

    public void vote(Voteable voteable, int voteValue) { // voteValue -> {1: upvote, -1: downvote, 0: remove vote}
        if(!this.active || voteable == null || (voteValue != -1 && voteValue != 1 && voteValue != 0))
            return;
    }

    public void setUsername(String username) {
        if (username == null || username.isEmpty() || username.length() > 32)
            return;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 16)
            return;
    }

    public void setBio(String bio) {
        if (bio == null)
            return;
    }

    public void setPFP(Media pfp) {
    }

    public int getId() {return this.id;}
    public String getUsername() {return this.username;}
    public String getEmail() {return this.email;}
    public String getPassword() {return this.password;}
    public Gender getGender() {return this.gender;}
    public String getBio() {return this.bio;}
    public Media getPfp() {return this.pfp;}
    public Timestamp getTimeCreated() {return this.timeCreated;}
    public boolean getActive() {return this.active;}
}
