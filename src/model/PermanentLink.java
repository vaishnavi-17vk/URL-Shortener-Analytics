package model;

public class PermanentLink extends AbstractLink {
    private static final long serialVersionUID = 1L;

    public PermanentLink(String shortCode, String longUrl) {
        super(shortCode, longUrl);
    }

    @Override
    public boolean isExpired() {
        return false;
    }
}
