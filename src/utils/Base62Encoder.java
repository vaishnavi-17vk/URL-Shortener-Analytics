package utils;

public class Base62Encoder {
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String encode(int num) {
        if (num == 0) return String.valueOf(CHARSET.charAt(0));
        
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(CHARSET.charAt(num % 62));
            num /= 62;
        }
        return sb.reverse().toString();
    }
}
