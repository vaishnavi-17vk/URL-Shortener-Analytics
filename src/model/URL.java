package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URL {
    private String shortCode;
    private String longUrl;
    private int clickCount;
    private List<LocalDateTime> clickTimestamps;
    
    // Advanced Features
    private LocalDateTime expiryTime;
    private Integer maxClicks; // null means unlimited
    private String creator; // User or Admin
    private Map<String, Integer> deviceStats; // e.g., "Mobile" -> 10, "Desktop" -> 5
    private boolean isSafe;

    public URL(String shortCode, String longUrl) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.clickCount = 0;
        this.clickTimestamps = new ArrayList<>();
        this.deviceStats = new HashMap<>();
        this.isSafe = true; // Default to safe
    }

    public void recordClick(String deviceType) {
        clickCount++;
        clickTimestamps.add(LocalDateTime.now());
        deviceStats.put(deviceType, deviceStats.getOrDefault(deviceType, 0) + 1);
    }

    public boolean isExpired() {
        if (expiryTime != null && LocalDateTime.now().isAfter(expiryTime)) return true;
        if (maxClicks != null && clickCount >= maxClicks) return true;
        return false;
    }

    // Getters and Setters
    public String getShortCode() { return shortCode; }
    public String getLongUrl() { return longUrl; }
    public int getClickCount() { return clickCount; }
    public List<LocalDateTime> getClickTimestamps() { return clickTimestamps; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
    public Integer getMaxClicks() { return maxClicks; }
    public void setMaxClicks(Integer maxClicks) { this.maxClicks = maxClicks; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public Map<String, Integer> getDeviceStats() { return deviceStats; }
    public boolean isSafe() { return isSafe; }
    public void setSafe(boolean safe) { isSafe = safe; }
}
