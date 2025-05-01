package org.example.models.webinar;

import java.time.LocalDateTime;

public class webinar {
    private int id;
    private Integer presenterId;
    private String title;
    private String description;
    private LocalDateTime debut;
    private int duration;
    private String category;
    private String tags;
    private boolean registrationRequired;
    private int maxAttendees;
    private String platform;
    private String lien;
    private boolean recordingAvailable;

    // Constructors
    public webinar() {}

    public webinar(int id, Integer presenterId, String title, String description, LocalDateTime debut,
                   int duration, String category, String tags, boolean registrationRequired,
                   int maxAttendees, String platform, String lien, boolean recordingAvailable) {
        this.id = id;
        this.presenterId = presenterId;
        this.title = title;
        this.description = description;
        this.debut = debut;
        this.duration = duration;
        this.category = category;
        this.tags = tags;
        this.registrationRequired = registrationRequired;
        this.maxAttendees = maxAttendees;
        this.platform = platform;
        this.lien = lien;
        this.recordingAvailable = recordingAvailable;
    }

    // Getters and setters (generate or write manually)
    // Only showing a few for brevity — generate the rest using your IDE

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getPresenterId() { return presenterId; }
    public void setPresenterId(Integer presenterId) { this.presenterId = presenterId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDebut() { return debut; }
    public void setDebut(LocalDateTime debut) { this.debut = debut; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isRegistrationRequired() { return registrationRequired; }
    public void setRegistrationRequired(boolean registrationRequired) { this.registrationRequired = registrationRequired; }

    public int getMaxAttendees() { return maxAttendees; }
    public void setMaxAttendees(int maxAttendees) { this.maxAttendees = maxAttendees; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getLien() { return lien; }
    public void setLien(String lien) { this.lien = lien; }

    public boolean isRecordingAvailable() { return recordingAvailable; }
    public void setRecordingAvailable(boolean recordingAvailable) { this.recordingAvailable = recordingAvailable; }
}
