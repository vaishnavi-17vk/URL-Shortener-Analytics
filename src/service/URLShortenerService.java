package service;

import model.*;
import repository.URLRepository;
import utils.Base62Encoder;
import exception.*;
import java.time.LocalDateTime;

public class URLShortenerService {
    private URLRepository repo;
    private int counter;

    public URLShortenerService(URLRepository repo) {
        this.repo = repo;
        this.counter = 10000 + repo.getRawMap().size();
    }

    public String shortenURL(String longUrl, String customAlias, Integer expiryHours, Integer maxClicks, String creator, String password) 
            throws InvalidURLException {
        
        if (longUrl == null || !longUrl.startsWith("http")) {
            throw new InvalidURLException("Invalid URL protocol");
        }

        String shortCode = (customAlias != null && !customAlias.isEmpty()) ? customAlias : Base62Encoder.encode(counter++);
        
        if (repo.find(shortCode) != null && (customAlias != null)) {
            throw new InvalidURLException("Alias taken");
        } 

        AbstractLink link;
        if (expiryHours != null || maxClicks != null) {
            LocalDateTime expiryDate = (expiryHours != null) ? LocalDateTime.now().plusHours(expiryHours) : null;
            link = new TemporaryLink(shortCode, longUrl, expiryDate, maxClicks);
        } else {
            link = new PermanentLink(shortCode, longUrl);
        }

        link.setCreator(creator);
        link.setPassword(password);
        
        repo.save(link);
        return shortCode;
    }

    public String redirect(String shortCode, String userAgent, String password) throws LinkStreamException {
        AbstractLink link = repo.find(shortCode);
        
        if (link == null) throw new LinkStreamException("Not found");
        if (link.isExpired()) throw new LinkExpiredException("Expired");
        if (!link.checkPassword(password)) throw new AccessDeniedException("Locked");

        link.recordClick(userAgent.toLowerCase().contains("mobile") ? "Mobile" : "Desktop");
        repo.save(link); 
        
        return link.getLongUrl();
    }

    public boolean deleteURL(String shortCode) {
        if (repo.find(shortCode) != null) {
            repo.delete(shortCode);
            return true;
        }
        return false;
    }
}
