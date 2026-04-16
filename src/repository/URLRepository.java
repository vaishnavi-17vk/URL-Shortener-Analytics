package repository;

import model.AbstractLink;
import model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class URLRepository {
    private Map<String, AbstractLink> map = new ConcurrentHashMap<>();
    private final String FILE_PATH = "links.dat";
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public URLRepository() {
        loadData();
        // Auto-save every 10 seconds
        scheduler.scheduleAtFixedRate(this::saveData, 10, 10, TimeUnit.SECONDS);
    }

    public void save(AbstractLink link) {
        map.put(link.getShortCode(), link);
    }

    public void delete(String shortCode) {
        map.remove(shortCode);
    }

    public AbstractLink find(String shortCode) {
        return map.get(shortCode);
    }

    // Access Control: Admin gets all, Guests get only theirs
    public List<AbstractLink> getAll(User user) {
        if (user != null && user.isAdmin()) {
            return new ArrayList<>(map.values());
        }
        String username = (user != null) ? user.getUsername() : "Guest";
        return map.values().stream()
                .filter(l -> username.equals(l.getCreator()))
                .collect(Collectors.toList());
    }

    public Map<String, AbstractLink> getRawMap() {
        return map;
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(new ArrayList<>(map.values()));
            oos.flush();
        } catch (IOException e) {
            System.err.println("Error saving links: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<AbstractLink> list = (List<AbstractLink>) ois.readObject();
            for (AbstractLink link : list) {
                map.put(link.getShortCode(), link);
            }
        } catch (Exception e) {
            System.err.println("Error loading links: " + e.getMessage());
        }
    }
}
