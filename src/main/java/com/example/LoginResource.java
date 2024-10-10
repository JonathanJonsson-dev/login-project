package com.example;

import com.example.model.User;
import com.example.service.LoginService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT" // or any other token format you use
)
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {

    @Inject
    LoginService loginService;

    @POST
    @Path("/login")
    @Operation(summary = "Get token with email")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Login link sent",
                    content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "404", description = "Användare finns ej",
                    content = @Content(mediaType = "application/json"))
    })
    public Response loggaIn(@Valid LoginRequest loginRequest) {
        try {
            Map<String, String> response = loginService.loggaIn(loginRequest.getEmail());
            return Response.status(Response.Status.OK).entity(response).build();
        }
        catch (NotFoundException e) {
            // Hantera användare ej hittad, returnera meddelande och status 404
            Map<String, String> errorResponse = Map.of("message", "Användare finns ej");
            return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
        }
    }

    @GET
    @Path("/requestaccess")
    @Operation(summary = "Request access with Authorization")
    @SecurityRequirement(name = "bearerAuth") // Reference the security scheme by name
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Access granted",
                    content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json"))
    })
    public Response requestAccess(@HeaderParam ("Authorization") String authHeader) {
        // Kontrollera om Authorization-headern finns och är korrekt
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Authorization header is missing or invalid"))
                    .build();
        }

        // Extrahera token från Authorization-headern
        String token = authHeader.substring("Bearer".length()).trim();

        // Verifiera tokenet (du kan implementera din egen tokenverifieringslogik)
        boolean isTokenValid = loginService.verifyToken(token);

        if (!isTokenValid) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Invalid token"))
                    .build();
        }

        // Om tokenet är giltigt, fortsätt med att ge åtkomst
        return Response.status(Response.Status.OK)
                .entity(Map.of("message", "Access granted"))
                .build();
    }
}
