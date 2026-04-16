package repository;

import model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private List<User> users = new ArrayList<>();
    private final String FILE_PATH = "users.dat";

    public UserRepository() {
        loadData();
        // Default users if empty
        if (users.isEmpty()) {
            users.add(new User("admin", "admin123", "Admin"));
            users.add(new User("guest", "guest123", "Guest"));
            saveData();
        }
    }

    public synchronized void save(User user) {
        users.add(user);
        saveData();
    }

    public List<User> getAll() {
        return users;
    }

    public User findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (List<User>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
}
