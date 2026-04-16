package service;

import model.URL;
import repository.URLRepository;
import utils.Base62Encoder;
import java.time.LocalDateTime;

public class URLShortenerService {
    private URLRepository repo;
    private int counter;

    public URLShortenerService(URLRepository repo) {
        this.repo = repo;
        this.counter = 10000 + repo.getAll().size();
    }

    public String shortenURL(String longUrl, String customAlias, Integer expiryHours, Integer maxClicks, String creator) {
        if (!longUrl.startsWith("http")) longUrl = "https://" + longUrl;

        String shortCode = (customAlias != null && !customAlias.isEmpty()) ? customAlias : Base62Encoder.encode(counter++);
        
        // Prevent duplicate aliases
        if (repo.find(shortCode) != null && (customAlias != null)) return null; 

        URL url = new URL(shortCode, longUrl);
        if (expiryHours != null) url.setExpiryTime(LocalDateTime.now().plusHours(expiryHours));
        if (maxClicks != null) url.setMaxClicks(maxClicks);
        url.setCreator(creator);
        
        repo.save(url);
        return shortCode;
    }

    public String redirect(String shortCode, String userAgent) {
        URL url = repo.find(shortCode);
        if (url != null) {
            if (url.isExpired()) return "EXPIRED";
            
            String deviceType = detectDevice(userAgent);
            url.recordClick(deviceType);
            repo.save(url); // Persist updated click count and stats
            return url.getLongUrl();
        }
        return null;
    }

    public boolean deleteURL(String shortCode) {
        if (repo.find(shortCode) != null) {
            repo.delete(shortCode);
            return true;
        }
        return false;
    }

    private String detectDevice(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.toLowerCase().contains("mobile")) return "Mobile";
        return "Desktop";
    }

    public URL getUrlDetails(String shortCode) {
        return repo.find(shortCode);
    }
}
