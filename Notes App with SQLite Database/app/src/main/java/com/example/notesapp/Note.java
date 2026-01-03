package com.example.notesapp;

public class Note {
    private int id;
    private String title;
    private String content;
    private String timestamp;
    private boolean isPinned;

    public Note(int id, String title, String content, String timestamp, boolean isPinned) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.isPinned = isPinned;
    }

    public Note(String title, String content, String timestamp, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.isPinned = isPinned;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }
}
