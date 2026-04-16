import repository.URLRepository;
import repository.UserRepository;
import service.URLShortenerService;
import controller.URLController;
import model.AbstractLink;
import model.User;
import security.LoginService;
import exception.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.io.File;
import java.nio.file.Files;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        URLRepository repo = new URLRepository();
        UserRepository userRepo = new UserRepository();
        LoginService loginService = new LoginService(userRepo);
        
        URLShortenerService service = new URLShortenerService(repo);
        URLController controller = new URLController(service);

        startWebServer(service, repo, userRepo, loginService);
        controller.start();
    }

    private static void startWebServer(URLShortenerService service, URLRepository repo, UserRepository userRepo, LoginService loginService) {
        new Thread(() -> {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
                
                // 1. Static Content (Frontend)
                server.createContext("/", exchange -> {
                    String path = exchange.getRequestURI().getPath();
                    if (path.equals("/")) path = "/index.html";
                    
                    File file = new File("web" + path);
                    if (file.exists() && !file.isDirectory()) {
                        String contentType = path.endsWith(".css") ? "text/css" : "text/html";
                        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                        exchange.getResponseHeaders().add("Content-Type", contentType);
                        byte[] content = Files.readAllBytes(file.toPath());
                        exchange.sendResponseHeaders(200, content.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(content);
                        }
                    } else {
                        String error = "File Not Found";
                        exchange.sendResponseHeaders(404, error.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(error.getBytes());
                        }
                    }
                    exchange.close();
                });

                // 2. API: /api/shorten
                server.createContext("/api/shorten", exchange -> {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    String longUrl = params.getOrDefault("url", "");
                    String alias = params.get("alias");
                    Integer expiry = params.containsKey("expiry") ? Integer.parseInt(params.get("expiry")) : null;
                    Integer maxClicks = params.containsKey("maxClicks") ? Integer.parseInt(params.get("maxClicks")) : null;
                    String password = params.get("password");
                    String creator = params.getOrDefault("creator", "Guest");
                    
                    try {
                        String code = service.shortenURL(longUrl, alias, expiry, maxClicks, creator, password);
                        String response = String.format("{\"shortUrl\": \"http://localhost:8081/r/%s\", \"code\": \"%s\", \"clicks\": 0}", code, code);
                        sendResponse(exchange, 200, response);
                    } catch (InvalidURLException e) {
                        sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
                    } catch (Exception e) {
                        sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
                    }
                });

                // 2.a LOGIN API
                server.createContext("/api/login", exchange -> {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    User user = loginService.login(params.get("u"), params.get("p"));
                    if (user != null) {
                        sendResponse(exchange, 200, String.format("{\"status\":\"success\",\"username\":\"%s\",\"role\":\"%s\"}", user.getUsername(), user.getRole()));
                    } else {
                        sendResponse(exchange, 401, "{\"status\":\"fail\"}");
                    }
                });

                // 2.b SIGNUP API
                server.createContext("/api/signup", exchange -> {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    String u = params.get("u");
                    String p = params.get("p");
                    
                    if (u != null && p != null && userRepo.findByUsername(u) == null) {
                        userRepo.save(new User(u, p, "Guest"));
                        sendResponse(exchange, 201, "{\"status\":\"success\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"status\":\"fail\",\"error\":\"Username exists\"}");
                    }
                });

                // 3. ADMIN Stats API (with RBAC)
                server.createContext("/api/admin/stats", exchange -> {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    
                    try {
                        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                        String username = params.getOrDefault("username", "Guest");
                        User user = userRepo.findByUsername(username);

                        StringBuilder json = new StringBuilder("[");
                        for (AbstractLink link : repo.getAll(user)) {
                            String topDevice = link.getDeviceStats().entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey).orElse("None");
                                
                            json.append(String.format("{\"code\":\"%s\",\"url\":\"%s\",\"clicks\":%d,\"creator\":\"%s\",\"expired\":%b,\"protected\":%b,\"topDevice\":\"%s\"},",
                                link.getShortCode(), link.getLongUrl(), link.getClickCount(), link.getCreator(), link.isExpired(), link.getPassword() != null, topDevice));
                        }
                        if (json.length() > 1) json.setLength(json.length() - 1);
                        json.append("]");
                        sendResponse(exchange, 200, json.toString());
                    } catch (Exception e) {
                        System.err.println("Stats error: " + e.getMessage());
                        sendResponse(exchange, 500, "[]");
                    }
                });

                // 4. ADMIN Delete API
                server.createContext("/api/admin/delete", exchange -> {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    String code = params.getOrDefault("code", "");
                    
                    if (service.deleteURL(code)) {
                        sendResponse(exchange, 200, "{\"status\": \"Deleted\"}");
                    } else {
                        sendResponse(exchange, 404, "{\"error\": \"Not found\"}");
                    }
                });

                // 5. REDIRECTOR
                server.createContext("/r/", exchange -> {
                    String path = exchange.getRequestURI().getPath();
                    String[] segments = path.split("/");
                    String code = (segments.length > 0) ? segments[segments.length - 1] : "";
                    
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    String providedPassword = params.get("password");
                    String userAgent = exchange.getRequestHeaders().getFirst("User-Agent");
                    
                    try {
                        String destination = service.redirect(code, userAgent, providedPassword);
                        exchange.getResponseHeaders().add("Location", destination);
                        exchange.getResponseHeaders().add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                        exchange.sendResponseHeaders(302, -1);
                    } catch (AccessDeniedException e) {
                        String html = "<html><head><style>" +
                            "body { background: #0f172a; color: white; font-family: sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }" +
                            ".card { background: rgba(30, 41, 59, 0.7); backdrop-filter: blur(10px); padding: 2rem; border-radius: 16px; border: 1px solid rgba(255,255,255,0.1); text-align: center; }" +
                            "input { background: rgba(0,0,0,0.2); border: 1px solid rgba(99,102,241,0.5); color: white; padding: 0.8rem; border-radius: 8px; width: 100%; margin-bottom: 1rem; box-sizing: border-box; }" +
                            "button { background: #6366f1; color: white; border: none; padding: 0.8rem 2rem; border-radius: 8px; cursor: pointer; font-weight: 600; width: 100%; }" +
                            "</style></head><body><div class='card'>" +
                            "<h1>🔐 Protected Link</h1><p>This link requires a password to access.</p>" +
                            "<form method='GET'><input type='password' name='password' placeholder='Enter password' required><button type='submit'>Unlock Link</button></form>" +
                            "</div></body></html>";
                        sendResponse(exchange, 403, html);
                    } catch (LinkExpiredException e) {
                        sendResponse(exchange, 410, "<html><body style='background:#0f172a;color:white;font-family:sans-serif;text-align:center;'><h1>⏰ Link Expired</h1><p>" + e.getMessage() + "</p></body></html>");
                    } catch (Exception e) {
                        sendResponse(exchange, 404, "Code Not Found");
                    }
                    exchange.close();
                });

                // 6. CSV EXPORT API
                server.createContext("/api/admin/export", exchange -> {
                    exchange.getResponseHeaders().add("Content-Type", "text/csv");
                    exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=links_report.csv");
                    
                    StringBuilder csv = new StringBuilder("Code,Long URL,Clicks,Creator,Expired\n");
                    for (AbstractLink link : repo.getRawMap().values()) {
                        csv.append(String.format("%s,%s,%d,%s,%b\n", 
                            link.getShortCode(), link.getLongUrl(), link.getClickCount(), link.getCreator(), link.isExpired()));
                    }
                    sendResponse(exchange, 200, csv.toString());
                });

                server.setExecutor(Executors.newFixedThreadPool(10));
                server.start();
                System.out.println("[System] Full-Stack Server active at http://localhost:8081");
            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
            }
        }).start();
    }

    private static Map<String, String> parseQuery(String query) {
        if (query == null) return Map.of();
        return java.util.Arrays.stream(query.split("&"))
            .map(s -> s.split("="))
            .collect(Collectors.toMap(
                a -> a[0], 
                a -> (a.length > 1) ? URLDecoder.decode(a[1], StandardCharsets.UTF_8) : ""
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
