package com.crdt;

import java.sql.Timestamp;

public class Admin extends Moderator {
    public Admin(int id, String username, String email, String password, Gender gender, String bio, Media pfp, Timestamp timeCreated, Timestamp lastSeen, boolean active) {
        super(id, username, email, password, gender, bio, pfp, timeCreated, lastSeen, active);
    }

    public void BanUser(User user, String reason) {
        BanMember(user, null, reason);
        user.delete();
    }

    public void UnbanUser(User user) {
        UnbanMember(user, null);
        user.activate();
    }
}
