package controller;

import service.URLShortenerService;
import exception.*;
import java.util.Scanner;

public class URLController {
    private URLShortenerService service;
    private Scanner sc = new Scanner(System.in);

    public URLController(URLShortenerService service) {
        this.service = service;
    }

    public void start() {
        System.out.println("LinkStream CLI Ready.");
    }
}
