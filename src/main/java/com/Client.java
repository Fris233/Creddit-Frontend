package com;

import com.crdt.Admin;
import com.crdt.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public abstract class Client {

    private static Gson gson ;

    public static void init() {
        RuntimeTypeAdapterFactory<User> userAdapter =
                RuntimeTypeAdapterFactory.of(User.class, "type")
                        .registerSubtype(User.class, "user")
                        .registerSubtype(Admin.class, "admin");
        gson = new GsonBuilder().registerTypeAdapterFactory(userAdapter).create();
    }

    public static boolean isServerReachable(String baseUrl) {
        try {
            URL url = new URL(baseUrl + "/ping");
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
        URL url = new URL(System.getenv("Base_URL") + String.format("/user?id=%s", java.net.URLEncoder.encode(String.valueOf(id), "UTF-8")));
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

}
