package com.crdt;

import com.google.gson.Gson;

import java.sql.Timestamp;

public class Moderator extends User {

    public Moderator(int id, String userName, String email, String password, Gender gender, String bio, Media profileMedia, Timestamp joinDate, Timestamp lastSeen, boolean active) {
        super(id, userName, email, password, gender, bio, profileMedia, joinDate, lastSeen, active);
    }

    public void BanMember(User user, Subcreddit subcreddit, String reason, String BASE_URL, Gson gson) throws Exception {
        if(user.id <= 0)
            return;
        boolean global = (subcreddit == null);
    }

    public void UnbanMember(User user, Subcreddit subcreddit, String BASE_URL, Gson gson) throws Exception {
        if(user.id <= 0)
            return;
        boolean global = (subcreddit == null);
    }
}
