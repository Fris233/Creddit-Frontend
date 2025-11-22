package com;

import com.crdt.Admin;
import com.crdt.Post;
import com.crdt.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Client {

    private static Gson gson ;
    private static String BASE_URL;

    public static void init() {
        RuntimeTypeAdapterFactory<User> userAdapter =
                RuntimeTypeAdapterFactory.of(User.class, "type")
                        .registerSubtype(User.class, "user")
                        .registerSubtype(Admin.class, "admin");
        gson = new GsonBuilder().registerTypeAdapterFactory(userAdapter).create();
        BASE_URL = System.getenv("BASE_URL");
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




    //BOOKMARK: User

    public static User GetUser(int id) throws Exception {
        URL url = new URL(BASE_URL + String.format("/user?id=%s", java.net.URLEncoder.encode(String.valueOf(id), "UTF-8")));
        System.out.println(url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        User user = gson.fromJson(sb.toString(), User.class);
        System.out.println(user.getId());
        System.out.println(user.getUsername());
        return user;
    }

    public static User login(String usermail, String password) throws Exception {
        return User.login(usermail, password, BASE_URL, gson);
    }

    public static int CheckVote(User user, Post post) throws Exception {
        return user.CheckVote(post, BASE_URL, gson);
    }

    public static boolean Vote(User user, Post post, int value) throws Exception {
        return user.Vote(post, value, BASE_URL, gson);
    }

    public static Map<Post, Integer> GetPostFeed(User user, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user));
        json.addProperty("lastID", gson.toJson(lastID));

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
}