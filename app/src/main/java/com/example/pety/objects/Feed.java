package com.example.pety.objects;

import java.util.HashMap;
import java.util.Map;

public class Feed {
    private String id;
    private String time;
    private boolean isActive;

    public Feed() {
    }

    public Feed(String time, boolean isActive) {
        this.time = time;
        this.isActive = isActive;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id",id);
        result.put("time", time);
        result.put("isActive", isActive);
        return result;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
