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
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static ArrayList<Post> GetPostFeed(User user, int lastID) throws Exception {
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

        Post[] postsArray = gson.fromJson(sb.toString(), Post[].class);
        ArrayList<Post> posts = new ArrayList<>(Arrays.asList(postsArray));
        for(Post post : posts)
            System.out.println(post.GetID());

        return posts;
    }

}
