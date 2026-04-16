package exception;

public class LinkExpiredException extends LinkStreamException {
    public LinkExpiredException(String message) {
        super(message);
    }
}
