package com.crdt;

import java.sql.Timestamp;

public class Moderator extends User {

    public Moderator(int id, String userName, String email, String password, Gender gender, String bio, Media profileMedia, Timestamp joinDate, Timestamp lastSeen, boolean active) {
        super(id, userName, email, password, gender, bio, profileMedia, joinDate, lastSeen, active);
    }

    public void BanMember(User user, Subcreddit subcreddit, String reason) {
        if(user.id <= 0)
            return;
        boolean global = (subcreddit == null);
        if(!global)
            if(!VerifyModeration(subcreddit))
                return;
    }

    public void UnbanMember(User user, Subcreddit subcreddit) {
        if(user.id <= 0)
            return;
        boolean global = (subcreddit == null);
        if(!global)
            if(!VerifyModeration(subcreddit))
                return;
    }

    public boolean VerifyModeration(Subcreddit subcreddit) {
        if(subcreddit.GetSubId() <= 0)
            return false;
        return false;
    }
}
