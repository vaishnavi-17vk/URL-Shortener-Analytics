package repository;

import model.URL;
import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class URLRepository {
    private Map<String, URL> map = new HashMap<>();
    private final String FILE_PATH = "data.txt";

    public URLRepository() {
        loadData();
    }

    public synchronized void save(URL url) {
        map.put(url.getShortCode(), url);
        saveData();
    }

    public synchronized void delete(String shortCode) {
        if (map.containsKey(shortCode)) {
            map.remove(shortCode);
            saveData();
        }
    }

    public URL find(String shortCode) {
        return map.get(shortCode);
    }

    public Map<String, URL> getAll() {
        return map;
    }

    private void saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (URL url : map.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(url.getShortCode()).append("|")
                  .append(url.getLongUrl()).append("|")
                  .append(url.getClickCount()).append("|")
                  .append(url.getExpiryTime() != null ? url.getExpiryTime() : "null").append("|")
                  .append(url.getMaxClicks() != null ? url.getMaxClicks() : "null").append("|")
                  .append(url.getCreator() != null ? url.getCreator() : "User");
                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    URL url = new URL(parts[0], parts[1]);
                    // Set preserved click count
                    int savedClicks = Integer.parseInt(parts[2]);
                    for(int i=0; i<savedClicks; i++) url.recordClick("Desktop"); // Mocking past clicks as desktop
                    
                    if (parts.length >= 6) {
                        if (!parts[3].equals("null")) url.setExpiryTime(LocalDateTime.parse(parts[3]));
                        if (!parts[4].equals("null")) url.setMaxClicks(Integer.parseInt(parts[4]));
                        url.setCreator(parts[5]);
                    }
                    map.put(parts[0], url);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}
