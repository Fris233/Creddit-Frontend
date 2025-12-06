package com.crdt;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Subcreddit {
    private int id;
    private String name ;
    private String description ;
    private Timestamp timecreated;
    private User creator;
    private Media subLogo;
    private boolean isPrivate;


    public Subcreddit(int id, String name, String description, Timestamp timecreated, User creator,  Media logo, boolean isPrivate){
        this.id = id;
        this.name = name;
        this.timecreated = timecreated;
        this.isPrivate = isPrivate;
        this.creator = creator;
        this.subLogo = logo;
    }

    public void create() {
    }

    public void delete() {
    }

    public ArrayList<User> GetMembers() {
        ArrayList<User> members = new ArrayList<>();
        return members;
    }

    public ArrayList<User> GetBannedMembers() {
        if(this.id <= 0)
            return null;

        ArrayList<User> bannedMembers = new ArrayList<>();
        return bannedMembers;
    }

    public int GetSubId() {
        return id;
    }

    public String GetSubName() {
        return name;
    }

    public String GetDescription() {
        return description;
    }

    public Timestamp GetTimecreated() {
        return timecreated;
    }

    public User GetCreator() {
        return creator;
    }

     public Media GetLogo() {
        return subLogo;
    }

    public boolean GetPrivate() {
        return isPrivate;
    }

    void UpdateDescription (String description){
    }

     void UpdateLogo (Media logo){
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Subcreddit) {
            return this.id == ((Subcreddit) obj).id;
        }
        return false;
    }
}

