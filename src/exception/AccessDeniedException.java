package exception;

public class AccessDeniedException extends LinkStreamException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
