package com.example.service;

import com.example.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.NotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class LoginService {
    private static final Map<String, User> userStore = new ConcurrentHashMap<>();

    public LoginService() {
        String preAddedUserId = UUID.randomUUID().toString();
        String preAddedEmail = "jonathan@example.com";
        User preAddedUser = new User(preAddedUserId, "Jonathan", preAddedEmail);
        userStore.put(preAddedUserId, preAddedUser);
    }

    // Metod för att lägga till en användare och returnera ett resultat
    public Map<String, String> loggaIn(String email) {
        // Kontrollera om användaren redan finns
        boolean userExists = checkUserExists(email);
        var existingUSer = getUserByEmail(email);

        // Om användaren redan finns, returnera token och meddelande
        if (userExists) {
            Map<String, String> response = new HashMap<>();
            var token =  UUID.randomUUID().toString();
            existingUSer.setToken(token);
            response.put("token", token);
            response.put("message", "Login link sent");
            return response;
        }
        throw new NotFoundException("Användare finns ej");
    }

    public boolean verifyToken(String token) {
        return userStore.values().stream()
                .anyMatch(user -> user.getToken() != null && user.getToken().equals(token));
    }


    // Metod för att kontrollera om en användare redan finns i userStore
    public boolean checkUserExists(String email) {
        return userStore.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    private User getUserByEmail(String email) {
        return userStore.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
    }
