package model;

import java.time.LocalDateTime;

public class TemporaryLink extends AbstractLink {
    private static final long serialVersionUID = 1L;
    
    private LocalDateTime expiryDate;
    private Integer maxClicks;

    public TemporaryLink(String shortCode, String longUrl, LocalDateTime expiryDate, Integer maxClicks) {
        super(shortCode, longUrl);
        this.expiryDate = expiryDate;
        this.maxClicks = maxClicks;
    }

    @Override
    public boolean isExpired() {
        if (expiryDate != null && LocalDateTime.now().isAfter(expiryDate)) return true;
        if (maxClicks != null && getClickCount() >= maxClicks) return true;
        return false;
    }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    public Integer getMaxClicks() { return maxClicks; }
    public void setMaxClicks(Integer maxClicks) { this.maxClicks = maxClicks; }
}
