package com.crdt;

import java.sql.Timestamp;
import java.util.ArrayList;

public class User implements Reportable {
    protected int id;
    protected String username;
    protected String email;
    protected String password;
    protected Gender gender;
    protected String bio;
    protected Media pfp;
    protected Timestamp timeCreated;
    protected boolean active;

    public User(int id, String username, String email, String password, Gender gender, String bio, Media pfp, Timestamp timeCreated, boolean active) {
        if (id <= 0)
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
        this.active = active;
    }

    public void register() {
    }

    public static User login(String s, String p) {
        return null;
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

    public void joinSubcreddit(Subcreddit subcreddit) {
        if(!this.active || subcreddit == null || subcreddit.GetSubId() <= 0)
            return;
    }

    public void leaveSubcreddit(Subcreddit subcreddit) {
        if(!this.active || subcreddit == null || subcreddit.GetSubId() <= 0)
            return;
    }

    public ArrayList<Subcreddit> GetSubcreddits() {
        ArrayList<Subcreddit> subcreddits = new ArrayList<>();
        return subcreddits;
    }

    public void sendFriendRequest(User user) {
        if(!this.active)
            return;
    }

    public void unfriend(User friend) {
        if(!this.active)
            return;
    }

    public ArrayList<User> GetFriends() {
        ArrayList<User> friends = new ArrayList<>();
        return friends;
    }

    public ArrayList<User> GetSentFriendRequests() {
        ArrayList<User> friends = new ArrayList<>();
        return friends;
    }

    public ArrayList<User> GetReceivedFriendRequests() {
        ArrayList<User> friends = new ArrayList<>();
        return friends;
    }

    public ArrayList<Message> GetPrivateMessageFeed(User friend, int lastMessageID) {
        ArrayList<Message> messages = new ArrayList<>();
        return messages;
    }

    public ArrayList<Message> GetLatestPrivateMessages(User friend, int lastMessageID) {
        ArrayList<Message> messages = new ArrayList<>();
        return messages;
    }

    public void addReport(Report report) {
    }

    public void vote(Voteable voteable, int voteValue) { // voteValue -> {1: upvote, -1: downvote, 0: remove vote}
        if(!this.active || voteable == null || (voteValue != -1 && voteValue != 1 && voteValue != 0))
            return;
    }

    public void setUsername(String username) {
        if (username == null || username.isEmpty() || username.length() > 32)
            return;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 16)
            return;
    }

    public void setBio(String bio) {
        if (bio == null)
            return;
    }

    public void setPFP(Media pfp) {
    }

    public int getId() {return this.id;}
    public String getUsername() {return this.username;}
    public String getEmail() {return this.email;}
    public String getPassword() {return this.password;}
    public Gender getGender() {return this.gender;}
    public String getBio() {return this.bio;}
    public Media getPfp() {return this.pfp;}
    public Timestamp getTimeCreated() {return this.timeCreated;}
    public boolean getActive() {return this.active;}
}
