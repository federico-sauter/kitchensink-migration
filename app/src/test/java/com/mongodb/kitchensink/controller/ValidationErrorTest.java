package com.mongodb.kitchensink.controller;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidationErrorTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/kitchensink";
    }

    @Test
    void registerMember_invalidPayload() {
        // Missing name, invalid email and phone format
        String badJson = "{\"email\":\"not-an-email\",\"phoneNumber\":\"abc\"}";

        given()
            .contentType("application/json")
            .body(badJson)
        .when()
            .post("/rest/members")
        .then()
            .log().ifValidationFails()
            .statusCode(400);
    }
}
