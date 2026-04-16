import repository.URLRepository;
import service.URLShortenerService;
import controller.URLController;
import model.URL;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        URLRepository repo = new URLRepository();
        URLShortenerService service = new URLShortenerService(repo);
        URLController controller = new URLController(service);

        startWebServer(service, repo);
        controller.start();
    }

    private static void startWebServer(URLShortenerService service, URLRepository repo) {
        new Thread(() -> {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
                
                // 1. ADVANCED API: /api/shorten?url=...&alias=...&expiry=...&maxClicks=...
                server.createContext("/api/shorten", exchange -> {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    String longUrl = params.getOrDefault("url", "");
                    String alias = params.get("alias");
                    Integer expiry = params.containsKey("expiry") ? Integer.parseInt(params.get("expiry")) : null;
                    Integer maxClicks = params.containsKey("maxClicks") ? Integer.parseInt(params.get("maxClicks")) : null;
                    String creator = params.getOrDefault("creator", "User");
                    
                    if (!longUrl.isEmpty()) {
                        String code = service.shortenURL(longUrl, alias, expiry, maxClicks, creator);
                        if (code != null) {
                            String response = String.format("{\"shortUrl\": \"http://localhost:8081/r/%s\", \"code\": \"%s\"}", code, code);
                            sendResponse(exchange, 200, response);
                        } else {
                            sendResponse(exchange, 400, "{\"error\": \"Alias already taken\"}");
                        }
                    } else {
                        sendResponse(exchange, 400, "{\"error\": \"Invalid URL\"}");
                    }
                });

                // 2. ADMIN POV: Get all analytics
                server.createContext("/api/admin/stats", exchange -> {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    
                    StringBuilder json = new StringBuilder("[");
                    for (URL url : repo.getAll().values()) {
                        json.append(String.format("{\"code\":\"%s\",\"url\":\"%s\",\"clicks\":%d,\"creator\":\"%s\",\"expired\":%b},",
                            url.getShortCode(), url.getLongUrl(), url.getClickCount(), url.getCreator(), url.isExpired()));
                    }
                    if (json.length() > 1) json.setLength(json.length() - 1);
                    json.append("]");
                    
                    sendResponse(exchange, 200, json.toString());
                });

                // 2.a DELETE API: /api/admin/delete?code=...
                server.createContext("/api/admin/delete", exchange -> {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    String code = params.getOrDefault("code", "");
                    
                    if (service.deleteURL(code)) {
                        sendResponse(exchange, 200, "{\"status\": \"Deleted\"}");
                    } else {
                        sendResponse(exchange, 404, "{\"error\": \"Code not found\"}");
                    }
                });

                // 3. SMART REDIRECTOR
                server.createContext("/r/", exchange -> {
                    String path = exchange.getRequestURI().getPath();
                    String code = path.substring(path.lastIndexOf("/") + 1);
                    String userAgent = exchange.getRequestHeaders().getFirst("User-Agent");
                    
                    String destination = service.redirect(code, userAgent);
                    
                    if (destination != null) {
                        if (destination.equals("EXPIRED")) {
                            sendResponse(exchange, 410, "<html><body style='font-family:sans-serif;text-align:center;padding:50px;'><h1>⏰ Link Expired</h1><p>This link has reached its click limit or expiration time.</p></body></html>");
                        } else {
                            exchange.getResponseHeaders().add("Location", destination);
                            exchange.sendResponseHeaders(302, -1);
                        }
                    } else {
                        sendResponse(exchange, 404, "URL Not Found");
                    }
                    exchange.close();
                });

                server.setExecutor(null);
                server.start();
            } catch (Exception e) {}
        }).start();
    }

    private static Map<String, String> parseQuery(String query) {
        if (query == null) return Map.of();
        return java.util.Arrays.stream(query.split("&"))
            .map(s -> s.split("="))
            .collect(Collectors.toMap(
                a -> a[0], 
                a -> a.length > 1 ? URLDecoder.decode(a[1], StandardCharsets.UTF_8) : ""
            ));
    }

    private static void sendResponse(com.sun.net.httpserver.HttpExchange exchange, int code, String content) throws java.io.IOException {
        exchange.sendResponseHeaders(code, content.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content.getBytes());
        }
        exchange.close();
    }
}
