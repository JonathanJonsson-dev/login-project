package com.example;

import com.example.service.LoginService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class LoginResourceTest {

    @InjectMock
    LoginService loginService;

    @Test
    public void testLoggaInSuccess() {
        // Definiera ett mockat svar för loggaIn-metoden
        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("message", "Login link sent");
        mockResponse.put("token", "mock-token");

        // Mocka LoginService så att den returnerar det fördefinierade svaret
        Mockito.when(loginService.loggaIn("test@example.com")).thenReturn(mockResponse);

        // Testa att endpointen returnerar det mockade svaret
        given()
                .header("Content-Type", "application/json")
                .body("{\"email\":\"test@example.com\"}")
                .when().post("api/login")
                .then()
                .statusCode(200)
                .body("message", is("Login link sent"))
                .body("token", is("mock-token"));
    }

    @Test
    public void testLoginUserDoesnotExist(){
        Mockito.when(loginService.loggaIn("test@example.com")).thenThrow(NotFoundException.class);
        // Skicka en tom JSON body istället för en tom LoginRequest-instans
        given()
                .header("Content-Type", "application/json")
                .body("{\"email\":\"test@example.com\"}")
                .when().post("api/login")
                .then()
                .statusCode(404)
                .body("message", is("Användare finns ej"));
    }

    @Test
    public void testRequestAccessWithValidToken() {
        // Definiera ett mockat svar för loggaIn-metoden
        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("message", "Login link sent");
        mockResponse.put("token", "mock-token");

        // Mocka LoginService så att den returnerar det fördefinierade svaret
        Mockito.when(loginService.loggaIn("test@example.com")).thenReturn(mockResponse);

        // 1. Skicka en POST-begäran till /login för att få en token
        String email = "{\"email\":\"test@example.com\"}";
        String token = given()
                .header("Content-Type", "application/json")
                .body(email)
                .when().post("api/login")
                .then()
                .statusCode(200)
                .extract().path("token");  // Extrahera token från JSON-svaret
        Mockito.when(loginService.verifyToken(token)).thenReturn(true);
        // 2. Använd token som Authorization header för att testa /requestaccess
        given()
                .header("Authorization", "Bearer " + token)  // Skicka token i Authorization-header
                .when().get("api/requestaccess")
                .then()
                .statusCode(200)
                .body("message", is("Access granted"));  // Kontrollera att access beviljas
    }

    @Test
    public void testRequestAccessWithInvalidToken() {
        // Testa med en ogiltig token
        String invalidToken = "invalid-token";

        Mockito.when(loginService.verifyToken(invalidToken)).thenReturn(false);

        given()
                .header("Authorization", "Bearer " + invalidToken)  // Skicka ogiltig token i Authorization-header
                .when().get("api/requestaccess")
                .then()
                .statusCode(401)
                .body("message", is("Invalid token"));  // Kontrollera att access nekas
    }
}