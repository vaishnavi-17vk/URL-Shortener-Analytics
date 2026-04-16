package controller;

import service.URLShortenerService;
import model.URL;
import java.util.Scanner;

public class URLController {
    private URLShortenerService service;
    private Scanner sc = new Scanner(System.in);

    public URLController(URLShortenerService service) {
        this.service = service;
    }

    public void start() {
        System.out.println("====================================");
        System.out.println("   ✨ LINKSTREAM CLI v2.0 ✨   ");
        System.out.println("====================================");
        
        while (true) {
            System.out.println("\n[1] Shorten URL (Basic)");
            System.out.println("[2] Redirect (Simulate Click)");
            System.out.println("[3] System Stats (Admin)");
            System.out.println("[4] Exit");
            System.out.print("\nChoose an option: ");

            String choiceStr = sc.nextLine();
            int choice;
            try { choice = Integer.parseInt(choiceStr); } 
            catch (NumberFormatException e) { continue; }

            switch (choice) {
                case 1:
                    System.out.print("Enter long URL: ");
                    String longUrl = sc.nextLine();
                    // Using defaults for CLI: null alias, null expiry, null max clicks, creator=CLI
                    String shortCode = service.shortenURL(longUrl, null, null, null, "CLI");
                    System.out.println("\n✅ Success! Branded Link: short.ly/" + shortCode);
                    break;

                case 2:
                    System.out.print("Enter short code: ");
                    String code = sc.nextLine();
                    // Simulating a Desktop click via CLI
                    String redirectUrl = service.redirect(code, "Desktop/CLI");
                    if (redirectUrl != null) {
                        if (redirectUrl.equals("EXPIRED")) System.out.println("❌ Error: This link has expired.");
                        else System.out.println("🚀 Redirecting to: " + redirectUrl);
                    } else {
                        System.out.println("❌ Error: URL not found.");
                    }
                    break;

                case 3:
                    System.out.println("\n📊 TOTAL SYSTEM ANALYTICS");
                    System.out.println("-------------------------");
                    // We can reuse the details for a simple CLI dump
                    System.out.println("Access the internal Admin Dashboard for full visual stats.");
                    break;

                case 4:
                    System.out.println("Closing LinkStream... Done.");
                    return;
                
                default:
                    System.out.println("❌ Invalid choice.");
            }
        }
    }
}
