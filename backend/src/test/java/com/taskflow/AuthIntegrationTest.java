package com.taskflow;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/taskflow",
        "spring.datasource.username=postgres",
        "spring.datasource.password=Atlas24!",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.jpa.open-in-view=false",
        "app.jwt.secret=test-secret-key-for-integration-tests-only-minimum-32-chars",
        "app.jwt.expiration=86400000",
        "server.shutdown=graceful"
})
public class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "name": "Test User",
                    "email": "integrationtest@example.com",
                    "password": "password123"
                }
            """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .body("token", notNullValue())
                .body("email", equalTo("integrationtest@example.com"))
                .body("name", equalTo("Test User"));
    }

    @Test
    void shouldReturn401ForWrongPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "email": "test@example.com",
                    "password": "wrongpassword"
                }
            """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401)
                .body("error", equalTo("Invalid email or password"));
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/projects")
                .then()
                .statusCode(401)
                .body("error", equalTo("unauthorized"));
    }
}