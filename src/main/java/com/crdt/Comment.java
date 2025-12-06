package com.crdt;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Comment implements Voteable, Reportable {
    private int id;
    private Post post;
    private User author;
    private Comment parent;
    private String content;
    private Media media;
    private int votes;
    private Timestamp timeCreated;
    private Timestamp timeEdited;

    public Comment(int id, Post post, User author, String content, Media media, Comment parent, int votes, Timestamp createTime, Timestamp editTime) {
        this.id = id;
        this.post = post;
        this.author = author;
        this.content = content;
        this.media = media;
        this.parent = parent;
        this.votes = votes;
        this.timeCreated = createTime;
        this.timeEdited = editTime;
    }

    public int getID() {return id;}
    public Post getPost() {return post;}
    public User getAuthor() {return author;}
    public Comment getParent() {return parent;}
    public String getContent() {return content;}
    public Media getMedia() {return media;}

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Comment) {
            return this.id == ((Comment) obj).id;
        }
        return false;
    }
}
