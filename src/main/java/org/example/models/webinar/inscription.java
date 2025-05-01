package org.example.models.webinar;


public class inscription {
    private int id;
    private int webinarId;
    private int userId;

    // Constructors
    public inscription() {}

    public inscription(int id, int webinarId, int userId) {
        this.id = id;
        this.webinarId = webinarId;
        this.userId = userId;
    }

    public inscription(int webinarId, int userId) {
        this.webinarId = webinarId;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWebinarId() {
        return webinarId;
    }

    public void setWebinarId(int webinarId) {
        this.webinarId = webinarId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}

