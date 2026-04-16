package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractLink implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String shortCode;
    private String longUrl;
    private int clickCount;
    private List<LocalDateTime> clickTimestamps;
    private Map<String, Integer> deviceStats;
    private String creator;
    private boolean isSafe;
    
    // Encapsulation: Private password
    private String password;

    public AbstractLink(String shortCode, String longUrl) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.clickCount = 0;
        this.clickTimestamps = new ArrayList<>();
        this.deviceStats = new HashMap<>();
        this.isSafe = true;
    }

    public void recordClick(String deviceType) {
        clickCount++;
        clickTimestamps.add(LocalDateTime.now());
        deviceStats.put(deviceType, deviceStats.getOrDefault(deviceType, 0) + 1);
    }

    // Abstract method to be implemented by child classes
    public abstract boolean isExpired();

    // Password check
    public boolean checkPassword(String input) {
        if (this.password == null || this.password.isEmpty()) return true;
        return this.password.equals(input);
    }

    // Getters and Setters
    public String getShortCode() { return shortCode; }
    public String getLongUrl() { return longUrl; }
    public int getClickCount() { return clickCount; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public void setPassword(String password) { this.password = password; }
    public String getPassword() { return password; }
    public boolean isSafe() { return isSafe; }
    public void setSafe(boolean safe) { isSafe = safe; }
    public Map<String, Integer> getDeviceStats() { return deviceStats; }
}
