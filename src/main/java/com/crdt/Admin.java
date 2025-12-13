package com.crdt;

import com.google.gson.Gson;

import java.sql.Timestamp;

public class Admin extends Moderator {
    public Admin(int id, String username, String email, String password, Gender gender, String bio, Media pfp, Timestamp timeCreated, Timestamp lastSeen, boolean active) {
        super(id, username, email, password, gender, bio, pfp, timeCreated, lastSeen, active);
    }

    public void BanUser(User user, String reason, String BASE_URL, Gson gson) throws Exception {
        BanMember(user, null, reason, BASE_URL, gson);
        user.delete();
    }

    public void UnbanUser(User user, String BASE_URL, Gson gson) throws Exception {
        UnbanMember(user, null, BASE_URL, gson);
        user.activate();
    }
}
