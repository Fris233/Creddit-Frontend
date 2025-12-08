package com;

import com.crdt.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class Client {

    private static Gson gson ;
    private static String BASE_URL;

    public static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void init() {
        RuntimeTypeAdapterFactory<User> userAdapter =
                RuntimeTypeAdapterFactory.of(User.class, "type")
                        .registerSubtype(User.class, "user")
                        .registerSubtype(Moderator.class, "moderator")
                        .registerSubtype(Admin.class, "admin");
        RuntimeTypeAdapterFactory<Reportable> reportableAdapter =
                RuntimeTypeAdapterFactory.of(Reportable.class, "type")
                        .registerSubtype(User.class, "user")
                        .registerSubtype(Post.class, "post")
                        .registerSubtype(Comment.class, "comment");
        RuntimeTypeAdapterFactory<Voteable> voteableAdapter =
                RuntimeTypeAdapterFactory.of(Voteable.class, "type")
                        .registerSubtype(Post.class, "post")
                        .registerSubtype(Comment.class, "comment");
        gson = new GsonBuilder().registerTypeAdapterFactory(userAdapter).registerTypeAdapterFactory(reportableAdapter).registerTypeAdapterFactory(voteableAdapter).create();
        BASE_URL = System.getenv("BASE_URL");
    }

    public static void cleanup() {
        THREAD_POOL.shutdown();
        try {
            if (!THREAD_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                THREAD_POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            THREAD_POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    //BOOKMARK: Helpers

    public static boolean isServerReachable() {
        try {
            URL url = new URL(BASE_URL + "/ping");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    return "yes".equals(in.readLine());
                }
            }
        } catch (IOException e) {
            System.out.println("Server unreachable: " + e.getMessage());
        }
        return false;
    }

    public static Map<?,?> GetResponse(String uploadResponse) {
        Map<?, ?> json = gson.fromJson(uploadResponse, Map.class);
        return json;
    }

    public static String GetJSON(Object obj) {
        return gson.toJson(obj);
    }

    public static String UploadFile(File file) throws Exception {
        String boundary = "----Boundary" + System.currentTimeMillis();
        URL url = new URL(BASE_URL + "/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
            out.writeBytes("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n\r\n");
            Files.copy(file.toPath(), out);
            out.writeBytes("\r\n--" + boundary + "--\r\n");
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();
        return response.toString();
    }




    //BOOKMARK: User

    public static User GetUser(int id) throws Exception {
        URL url = new URL(BASE_URL + String.format("/user?id=%s", java.net.URLEncoder.encode(String.valueOf(id), "UTF-8")));
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

    public static User login(String usermail, String password) throws Exception {
        return User.login(usermail, password, BASE_URL, gson);
    }

    public static void keepAlive(User user) {
        if(user == null)
            return;
        try {
            user.keepAlive(BASE_URL, gson);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean register(User user) throws Exception {
        if(user == null)
            return false;
        return user.register(BASE_URL, gson);
    }

    public static int CheckVote(User user, Voteable voteable) throws Exception {
        return user.CheckVote(voteable, BASE_URL, gson);
    }

    public static boolean Vote(User user, Voteable voteable, int value) {
        THREAD_POOL.submit(() -> {
            try {
                user.Vote(voteable, value, BASE_URL, gson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    public static ArrayList<User> GetFriends(User user) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetFriends(BASE_URL, gson)));
    }

    public static ArrayList<User> GetSentFriendRequests(User user) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetSentFriendRequests(BASE_URL, gson)));
    }

    public static ArrayList<User> GetReceivedFriendRequests(User user) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetReceivedFriendRequests(BASE_URL, gson)));
    }

    public static boolean SendFriendRequest(User sender, User receiver) throws Exception {
        return sender.sendFriendRequest(receiver, BASE_URL, gson);
    }

    public static boolean AcceptFriendRequest(User sender, User receiver) throws Exception {
        return receiver.acceptFriend(sender, BASE_URL, gson);
    }

    public static boolean Unfriend(User me, User friend) throws Exception {
        return me.unfriend(friend, BASE_URL, gson);
    }

    public static ArrayList<Subcreddit> GetUserSubcreddits(User user) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetSubcreddits(BASE_URL, gson)));
    }

    public static Map<Post, Integer> GetPostFeed(User user, String prompt, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.addProperty("prompt", prompt);
        json.addProperty("lastID", lastID);

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/post/feed");
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
        Post[] posts = gson.fromJson(jsonObj.get("posts"), Post[].class);
        Integer[] myVotes = gson.fromJson(jsonObj.get("votes"), Integer[].class);

        Map<Post, Integer> postMap = new LinkedHashMap<>();
        int sz = posts.length;
        for(int i = 0; i < sz; i++)
            postMap.put(posts[i], myVotes[i]);

        return postMap;
    }

    public static Map<Post, Integer> GetPostFeedFilterSub(User user, Subcreddit sub, String prompt, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.add("sub", gson.toJsonTree(sub));
        json.addProperty("prompt", prompt);
        json.addProperty("lastID", gson.toJson(lastID));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/post/feed/filter-sub");
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
        Post[] posts = gson.fromJson(jsonObj.get("posts"), Post[].class);
        Integer[] myVotes = gson.fromJson(jsonObj.get("votes"), Integer[].class);

        Map<Post, Integer> postMap = new LinkedHashMap<>();
        int sz = posts.length;
        for(int i = 0; i < sz; i++)
            postMap.put(posts[i], myVotes[i]);

        return postMap;
    }

    public static Map<Post, Integer> GetPostFeedFilterAuthor(User user, User author, String prompt, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.add("author", gson.toJsonTree(author, User.class));
        json.addProperty("prompt", prompt);
        json.addProperty("lastID", gson.toJson(lastID));

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/post/feed/filter-author");
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
        Post[] posts = gson.fromJson(jsonObj.get("posts"), Post[].class);
        Integer[] myVotes = gson.fromJson(jsonObj.get("votes"), Integer[].class);

        Map<Post, Integer> postMap = new LinkedHashMap<>();
        int sz = posts.length;
        for(int i = 0; i < sz; i++)
            postMap.put(posts[i], myVotes[i]);

        return postMap;
    }




    //BOOKMARK: Post

    public static boolean CreatePost(Post post) throws Exception {
        return post.create(BASE_URL, gson);
    }

    public static String[] GetAllCategories() throws Exception {
        URL url = new URL(BASE_URL + "/category/all");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return gson.fromJson(sb.toString(), String[].class);
    }
}