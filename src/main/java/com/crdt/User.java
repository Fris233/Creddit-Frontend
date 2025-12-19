package com.crdt;

import com.Client;
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
    protected Timestamp lastSeen;
    protected boolean active;

    public User(int id, String username, String email, String password, Gender gender, String bio, Media pfp, Timestamp timeCreated, Timestamp lastSeen, boolean active) {
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
        this.lastSeen = lastSeen;
        this.active = active;
    }

    public boolean register(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, User.class);
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

        if(conn.getResponseCode() == 500 || conn.getResponseCode() == 233) {
            Map<?,?> map = gson.fromJson(sb.toString(), Map.class);
            if(map.get("status").equals("error")) {
                throw new Exception(String.valueOf(map.get("message")));
            }
        }

        return gson.fromJson(sb.toString(), User.class);
    }

    public boolean update(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, User.class);

        URL url = new URL(BASE_URL + "/user/update");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public boolean keepAlive(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, User.class);
        URL url = new URL(BASE_URL + "/user/keepalive");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
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

    public boolean Vote(Voteable voteable, int value, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(this, User.class));
        json.add("voteable", gson.toJsonTree(voteable, Voteable.class));
        json.addProperty("value", gson.toJson(value));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/vote");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public int CheckVote(Voteable voteable, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(this, User.class));
        json.add("voteable", gson.toJsonTree(voteable, Voteable.class));

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

    public boolean joinSubcreddit(Subcreddit subcreddit, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(this, User.class));
        json.add("subcreddit", gson.toJsonTree(subcreddit, Subcreddit.class));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/subcreddit/join");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public boolean leaveSubcreddit(Subcreddit subcreddit, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(this, User.class));
        json.add("subcreddit", gson.toJsonTree(subcreddit, Subcreddit.class));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/subcreddit/leave");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public boolean isMember(Subcreddit subcreddit, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(this, User.class));
        json.add("subcreddit", gson.toJsonTree(subcreddit, Subcreddit.class));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/user/ismember");
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

        return gson.fromJson(sb.toString(), boolean.class);
    }

    public Subcreddit[] GetSubcreddits(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, User.class);
        URL url = new URL(BASE_URL + "/user/subcreddits");
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

        return gson.fromJson(sb.toString(), Subcreddit[].class);
    }

    public boolean sendFriendRequest(User receiver, String BASE_URL, Gson gson) throws Exception {
        if(!this.active)
            return false;
        JsonObject json = new JsonObject();
        json.add("sender", gson.toJsonTree(this, User.class));
        json.add("receiver", gson.toJsonTree(receiver, User.class));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/friends/send");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public boolean acceptFriend(User sender, String BASE_URL, Gson gson) throws Exception {
        if(!this.active)
            return false;
        JsonObject json = new JsonObject();
        json.add("sender", gson.toJsonTree(sender, User.class));
        json.add("receiver", gson.toJsonTree(this, User.class));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/friends/accept");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public boolean unfriend(User friend, String BASE_URL, Gson gson) throws Exception {
        if(!this.active)
            return false;
        JsonObject json = new JsonObject();
        json.add("user1", gson.toJsonTree(this, User.class));
        json.add("user2", gson.toJsonTree(friend, User.class));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/friends/remove");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    public User[] GetFriends(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, User.class);
        URL url = new URL(BASE_URL + "/friends");
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

        return gson.fromJson(sb.toString(), User[].class);
    }

    public User[] GetSentFriendRequests(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, User.class);
        URL url = new URL(BASE_URL + "/friends/sent");
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

        return gson.fromJson(sb.toString(), User[].class);
    }

    public User[] GetReceivedFriendRequests(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, User.class);
        URL url = new URL(BASE_URL + "/friends/received");
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

        return gson.fromJson(sb.toString(), User[].class);
    }

    public Message[] GetPrivateMessageFeed(User friend, int lastMessageID, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(this, User.class));
        json.add("friend", gson.toJsonTree(friend, User.class));
        json.addProperty("lastID", gson.toJson(lastMessageID, int.class));

        String jsonBody = gson.toJson(json);
        URL url = new URL(BASE_URL + "/pm/feed");
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

        return gson.fromJson(sb.toString(), Message[].class);
    }

    public ArrayList<Message> GetLatestPrivateMessages(User friend, int lastMessageID) {
        ArrayList<Message> messages = new ArrayList<>();
        return messages;
    }

    public Message[] GetUnreadPrivateMessages(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, User.class);
        URL url = new URL(BASE_URL + "/pm/unread");
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

        return gson.fromJson(sb.toString(), Message[].class);
    }

    public boolean ReadMessages(User friend, String BASE_URL, Gson gson) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(this, User.class));
        json.add("friend", gson.toJsonTree(friend, User.class));

        String jsonBody = gson.toJson(json);
        URL url = new URL(BASE_URL + "/pm/read");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }
        return conn.getResponseCode() == 200;
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank() || username.length() > 32)
            return;
        this.username = username;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 8 || password.isBlank() || password.length() > 16)
            return;
        this.password = password;
    }

    public void setBio(String bio) {
        if (bio == null)
            return;
        this.bio = bio;
    }

    public void setPFP(Media pfp) {
        this.pfp = pfp;
    }

    public int getId() {return this.id;}
    public String getUsername() {return this.username;}
    public String getEmail() {return this.email;}
    public String getPassword() {return this.password;}
    public Gender getGender() {return this.gender;}
    public String getBio() {return this.bio;}
    public Media getPfp() {return this.pfp;}
    public Timestamp getTimeCreated() {return this.timeCreated;}
    public Timestamp getLastSeen() {return this.lastSeen;}
    public boolean getActive() {return this.active;}

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof User) {
            return this.id == ((User) obj).id;
        }
        return false;
    }
}
