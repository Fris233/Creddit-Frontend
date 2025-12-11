package com.crdt;

import com.Client;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Message {
    private int id;
    private User sender;
    private User receiver;
    private String text;
    private Media media;
    private Timestamp create_time;
    private Timestamp edit_time;
    private boolean read;

    //Constructor
    public Message(int id, User sender, User receiver, String text, Media media, Timestamp create_time, Timestamp edit_time, boolean read){

        this.id=id;
        this.sender=sender;
        this.receiver=receiver;
        this.text=text;
        this.media=media;
        this.create_time=create_time;
        this.edit_time=edit_time;
        this.read=read;

    }

    //getters
    public int GetID(){return id;}
    public User GetSender(){return sender;}
    public User GetReceiver() {return receiver;}
    public String GetText() {return text;}
    public Media GetMedia() {return media;}
    public Timestamp GetCreate_time() {return create_time;}
    public Timestamp GetEdit_time() {return edit_time;}
    public boolean GetRead(){return read;}

    public boolean send(String BASE_URL, Gson gson) throws Exception {
        String jsonBody = gson.toJson(this, Message.class);

        URL url = new URL(BASE_URL + "/pm/send");
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
        String jsonBody = gson.toJson(this, Message.class);

        URL url = new URL(BASE_URL + "/pm/delete");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        return conn.getResponseCode() == 200;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Message) {
            return this.id == ((Message) obj).id;
        }
        return false;
    }
}
